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

package uk.gov.gchq.palisade.service.data.s3;

import akka.stream.Materializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.gchq.palisade.service.data.service.reader.DataReader;

import static uk.gov.gchq.palisade.service.data.s3.S3Properties.S3_PREFIX;

/**
 * A Spring S3 Configuration class, creating the necessary beans for an implementation of a {@link S3DataReader}
 */
@Configuration
@ConditionalOnClass(DataReader.class)
public class S3Configuration {

    /**
     * Bean implementation for {@link S3DataReader} which is used for setting s3Configurations and reading available resources.
     *
     * @param materialiser the materialiser
     * @return a new instance of {@link S3DataReader}
     */
    @Bean
    @ConditionalOnProperty(prefix = "data", name = "implementation", havingValue = S3_PREFIX)
    DataReader s3DataReader(final Materializer materialiser) {
        return new S3DataReader(materialiser);
    }
}
