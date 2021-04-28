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

package uk.gov.gchq.palisade.service.resource.s3;

import akka.stream.Materializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.gchq.palisade.service.resource.service.ResourceService;

import java.io.IOException;

/**
 * A Spring S3 Configuration class, creating the necessary beans for an implementation of a {@link S3ResourceService}
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Configuration {
    public static final String S3_PREFIX = "s3";
    public static final String S3_PATH_SEP = "/";

    /**
     * Bean implementation for {@link S3ResourceService}  and is used for setting s3Configurations and reading available resources.
     * //     * @param configuration a s3 configuration specifying the target cluster
     *
     * @param materialiser the materialiser
     * @return a new instance of {@link S3ResourceService}
     * @throws IOException ioException
     */
    @Bean
    @ConditionalOnProperty(prefix = "data", name = "implementation", havingValue = S3_PREFIX)
    ResourceService s3ResourceService(final S3Properties properties, final Materializer materialiser) throws IOException {
        return new S3ResourceService(properties, materialiser);
    }
}
