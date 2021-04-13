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

package uk.gov.gchq.palisade.service.data.reader.hadoop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.gov.gchq.palisade.service.data.DataApplication;
import uk.gov.gchq.palisade.service.data.common.data.reader.DataReader;
import uk.gov.gchq.palisade.service.data.service.SimpleDataService;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {DataApplication.class},
        properties = {"data.reader.implementation=hadoop", "data.service.implementation=simple"}
)
class HadoopDataApplicationTest {
    @Autowired
    SimpleDataService dependant;

    @Test
    void testHadoopDataReaderIsInjected() throws IllegalAccessException, NoSuchFieldException {
        // Given Spring DI has created an object dependant on a DataReader

        // When we inspect the instance injected into the dependant
        Field injectedField = dependant.getClass().getDeclaredField("dataReader");
        injectedField.setAccessible(true);
        DataReader injectedInstance = (DataReader) injectedField.get(dependant);
        injectedField.setAccessible(false);

        // The instance is a HadoopResourceService
        assertThat(injectedInstance)
                .isInstanceOf(HadoopDataReader.class);
    }
}
