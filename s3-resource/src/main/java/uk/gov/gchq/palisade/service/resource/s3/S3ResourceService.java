/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.service.resource.s3;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.BucketAccess;
import akka.stream.alpakka.s3.ListBucketResultContents;
import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.service.ResourceService;
import uk.gov.gchq.palisade.util.ResourceBuilder;
import uk.gov.gchq.palisade.util.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static uk.gov.gchq.palisade.service.resource.s3.S3Properties.S3_PREFIX;

/**
 * An implementation of the ResourceService.
 * This service is for the retrieval of Resources only. Resources cannot be added via this Service, they should be added
 * through S3.
 */
public class S3ResourceService implements ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ResourceService.class);
    private static final int PARALLELISM = 1;
    private static final URI ROOT_URI = URI.create("s3:/");

    private final S3Properties properties;
    private final Materializer materialiser;

    /**
     * Constructor for the S3ResourceService, taking in S3Properties and a materaliser
     *
     * @param properties   S3Properties, containing bucketName, connection detail and headers.
     * @param materialiser The Materializer is responsible for turning a stream blueprint into a running stream.
     * @throws IOException if there was an exception thrown when checking if the bucket exists
     */
    public S3ResourceService(final S3Properties properties, final Materializer materialiser) throws IOException {
        this.properties = properties;
        this.materialiser = materialiser;

        // Check bucket exists, or throw an exception
        this.checkBucketExists();
    }

    @Override
    public Iterator<LeafResource> getResourcesByResource(final Resource resource) {
        LOGGER.debug("Invoking getResourcesByResource with request: {}", resource);
        return getResourcesById(resource.getId());
    }

    @Override
    public Iterator<LeafResource> getResourcesById(final String resourceId) {
        return getResourcesByIdSource(resourceId)
                .runWith(StreamConverters.asJavaStream(), materialiser).iterator();
    }

    private Source<LeafResource, NotUsed> getResourcesByIdSource(final String resourceId) {
        var resourceUri = URI.create(resourceId);

        if (!resourceUri.getScheme().equals(S3_PREFIX)) {
            throw new UnsupportedOperationException(String.format(
                    "Requested resource scheme is out of scope for %s. Found: %s expected: %s",
                    S3ResourceService.class.getSimpleName(), resourceUri.getScheme(), S3_PREFIX
            ));
        }
        return getResourceObjects(resourceUri);
    }

    @Override
    public Iterator<LeafResource> getResourcesByType(final String type) {
        LOGGER.warn("No efficient implementation of getResourcesBySerialisedFormat for {}", S3ResourceService.class.getSimpleName());
        return getResourcesByTypeSource(type)
                .runWith(StreamConverters.asJavaStream(), materialiser).iterator();
    }

    private Source<LeafResource, NotUsed> getResourcesByTypeSource(final String type) {
        return getResourceObjects(ROOT_URI)
                .filter(resource -> resource.getType().equals(type));
    }

    @Override
    public Iterator<LeafResource> getResourcesBySerialisedFormat(final String serialisedFormat) {
        LOGGER.warn("No efficient implementation of getResourcesBySerialisedFormat for {}", S3ResourceService.class.getSimpleName());
        return getResourcesBySerialisedFormatSource(serialisedFormat)
                .runWith(StreamConverters.asJavaStream(), materialiser).iterator();
    }

    private Source<LeafResource, NotUsed> getResourcesBySerialisedFormatSource(final String serialisedFormat) {
        return getResourceObjects(ROOT_URI)
                .filter(resource -> resource.getSerialisedFormat().equals(serialisedFormat));
    }

    @Override
    public Boolean addResource(final LeafResource leafResource) {
        LOGGER.error("No such implementation of addResource for {}", S3ResourceService.class.getSimpleName());
        return false;
    }

    private Source<LeafResource, NotUsed> getResourceObjects(final URI resourceUri) {
        return listBucketWithMetadata(resourceUri.getSchemeSpecificPart())
                .map((Pair<ListBucketResultContents, ObjectMetadata> resourceMetaPair) -> {
                    // Create a uri of the format 's3:resource-key'
                    var fileUri = UriBuilder.create()
                            .withScheme(S3_PREFIX)
                            .withoutAuthority()
                            .withPath(resourceMetaPair.first().getKey())
                            .withoutQuery().withoutFragment();

                    // Get headers starting with USER_META_PREFIX, strip the prefix, collect to map
                    Map<String, String> userMetadata = resourceMetaPair.second().headers().stream()
                            .filter(header -> header.name().startsWith(properties.getUserMetaPrefix()))
                            .map(header -> Map.entry(header.name().substring(properties.getUserMetaPrefix().length()), header.value()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    // Get headers _NOT_ starting with USER_META_PREFIX, collect to map
                    Map<String, String> systemMetadata = resourceMetaPair.second().headers().stream()
                            .filter(header -> !header.name().startsWith(properties.getUserMetaPrefix()))
                            .map(header -> Map.entry(header.name(), header.value()))
                            .collect(Collectors.toSet())
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    // Build the S3Resource object
                    var serialisedFormat = Optional.ofNullable(userMetadata.get(properties.getPalisadeFormatHeader())).orElseGet(resourceMetaPair.second().contentType()::get);
                    return ((S3Resource) ((LeafResource) ResourceBuilder.create(fileUri))
                            .type(userMetadata.get(properties.getPalisadeTypeHeader()))
                            .serialisedFormat(serialisedFormat)
                            .connectionDetail(new SimpleConnectionDetail().serviceName(properties.getConnectionDetail())))
                            .userMetadata(userMetadata)
                            .systemMetadata(systemMetadata);
                });
    }

    /**
     * Check if the bucket exists in S3
     *
     * @return a {@link Boolean} true if access is granted to the bucket
     */
    public CompletionStage<Boolean> bucketExists() {
        return S3.checkIfBucketExistsSource(properties.getBucketName())
                .runWith(Sink.head(), materialiser)
                .thenApply(bucketAccess -> bucketAccess.equals(BucketAccess.accessGranted()));
    }

    private void checkBucketExists() throws IOException {
        if (this.bucketExists().toCompletableFuture().join().equals(false)) {
            throw new IOException(String.format("Bucket %s does not exist", properties.getBucketName()));
        }
    }

    /**
     * Check the resource exists in the bucket by calling {@link S3#listBucket(String, Optional)}, if it does, then get
     * the objectMetadata by calling {@link S3#getObjectMetadata(String, String)}.
     * If the resource exists, return a pair of the contents and metadata, otherwise throw a Runtime Exception with the
     * object key.
     *
     * @param resourcePrefix the (prefix of a) resource the user wants to request from S3
     * @return a Pair of the Contents and metadata for that resource
     */
    private Source<Pair<ListBucketResultContents, ObjectMetadata>, NotUsed> listBucketWithMetadata(final String resourcePrefix) {
        // List the contents of the bucket, and if the resource exists, get the metadata for the resource
        // Then return the value as a Pair of Contents and the resources metadata
        return S3.listBucket(properties.getBucketName(), Optional.of(resourcePrefix))
                .flatMapMerge(PARALLELISM, bucketContents -> S3.getObjectMetadata(properties.getBucketName(), bucketContents.getKey())
                        .map(objectMetadata -> Pair.create(bucketContents, objectMetadata
                                .orElseThrow(() -> new RuntimeException(String.format("Lost object '%s' while listing bucket", bucketContents.getKey()))))));
    }
}
