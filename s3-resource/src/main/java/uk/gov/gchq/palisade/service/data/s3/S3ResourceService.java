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

import akka.stream.alpakka.s3.S3Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.resource.service.ResourceService;

import java.util.Iterator;

import static akka.stream.alpakka.s3.impl.S3Stream.getObjectMetadata;

/**
 * An implementation of the ResourceService.
 * <p>
 * This service is for the retrieval of Resources only. Resources cannot be added via this service, they should be added
 * through the actual real filing system.
 */

public class S3ResourceService implements ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ResourceService.class);
    public static final String ERROR_ADD_RESOURCE = "AddResource is not supported by the Resource Service, resources should be added/created via regular file system behaviour.";


    @Override
    public Iterator<LeafResource> getResourcesByResource(final Resource resource) {
        return getResourcesById(resource.getId());
    }

    @Override
    public Iterator<LeafResource> getResourcesById(final String resourceId) {
        String bucketName = "";
        Option<String> versionId = Option.empty();
        S3Headers headers = new S3Headers(null, null, null, null, null);
        // A source of optional<objectMetadata> if the resource exists
        var x = getObjectMetadata(bucketName, resourceId, versionId, headers);
        return null;
    }

    @Override
    @Deprecated
    public Iterator<LeafResource> getResourcesByType(final String type) {
        return null;
    }

    @Override
    @Deprecated
    public Iterator<LeafResource> getResourcesBySerialisedFormat(final String serialisedFormat) {
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
}
