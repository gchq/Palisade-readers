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

package uk.gov.gchq.palisade.service.data.s3.util;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link S3ResourceBuilder}.
 */
class S3ResourceBuilderTest {

    @Test
    void testResourceBuilderAcceptsS3Scheme() {
        // Given
        String resourceUri = "s3://bucket/some/object";

        // When
        Resource resource = ResourceBuilder.create(resourceUri);

        // Then
        assertThat(resource)
                .as("Check that when building a resource with a s3 prefix, it is an instance of a FileResource")
                .isInstanceOf(FileResource.class)
                .as("Check that the resourceId is formatted correctly")
                .extracting(Resource::getId)
                .isEqualTo(resourceUri);
    }

    @Test
    void testResourceBuilderAlsoAcceptsFileScheme() {
        // Given
        String resourceUri = "file:/some/object";

        // When
        Resource resource = ResourceBuilder.create(resourceUri);

        // Then
        assertThat(resource)
                .as("Check that when building a resource with a file prefix, it is an instance of a FileResource")
                .isInstanceOf(FileResource.class)
                .as("Check that the resourceId is formatted correctly")
                .extracting(Resource::getId)
                .isEqualTo(resourceUri);
    }
}
