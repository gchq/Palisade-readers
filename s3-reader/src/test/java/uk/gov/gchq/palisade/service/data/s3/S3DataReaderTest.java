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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.data.stream.config.AkkaSystemConfig;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.gov.gchq.palisade.service.data.s3.S3Initializer.localstackContainer;

@SpringBootTest(classes = {S3Configuration.class, AkkaSystemConfig.class})
@ContextConfiguration(initializers = {S3Initializer.class})
@ActiveProfiles({"s3", "testcontainers"})
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
class S3DataReaderTest {
    @Autowired
    S3DataReader reader;

    private static final String BUCKET_NAME = "test-bucket";

    private S3Client s3;

    @BeforeAll
    void setup() {
        s3 = S3Client
                .builder()
                .endpointOverride(localstackContainer.getEndpointOverride(S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localstackContainer.getAccessKey(), localstackContainer.getSecretKey())))
                .build();

        // Build the bucket
        s3.createBucket(b -> b.bucket(BUCKET_NAME));
    }

    @Test
    @Order(1)
    void testAutowiring() {
        assertThat(reader)
                .as("Check that the service has been started successfully")
                .isNotNull();

        assertThat(localstackContainer)
                .as("Check that the localstack container has been started successfully")
                .isNotNull();
    }

    @Test
    @Order(2)
    void testBucketExits() {
        var bucketsMatchingPropertiesBucketName = s3.listBuckets()
                .buckets()
                .stream()
                .filter(b -> b.name().equals(BUCKET_NAME))
                .collect(Collectors.toList());

        assertThat(bucketsMatchingPropertiesBucketName)
                .as("Check one the bucket has been created in the Initializer")
                .asList()
                .hasSize(1)
                .extracting("name")
                .containsOnly(BUCKET_NAME);

        assertThat(reader.canAccess(BUCKET_NAME).toCompletableFuture().join())
                .as("Check that the S3ResourceService knows the bucket: %s, exists", BUCKET_NAME)
                .isTrue();
    }

    @Test
    @Order(3)
    void readResource() throws IOException {
        // We will add a test file to the bucket
        var s3Resource = (FileResource) ((LeafResource) ResourceBuilder.create("s3://" + BUCKET_NAME + "/testFile.txt"))
                .type("text")
                .serialisedFormat("text/plain; charset=UTF-8")
                .connectionDetail(new SimpleConnectionDetail().serviceName("s3-data-service"));

        // Given we write some test data to an object in a bucket
        String testData = "Test data";

        s3.putObject(b -> b.acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                .bucket(BUCKET_NAME)
                .key(URI.create(s3Resource.getId()).getPath().substring(1)), RequestBody.fromString(testData));

        // When we read the data back
        var inputStream = reader.readRaw(s3Resource);
        var readData = new String(inputStream.readAllBytes());

        // Then it is equal to what was written
        assertThat(readData)
                .as("Check that the data read from S3 is equal to what was written")
                .isEqualTo(testData);
    }
}
