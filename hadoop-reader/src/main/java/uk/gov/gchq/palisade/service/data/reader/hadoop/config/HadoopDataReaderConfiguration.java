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

package uk.gov.gchq.palisade.service.data.reader.hadoop.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.gchq.palisade.service.data.common.data.reader.SerialisedDataReader;
import uk.gov.gchq.palisade.service.data.reader.hadoop.HadoopDataReader;

import java.io.IOException;

@Configuration
public class HadoopDataReaderConfiguration {
    /**
     * Default (empty) hadoop configuration.
     *
     * @return a {@link org.apache.hadoop.conf.Configuration} to be used to configure the {@link HadoopDataReader} instance
     */
    @Bean
    @ConditionalOnProperty(prefix = "data.reader", name = "implementation", havingValue = "hadoop")
    org.apache.hadoop.conf.Configuration hadoopConfiguration() {
        return new org.apache.hadoop.conf.Configuration();
    }

    /**
     * Bean implementation for {@link HadoopDataReader} which extends {@link SerialisedDataReader} and is used for setting hadoopConfigurations and reading raw data.
     *
     * @param hadoopConfig the {@link org.apache.hadoop.conf.Configuration} for the target hadoop instance
     * @return a new instance of {@link HadoopDataReader}
     * @throws IOException ioException
     */
    @Bean
    @ConditionalOnProperty(prefix = "data.reader", name = "implementation", havingValue = "hadoop")
    HadoopDataReader hadoopDataReader(final org.apache.hadoop.conf.Configuration hadoopConfig) throws IOException {
        return new HadoopDataReader(hadoopConfig);
    }

}
