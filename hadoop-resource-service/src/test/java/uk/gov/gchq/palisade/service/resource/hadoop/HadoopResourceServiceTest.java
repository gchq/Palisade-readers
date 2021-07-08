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

package uk.gov.gchq.palisade.service.resource.hadoop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.util.AbstractResourceBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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

        URI uri = new File(".").toURI().resolve("type_file.format");
        var details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // When
        var resource = service.addConnectionDetail(details);

        // Then
        assertThat(resource.getConnectionDetail())
                .as("Check that the connection detail has been set correctly")
                .isEqualTo(dataService);

        assertThat(resource)
                .as("Check that resource has been retrieved successfully")
                .isEqualTo(details.getResource().connectionDetail(dataService));
    }

    @Test
    void testCannotAddResourcesAtRuntime() throws URISyntaxException {
        // Given this is a hadoop resource service

        // When
        var resource = (LeafResource) AbstractResourceBuilder.create(new URI("file:/hadoop/test_resource.avro"));
        boolean success = service.addResource(resource);

        // Then
        assertThat(success)
                .as("Check that you cant add files to the HadoopResourceService")
                .isFalse();
    }

    @Test
    void testGetConf() throws IOException {
        Map<String, String> conf = new HashMap<>();
        conf.put("Key", "Value");
        HadoopResourceService resourceService = new HadoopResourceService(conf);
        Map<String, String> configMap = resourceService.getConf();
        assertThat(configMap)
                .as("Check the returned configuration is not empty")
                .isNotEmpty();
    }
}
