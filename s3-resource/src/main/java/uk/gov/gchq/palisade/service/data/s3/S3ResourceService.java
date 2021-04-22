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

import akka.japi.Pair;
import akka.stream.Attributes;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.BucketAccess.AccessDenied$;
import akka.stream.alpakka.s3.BucketAccess.AccessGranted$;
import akka.stream.alpakka.s3.ListBucketResultContents;
import akka.stream.alpakka.s3.impl.S3Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.resource.service.ResourceService;

import java.util.Iterator;
import java.util.Optional;

import static akka.stream.alpakka.s3.javadsl.S3.checkIfBucketExists;
import static akka.stream.alpakka.s3.javadsl.S3.getObjectMetadata;
import static akka.stream.alpakka.s3.javadsl.S3.listBucket;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of the ResourceService.
 * <p>
 * This service is for the retrieval of Resources only. Resources cannot be added via this service, they should be added
 * through the actual real filing system.
 */
public class S3ResourceService implements ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ResourceService.class);
    public static final String ERROR_ADD_RESOURCE = "AddResource is not supported by the Resource Service, resources should be added/created via regular file system behaviour.";
    public static final String ERROR_GET_BY_FORMAT = "getResourcesBySerialisedFormat is not supported by the Resource Service";
    public static final String ERROR_GET_BY_TYPE = "getResourcesByType is not supported by the Resource Service";
    public static final String NOT_EXISTS = "The Bucket, %s , does not exist or you do not have access to it.";
    private final Materializer materialiser;

    /**
     * Get the bucket name from the application.yaml
     */
    @Value("${s3-resource-service.bucket-name}")
    private String bucketName;

    public S3ResourceService(final Materializer materialiser) {
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
        if (validateBucket(bucketName, materialiser, null) == Boolean.TRUE) {

            listBucket(bucketName, Optional.of(resourceId))
                    .flatMap((ListBucketResultContents s3Resource) -> getObjectMetadata(bucketName, s3Resource.getKey(), null, null)
                            .map(maybeMeta -> Pair.create(s3Resource, maybeMeta.orElseThrow(new RuntimeException("our bad")))))
                    .map((resource, meta) -> ...)
        }
        return null;
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
     * The user has access to the existing bucket, then it will return AccessGranted
     * The user doesn’t have access but the bucket exists so AccessDenied will be returned
     * The bucket doesn’t exist, the method will return NotExists
     *
     * @param bucket the String bucket the user wants access too
     * @param mat    the materaliser
     * @param attr   any additional attributes
     * @return a Boolean value if the user has access to the bucket or not
     */
    public Boolean validateBucket(final String bucket, final Materializer mat, final Attributes attr) {
        var bucketAccessFuture = checkIfBucketExists(bucket, mat, attr, null).toCompletableFuture().join();

        if (bucketAccessFuture instanceof AccessGranted$) {
            return Boolean.TRUE;
        } else if (bucketAccessFuture instanceof AccessDenied$) {
            return Boolean.FALSE;
        } else {
            throw new UnsupportedOperationException(String.format(NOT_EXISTS, bucket));
        }
    }
}
