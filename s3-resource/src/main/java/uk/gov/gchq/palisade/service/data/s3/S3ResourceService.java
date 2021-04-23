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

package uk.gov.gchq.palisade.service.data.s3;

import akka.NotUsed;
import akka.http.javadsl.model.HttpHeader;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.BucketAccess;
import akka.stream.alpakka.s3.ListBucketResultContents;
import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.resource.service.ResourceService;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the ResourceService.
 * This service is for the retrieval of Resources only. Resources cannot be added via this service, they should be added
 * through the actual real filing system.
 */
public class S3ResourceService implements ResourceService {
    public static final String ERROR_ADD_RESOURCE = "AddResource is not supported by the Resource Service, resources should be added/created via regular file system behaviour.";
    public static final String ERROR_GET_BY_FORMAT = "getResourcesBySerialisedFormat is not supported by the Resource Service";
    public static final String ERROR_GET_BY_TYPE = "getResourcesByType is not supported by the Resource Service";
    public static final String ERROR_BUCKET_DOES_NOT_EXIST = "The bucket %s does not exist, or is not known to S3";
    public static final String ERROR_OUT_SCOPE = "resourceId is out of scope of the this Resource Service. Found: %s expected: %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ResourceService.class);
    private static final int PARALLELISM = 1;
    private final String bucketName;
    private final Materializer materialiser;

    public S3ResourceService(final String bucketName, final Materializer materialiser) {
        bucketExists();
        this.bucketName = bucketName;
        this.materialiser = materialiser;
    }

    @Override
    public Iterator<LeafResource> getResourcesByResource(final Resource resource) {
        requireNonNull(resource, "resource");
        LOGGER.debug("Invoking getResourcesByResource with request: {}", resource);
        return getResourcesById(resource.getId());
    }

    @Override
    public Iterator<LeafResource> getResourcesById(final String resourceId) {
        requireNonNull(resourceId, "resourceId");
        var resourceUri = URI.create(resourceId);

        if (!resourceUri.getScheme().equals(S3Configuration.s3Prefix)) {
            throw new UnsupportedOperationException(java.lang.String.format(ERROR_OUT_SCOPE, resourceId, S3Configuration.s3Prefix));
        }

        return StreamConverters.asJavaStream(getResourcesObject(resourceUri.getPath()), materialiser).iterator();
    }

    private Source<LeafResource, NotUsed> getResourcesObject(final String resourceId) {
        return listBucketWithMetadata(resourceId)
                .map((Pair<ListBucketResultContents, ObjectMetadata> objectMetaPair) -> {
                    // Get the filename
                    var fileName = objectMetaPair.first().getKey();
                    // Get the content-type from the headers as the serialised format
                    var contentType = objectMetaPair.second().headers()
                            .stream().findFirst()
                            .map(HttpHeader::name)
                            .filter(name -> name.equals("Content-Type"))
                            .orElse(null);
                    // Build the LeafResource
                    return ((LeafResource) ResourceBuilder.create(fileName)).serialisedFormat(contentType);
                });
    }

    @Override
    @Deprecated
    public Iterator<LeafResource> getResourcesByType(final String type) {
        LOGGER.error(ERROR_GET_BY_TYPE);
        return null;
    }

    @Override
    @Deprecated
    public Iterator<LeafResource> getResourcesBySerialisedFormat(final String serialisedFormat) {
        LOGGER.error(ERROR_GET_BY_FORMAT);
        return null;
    }

    /**
     * Informs Palisade about a specific resource that it may return to users. This lets Palisade clients request access
     * to that resource and allows Palisade to provide policy controlled access to it via the other methods in this
     * interface.
     * This is not permitted by the S3ResourceService, so will always return failure (false)
     *
     * @param leafResource the resource that Palisade can manage access to
     * @return whether or not the addResource call completed successfully, always false
     */
    @Override
    public Boolean addResource(final LeafResource leafResource) {
        LOGGER.error(ERROR_ADD_RESOURCE);
        return false;
    }

    /**
     * Check if the bucket exists in S3
     *
     * @return a {@link Boolean} true if access is granted to the bucket
     */
    private Source<Boolean, NotUsed> bucketExists() {
        return S3.checkIfBucketExistsSource(bucketName)
                .map(bucketAccess -> bucketAccess.equals(BucketAccess.accessGranted()));
    }

    /**
     * Check the resource exists in the bucket by calling {@link S3#listBucket(String, Optional)}, if it does, then get the objectMetadata by calling {@link S3#getObjectMetadata(String, String)}.
     * If the resource exists, return a pair of the contents and metadata, otherwise throw a Runtime Exception with the object key.
     *
     * @param resourceId the resource the user wants to request from S3
     * @return a Pair of the Contents and metadata for that resource
     */
    private Source<Pair<ListBucketResultContents, ObjectMetadata>, NotUsed> listBucketWithMetadata(final String resourceId) {
        // List the contents of the bucket, and if the resource exists, get the metadata for the resource
        // Then return the value as a Pair of Contents and the resources metadata
        return S3.listBucket(bucketName, Optional.of(resourceId))
                .flatMapMerge(PARALLELISM, bucketContents -> S3.getObjectMetadata(bucketName, bucketContents.getKey())
                        .map(objectMetadata -> Pair.create(bucketContents, objectMetadata
                                .orElseThrow(() -> new RuntimeException(String.format("Lost object '%s' while listing bucket", bucketContents.getKey()))))));
    }
}
