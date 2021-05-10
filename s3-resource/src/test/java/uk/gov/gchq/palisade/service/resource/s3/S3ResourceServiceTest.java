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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import uk.gov.gchq.palisade.resource.ConnectionDetail;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.stream.config.AkkaSystemConfig;
import uk.gov.gchq.palisade.util.FileResourceBuilder;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.gov.gchq.palisade.service.resource.s3.S3Initializer.localStackContainer;

@SpringBootTest(classes = {S3Configuration.class, AkkaSystemConfig.class})
@ContextConfiguration(initializers = {S3Initializer.class})
@ActiveProfiles({"s3", "testcontainers"})
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
class S3ResourceServiceTest {

    private static final String FORMAT_VALUE = "txt";
    private static final String TYPE_VALUE = "bob";
    private static final String TYPE_CLASSNAME = "com.type.bob";


    @Autowired
    S3ResourceService service;

    @TempDir
    Path tempPathDirectory;

    private Path testFile;
    private LeafResource resource1;

    @Autowired
    S3Properties s3Properties;

    private S3Client s3;

    @BeforeEach
    void setup() throws IOException {
        s3 = S3Client
                .builder()
                .endpointOverride(localStackContainer.getEndpointOverride(S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .build();

        // Create a test file
        testFile = tempPathDirectory.resolve("testFile.txt");
        Files.write(testFile, Arrays.asList("1", "2", "3"));

        // Add the file to the bucket
        s3.putObject(b -> b.bucket(s3Properties.getBucketName()).key(testFile.toString()), testFile);

        ConnectionDetail connectionDetail = new SimpleConnectionDetail().serviceName("s3-data-service");
        resource1 = ((LeafResource) ResourceBuilder.create("s3:/testFile.txt"))
                .type(TYPE_CLASSNAME)
                .serialisedFormat(FORMAT_VALUE)
                .connectionDetail(connectionDetail);
    }

    @AfterAll
    void cleanup() {
        // Finally delete the bucket
        s3.deleteBucket(DeleteBucketRequest.builder().bucket(s3Properties.getBucketName()).build());
    }

    @Test
    @Order(1)
    void testAutowiring() {
        assertThat(service)
                .as("Check that the service has been started successfully")
                .isNotNull();

        assertThat(localStackContainer)
                .as("Check that the localstack container has been started successfully")
                .isNotNull();
    }

    @Test
    @Order(2)
    void testBucketExits() {
        assertThat(s3.listBuckets().buckets())
                .as("Check one the bucket has been created in the Initializer")
                .asList()
                .hasSize(1)
                .first()
                .extracting("name")
                .isEqualTo(s3Properties.getBucketName());
    }

    @Test
    @Order(3)
    void testGetResourcesById() {
        // Given an empty list
        var resultList = new ArrayList<>();

        // When making a get request to the resource service by resourceId
        var resourcesById = service.getResourcesById(resource1.getId());
        resourcesById.forEachRemaining(resultList::add);

        // Then assert that the expected resource(s) are returned
        Assertions.assertThat(resultList)
                .as("Check that when I get a resource by its Id, the correct resource is returned")
                .containsOnly(resource1);
    }

    @Test
    @Order(3)
    void testV2() throws IOException {
        // AWS SDK v2

        var bucketName = s3Properties.getBucketName();
        var lOR = ListObjectsRequest.builder().bucket(bucketName).build();
        assertThat(s3.listObjects(lOR).contents())
                .as("Check one object is returned")
                .asList()
                .hasSize(1)
                .first()
                .extracting("key")
                .isEqualTo(testFile.toString());

        // Getting the object using the getObjectRequest saves the file to the tempPathDirectory
        var gOR = GetObjectRequest.builder().bucket(bucketName).key(testFile.toString()).build();
        s3.getObject(gOR, tempPathDirectory.resolve("testFileFromS3.txt"));

        // Get the file from the temp directory
        var fileFromS3 = FileUtils.getFile(String.valueOf(tempPathDirectory), "testFileFromS3.txt");

        assertThat(Files.readAllLines(fileFromS3.toPath()))
                .as("Check that the lines are the same and the file has not been modified")
                .isEqualTo(Files.readAllLines(testFile.toAbsolutePath()));

        // Getting the object should throw an exception as it no longer exists
        var dOR = DeleteObjectRequest.builder().bucket(bucketName).key(testFile.toString()).build();
        s3.deleteObject(dOR);
        assertThrows(NoSuchKeyException.class, () -> s3.getObject(gOR), "Test should throw an exception");
    }
}
