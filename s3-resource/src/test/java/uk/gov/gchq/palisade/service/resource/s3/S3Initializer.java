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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class S3Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Initializer.class);

    @Value("${testcontainers.localstack.image}")
    private String fullImageName;
    @Value("${testcontainers.localstack.default.image}")
    private String defaultImageName;

    static LocalStackContainer lcs;

    @Override
    public void initialize(@NonNull final ConfigurableApplicationContext context) {

        DockerImageName localstackImageName;
        try {
            localstackImageName = DockerImageName.parse(fullImageName)
                    .asCompatibleSubstituteFor(defaultImageName);
            localstackImageName.assertValid();
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Image name {} was invalid, falling back to default name {}", fullImageName, defaultImageName, ex);
            localstackImageName = DockerImageName.parse(defaultImageName);
        }

        final GenericContainer<?> localStackContainer = new LocalStackContainer(localstackImageName)
                .withServices(S3)
                .withReuse(true);

        context.getEnvironment().setActiveProfiles("s3");
        // Start container
        localStackContainer.start();

        lcs = (LocalStackContainer) localStackContainer;
        var accessKey = "aws.accessKeyId=" + lcs.getDefaultCredentialsProvider().getCredentials().getAWSAccessKeyId();
        var secretKey = "aws.secretKey=" + lcs.getDefaultCredentialsProvider().getCredentials().getAWSSecretKey();
        var endpoint = "aws.endpoint=" + lcs.getEndpointConfiguration(S3).getServiceEndpoint();
        var region = "aws.region=" + lcs.getEndpointConfiguration(S3).getSigningRegion();

        LOGGER.info("Starting LocalStack S3 with accessKey: {}, secretKey: {}, endpoint: {}, and region: {}", accessKey, secretKey, endpoint, region);
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, accessKey, secretKey, endpoint, region);
    }
}
