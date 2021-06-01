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

package uk.gov.gchq.palisade.service.data.hadoop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.gchq.palisade.service.data.reader.DataReader;

import java.io.IOException;

/**
 * A Spring Hadoop Configuration class, creating the necessary beans for a Hadoop data reader
 */
@Configuration
@ConditionalOnClass(DataReader.class)
public class HadoopConfiguration {

    @Bean
    org.apache.hadoop.conf.Configuration hadoopConfiguration() {
        return new org.apache.hadoop.conf.Configuration();
    }

    /**
     * Bean implementation for {@link HadoopDataReader} which extends {@link uk.gov.gchq.palisade.service.data.reader.SerialisedDataReader} and is used for setting hadoopConfigurations and reading raw data.
     *
     * @param configuration a hadoop configuration specifying the target cluster
     * @return a new instance of {@link HadoopDataReader}
     * @throws IOException ioException
     */
    @Bean
    @ConditionalOnProperty(prefix = "data", name = "implementation", havingValue = "hadoop")
    DataReader hadoopDataReader(final org.apache.hadoop.conf.Configuration configuration) throws IOException {
        return new HadoopDataReader(configuration);
    }
}
