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
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.BucketAccess;
import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.data.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.data.reader.SerialisedDataReader;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletionStage;

import static uk.gov.gchq.palisade.service.data.s3.S3Properties.S3_PREFIX;

/**
 * An implementation of the ResourceService.
 * This service is for the retrieval of Resources only. Resources cannot be added via this Service, they should be added
 * through S3.
 */
public class S3DataReader extends SerialisedDataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3DataReader.class);
    private static final int PARALLELISM = 1;

    private final Materializer materialiser;

    /**
     * Constructor for the S3ResourceService, taking in S3Properties and a materaliser
     *
     * @param materialiser The Materializer is responsible for turning a stream blueprint into a running stream.
     */
    public S3DataReader(final Materializer materialiser) {
        this.materialiser = materialiser;
    }

    @Override
    protected InputStream readRaw(final LeafResource resource) {
        return readRawSource(resource)
                .runWith(StreamConverters.asInputStream(), materialiser);
    }

    protected Source<ByteString, NotUsed> readRawSource(final LeafResource resource) {
        URI resourceUri = URI.create(resource.getId());

        if (!resourceUri.getScheme().equals(S3_PREFIX)) {
            throw new UnsupportedOperationException(String.format(
                    "Requested resource scheme is out of scope for %s. Found: %s expected: %s",
                    S3DataReader.class.getSimpleName(), resourceUri.getScheme(), S3_PREFIX
            ));
        }

        String bucket = resourceUri.getHost();
        // Strip leading slash
        String resourcePrefix = resourceUri.getPath().substring(1);
        LOGGER.debug("Using bucket '{}' and prefix '{}'", bucket, resourcePrefix);

        return checkBucketAccessible(bucket)
                .flatMapMerge(PARALLELISM, access -> downloadObject(bucket, resourcePrefix))
                .flatMapMerge(PARALLELISM, Pair::first);
    }

    /**
     * Check if the bucket exists in S3.
     *
     * @param bucketName the name of the bucket to check, using the configured credentials
     * @return a {@link Boolean} true if access is granted to the bucket
     */
    public CompletionStage<Boolean> canAccess(final String bucketName) {
        return checkBucketAccessible(bucketName)
                .map(accessible -> true)
                .recover(IllegalArgumentException.class, () -> false)
                .runWith(Sink.head(), materialiser);
    }

    private Source<BucketAccess, NotUsed> checkBucketAccessible(final String bucketName) {
        return S3.checkIfBucketExistsSource(bucketName)
                .map((BucketAccess access) -> {
                    LOGGER.debug("Bucket existence check returned {}", access);
                    if (access == BucketAccess.accessDenied()) {
                        throw new IllegalArgumentException("Access denied to bucket " + bucketName);
                    } else if (access == BucketAccess.notExists()) {
                        throw new IllegalArgumentException("Could not find bucket " + bucketName);
                    } else {
                        return access;
                    }
                });
    }

    private Source<Pair<Source<ByteString, NotUsed>, ObjectMetadata>, NotUsed> downloadObject(final String bucketName, final String objectKey) {
        // List the contents of the bucket, and if the resource exists, get the metadata for the resource
        // Then return the value as a Pair of Contents and the resources metadata
        return S3.download(bucketName, objectKey)
                .map(foundObject -> {
                    LOGGER.debug("Download for object '{}' was present? {}", objectKey, foundObject.isPresent());
                    foundObject.ifPresent(sourceMetaPair -> LOGGER.trace("Object metadata was '{}'", sourceMetaPair.second().headers()));
                    return foundObject.orElseThrow(() -> new ForbiddenException("Resource access was denied, or the object no longer exists, for key " + objectKey));
                });
    }

}
