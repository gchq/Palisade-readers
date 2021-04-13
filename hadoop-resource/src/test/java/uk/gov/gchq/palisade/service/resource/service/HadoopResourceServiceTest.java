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

package uk.gov.gchq.palisade.service.resource.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.service.resource.common.resource.LeafResource;
import uk.gov.gchq.palisade.service.resource.common.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.common.util.ResourceBuilder;
import uk.gov.gchq.palisade.service.resource.util.HadoopResourceDetails;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class HadoopResourceServiceTest {
    private final HadoopResourceService service = new HadoopResourceService();

    @BeforeEach
    public void setup() {
        HadoopResourceDetails.addTypeSupport("type", "type");
    }

    @Test
    void testResourceDetailsGetDataServiceConnection() {
        // Given
        var dataService = new SimpleConnectionDetail().serviceName("data-service");
        service.addDataService(dataService);

        var uri = new File(".").toURI().resolve("type_file.format");
        var details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // When
        var resource = service.addConnectionDetail(details);

        // Then
        assertThat(resource.getConnectionDetail())
                .as("Check that the connection detail has been set correctly")
                .isEqualTo(dataService);

        assertThat(resource)
                .as("Check that the resource has the correct connection detail")
                .isEqualTo(details.getResource().connectionDetail(dataService));
    }

    @Test
    void testCannotAddResourcesAtRuntime() {
        // Given this is a hadoop resource service

        // When
        boolean success = service.addResource((LeafResource) ResourceBuilder.create("file:/hadoop/test_resource.avro"));

        // Then
        assertThat(success)
                .as("Check that you cannot add a resource to the hadoop resource service at runtime")
                .isFalse();
    }

}
