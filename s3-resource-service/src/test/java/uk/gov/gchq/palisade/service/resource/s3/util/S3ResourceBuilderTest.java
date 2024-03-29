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

package uk.gov.gchq.palisade.service.resource.s3.util;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.util.AbstractResourceBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class S3ResourceBuilderTest {

    @Test
    void testResourceBuilderAcceptsS3Scheme() {
        // Given
        String resourceUri = "s3://bucket/some/object";

        // When
        var resource = AbstractResourceBuilder.create(resourceUri);

        // Then
        assertThat(resource)
                .as("Check that when building a resource with an s3 prefix, it is an instance of a FileResource")
                .isInstanceOf(FileResource.class)
                .as("Check that the resourceId is formatted correctly")
                .extracting(Resource::getId)
                .isEqualTo(resourceUri);

        // Given
        var directoryResourceUri = "s3://bucket/some/";

        // When
        var directoryResource = AbstractResourceBuilder.create(directoryResourceUri);

        // Then
        assertThat(directoryResource)
                .as("Check that when building a resource with an s3 prefix, it is an instance of a DirectoryResource")
                .isInstanceOf(DirectoryResource.class)
                .as("Check that the resource is the parent of the first resource")
                .isEqualTo(((FileResource) resource).getParent());

        // Given
        var bucketResourceUri = "s3://bucket/";

        // When
        var bucketResource = AbstractResourceBuilder.create(bucketResourceUri);

        // Then
        assertThat(bucketResource)
                .as("Check that when building a resource with an s3 prefix, it is an instance of a SystemResource")
                .isInstanceOf(SystemResource.class)
                .as("Check that the resource is the parent of the second resource")
                .isEqualTo(((DirectoryResource) directoryResource).getParent());
    }

    @Test
    void testResourceBuilderAlsoAcceptsFileScheme() {
        // Given
        var resourceUri = "file:/some/object";

        // When
        var resource = AbstractResourceBuilder.create(resourceUri);

        // Then
        assertThat(resource)
                .as("Check that when building a resource with a file prefix, it is an instance of a FileResource")
                .isInstanceOf(FileResource.class)
                .as("Check that the resourceId is formatted correctly")
                .extracting(Resource::getId)
                .isEqualTo(resourceUri);
    }
}
