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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import uk.gov.gchq.palisade.service.resource.ResourceApplication;
import uk.gov.gchq.palisade.service.resource.common.resource.ResourceService;
import uk.gov.gchq.palisade.service.resource.service.ResourceServicePersistenceProxy;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {ResourceApplication.class},
        webEnvironment = WebEnvironment.NONE,
        properties = {"resource.implementation=hadoop"}
)
class HadoopResourceApplicationTest {
    @Autowired
    ResourceServicePersistenceProxy dependant;

    @Test
    void testHadoopResourceServiceIsInjected() throws IllegalAccessException, NoSuchFieldException {
        // Given Spring DI has created an object dependant on a ResourceService

        // When we inspect the instance injected into the dependant
        Field injectedField = dependant.getClass().getDeclaredField("delegate");
        injectedField.setAccessible(true);
        ResourceService injectedInstance = (ResourceService) injectedField.get(dependant);
        injectedField.setAccessible(false);

        // The instance is a HadoopResourceService
        assertThat(injectedInstance)
                .isInstanceOf(HadoopResourceService.class);
    }
}
