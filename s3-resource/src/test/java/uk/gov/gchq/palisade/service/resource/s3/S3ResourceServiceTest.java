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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;

import uk.gov.gchq.palisade.service.resource.stream.config.AkkaSystemConfig;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.gov.gchq.palisade.service.resource.s3.S3Initializer.localStackContainer;

@SpringBootTest(classes = {S3Configuration.class, AkkaSystemConfig.class})
@ContextConfiguration(initializers = {S3Initializer.class})
@ActiveProfiles({"s3", "testcontainers"})
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
class S3ResourceServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ResourceServiceTest.class);

    @Autowired
    S3ResourceService service;

    @TempDir
    Path tempPathDirectory;

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
    }

    @AfterAll
    void cleanup() {
        // List all of the objects in the bucket and then delete each one
        var objectListing = s3.listObjects(builder -> builder.bucket(s3Properties.getBucketName()));
        for (var os : objectListing.contents()) {
            LOGGER.info("Resource {}, is in the bucket", os.key());
            s3.deleteObject(builder -> builder.bucket(s3Properties.getBucketName()).key(os.key()));
        }

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

        assertThat(service.bucketExists().toCompletableFuture().join())
                .as("Check that the S3ResourceService knows the bucket: %s, exists", s3Properties.getBucketName())
                .isTrue();
    }

    @Test
    @Order(3)
    void testGetResourcesById() {
        // Add a test file to the bucket
        var s3ResourceId = "s3:/testFile.txt";
        s3.putObject(b -> b.bucket(s3Properties.getBucketName()).key(s3ResourceId), RequestBody.fromString("Test Body"));
        var s3Resource = ((S3Resource) ResourceBuilder.create(s3ResourceId));


        // Given an empty list
        var resultList = new ArrayList<>();

        // When getting the resource from the S3 Resource Service using the resources Id
        var resourcesById = service.getResourcesById(s3Resource.getId());
        resourcesById.forEachRemaining(resultList::add);

        Assertions.assertThat(resultList)
                .as("Check that when I get a resource by its Id, the correct resource is returned")
                .containsOnly(s3Resource);
    }

//    @Test
//    @Order(3)
//    @Disabled
//    void testV2() throws IOException {
//        // AWS SDK v2
//
//        var bucketName = s3Properties.getBucketName();
//        var lOR = ListObjectsRequest.builder().bucket(bucketName).build();
//        assertThat(s3.listObjects(lOR).contents())
//                .as("Check one object is returned")
//                .asList()
//                .hasSize(1)
//                .first()
//                .extracting("key")
//                .isEqualTo(testFile.toString());
//
//        // Getting the object using the getObjectRequest saves the file to the tempPathDirectory
//        var gOR = GetObjectRequest.builder().bucket(bucketName).key(testFile.toString()).build();
//        s3.getObject(gOR, tempPathDirectory.resolve("testFileFromS3.txt"));
//
//        // Get the file from the temp directory
//        var fileFromS3 = FileUtils.getFile(String.valueOf(tempPathDirectory), "testFileFromS3.txt");
//
//        assertThat(Files.readAllLines(fileFromS3.toPath()))
//                .as("Check that the lines are the same and the file has not been modified")
//                .isEqualTo(Files.readAllLines(testFile.toAbsolutePath()));
//
//        // Getting the object should throw an exception as it no longer exists
//        var dOR = DeleteObjectRequest.builder().bucket(bucketName).key(testFile.toString()).build();
//        s3.deleteObject(dOR);
//        assertThrows(NoSuchKeyException.class, () -> s3.getObject(gOR), "Test should throw an exception");
//    }
}
