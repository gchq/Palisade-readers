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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.gchq.palisade.service.data.reader.DataReader;

import java.io.IOException;

import static uk.gov.gchq.palisade.service.data.s3.S3Properties.S3_PREFIX;

/**
 * A Spring Configuration class for creating a Spring Bean needed in reading the content of a S3 bucket.
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Configuration {

    /**
     * Bean implementation for {@link S3DataReader} which is used for reading an available resources.
     *
     * @param properties   a S3Properties specifying the target cluster
     * @param materialiser the materialiser
     * @return a new instance of {@link S3DataReader}
     * @throws IOException ioException
     */
    @Bean
    @ConditionalOnProperty(prefix = "data", name = "implementation", havingValue = S3_PREFIX)
    DataReader s3DataReader(final S3Properties properties, final Materializer materialiser) throws IOException {
        return new S3DataReader(properties, materialiser);
    }
}