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

package uk.gov.gchq.palisade.service.resource.hadoop.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import uk.gov.gchq.palisade.service.resource.hadoop.service.ConfiguredHadoopResourceService;
import uk.gov.gchq.palisade.service.resource.hadoop.service.HadoopResourceService;

import java.io.IOException;

@Configuration
public class ApplicationConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "resource", name = "implementation", havingValue = "hadoop")
    org.apache.hadoop.conf.Configuration hadoopConfiguration() {
        return new org.apache.hadoop.conf.Configuration();
    }

    @Bean
    @ConditionalOnProperty(prefix = "resource", name = "implementation", havingValue = "hadoop")
    HadoopResourceService hadoopResourceService(final org.apache.hadoop.conf.Configuration hadoopConf) throws IOException {
        return new ConfiguredHadoopResourceService(hadoopConf);
    }

    /**
     * A bean for the implementation of the HadoopResourceService which implements {@link ResourceService} used for retrieving resources from Hadoop
     *
     * @param config hadoop configuration
     * @return a {@link ConfiguredHadoopResourceService} used for adding connection details to leaf resources
     * @throws IOException ioexception
     */
    @Bean("hadoopResourceService")
    @ConditionalOnProperty(prefix = "resource", name = "implementation", havingValue = "hadoop")
    public HadoopResourceService hadoopResourceService(final org.apache.hadoop.conf.Configuration config) throws IOException {
        return new ConfiguredHadoopResourceService(config);
    }
}
