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
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.service.resource.s3.S3Resource;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class S3ResourceBuilderTest {

    @Test
    void testResourceBuilderAcceptsS3Scheme() {
        // Given
        String resourceUri = "s3:/some/object";

        // When
        Resource resource = ResourceBuilder.create(resourceUri);

        // Then
        assertThat(resource)
                .isInstanceOf(S3Resource.class);
    }

    @Test
    void testResourceBuilderAlsoAcceptsFileScheme() {
        // Given
        String resourceUri = "file:/some/object";

        // When
        Resource resource = ResourceBuilder.create(resourceUri);

        // Then
        assertThat(resource)
                .isInstanceOf(FileResource.class);
    }
}
