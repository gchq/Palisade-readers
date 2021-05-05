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
package uk.gov.gchq.palisade.demo;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
class LocalstackS3JUnit5Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalstackS3JUnit5Test.class);

    @TempDir
    Path tempPathDirectory;

    @Container
    private final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.9.1"))
            .withServices(S3);

    @Test
    @Order(1)
    public void testLocalstackIsRunning() {
        LOGGER.debug("!!!!!!!!!!Starting the test");
        assertThat(localstack.isRunning())
                .as("Check that localstack is running")
                .isTrue();
    }

    @Test
    @Order(2)
    void testDemoS3Bucket() throws IOException {
        // AWS SDK v2
        var s3 = S3Client
                .builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .region(Region.of(localstack.getRegion()))
                .build();

        // Create a test file and add some text to it
        var testFile = tempPathDirectory.resolve("testFile.txt");
        var lines = Arrays.asList("1", "2", "3");
        Files.write(testFile, lines);

        // Create the bucket, and ad the object
        s3.createBucket(b -> b.bucket("foo"));
        s3.putObject(b -> b.bucket("foo").key(testFile.toString()), testFile);

        assertThat(s3.listBuckets().buckets())
                .as("Check one bucket exists")
                .asList()
                .hasSize(1)
                .first()
                .extracting("name")
                .isEqualTo("foo");

        var lOR = ListObjectsRequest.builder().bucket("foo").build();
        assertThat(s3.listObjects(lOR).contents())
                .as("Check one object is returned")
                .asList()
                .hasSize(1)
                .first()
                .extracting("key")
                .isEqualTo(testFile.toString());

        // Getting the object using the getObjectRequest saves the file to the tempPathDirectory
        var gOR = GetObjectRequest.builder().bucket("foo").key(testFile.toString()).build();
        s3.getObject(gOR, tempPathDirectory.resolve("testFileFromS3.txt"));

        // Get the file from the temp directory
        var fileFromS3 = FileUtils.getFile(String.valueOf(tempPathDirectory), "testFileFromS3.txt");

        assertThat(Files.readAllLines(fileFromS3.toPath()))
                .as("Check that the lines are the same and the file has not been modified")
                .isEqualTo(Files.readAllLines(testFile.toAbsolutePath()));

        // Getting the object should throw an exception as it no longer exists
        var dOR = DeleteObjectRequest.builder().bucket("foo").key(testFile.toString()).build();
        s3.deleteObject(dOR);
        assertThrows(NoSuchKeyException.class, () -> s3.getObject(gOR), "Bucket is deleted, so getting the object should throw an exception");
    }


}
