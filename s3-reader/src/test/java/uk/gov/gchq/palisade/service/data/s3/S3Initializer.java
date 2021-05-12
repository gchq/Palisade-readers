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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.s3.S3Client;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class S3Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Initializer.class);

    static LocalStackContainer LOCALSTACK_CONTAINER;

    @Override
    public void initialize(@NonNull final ConfigurableApplicationContext context) {
        final String fullImageName = context.getEnvironment().getRequiredProperty("testcontainers.localstack.image");
        final String defaultImageName = context.getEnvironment().getRequiredProperty("testcontainers.localstack.default.image");

        DockerImageName localstackImageName;
        try {
            localstackImageName = DockerImageName.parse(fullImageName)
                    .asCompatibleSubstituteFor(defaultImageName);
            localstackImageName.assertValid();
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Image name {} was invalid, falling back to default name {}", fullImageName, defaultImageName, ex);
            localstackImageName = DockerImageName.parse(defaultImageName);
        }

        LOCALSTACK_CONTAINER = new LocalStackContainer(localstackImageName)
                .withServices(S3)
                .withReuse(true);

        context.getEnvironment().setActiveProfiles("s3");

        // Start container
        LOCALSTACK_CONTAINER.start();

        var bucketKey = "s3.bucketName=";
        var bucketName = "testBucketName";
        var endpointKey = "alpakka.s3.endpoint-url=";
        var endpointName = LOCALSTACK_CONTAINER.getEndpointConfiguration(S3).getServiceEndpoint() + "/{bucket}";

        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), LOCALSTACK_CONTAINER.getAccessKey());
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), LOCALSTACK_CONTAINER.getSecretKey());
        System.setProperty(SdkSystemSetting.AWS_REGION.property(), LOCALSTACK_CONTAINER.getRegion());

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, endpointKey + endpointName, bucketKey + bucketName);

        var s3Client = S3Client
                .builder()
                .endpointOverride(LOCALSTACK_CONTAINER.getEndpointOverride(S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(LOCALSTACK_CONTAINER.getAccessKey(), LOCALSTACK_CONTAINER.getSecretKey())))
                .build();

        // Build the bucket
        s3Client.createBucket(b -> b.bucket(bucketName));
    }
}