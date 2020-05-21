/*
 * Copyright 2020 Crown Copyright
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.util.HadoopResourceDetails;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.io.File;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JUnit4.class)
public class HadoopResourceServiceTest {
    private final HadoopResourceService service = new HadoopResourceService();

    @Before
    public void setup() {
        HadoopResourceDetails.addTypeSupport("type", "type");
    }

    @Test
    public void resourceDetailsGetDataServiceConnection() {
        // Given
        ConnectionDetail dataService = new SimpleConnectionDetail().uri("http://data-service");
        service.addDataService(dataService);

        URI uri = new File(".").toURI().resolve("type_file.format");
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // When
        LeafResource resource = service.addConnectionDetail(details);

        // Then
        assertThat(resource.getConnectionDetail(), equalTo(dataService));
        assertThat(resource, equalTo(details.getResource().connectionDetail(dataService)));
    }

    @Test
    public void cannotAddResourcesAtRuntime() {
        // Given this is a hadoop resource service

        // When
        boolean success = service.addResource((LeafResource) ResourceBuilder.create("file:/hadoop/test_resource.avro"));

        assertThat(success, equalTo(false));
    }

}
