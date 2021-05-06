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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
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

import uk.gov.gchq.palisade.service.resource.stream.config.AkkaSystemConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.gov.gchq.palisade.service.resource.s3.S3Initializer.lsc;

@SpringBootTest(classes = {S3Configuration.class, AkkaSystemConfig.class})
@ContextConfiguration(initializers = {S3Initializer.class})
@ActiveProfiles({"s3", "testcontainers"})
class S3ResourceServiceTest {

    @Autowired
    S3ResourceService service;

    @TempDir
    Path tempPathDirectory;

    private Path testFile;

    @Test
    @Order(1)
    void testAutowiring() {
        assertThat(lsc)
                .as("Check that the service has been started successfully")
                .isNotNull();
    }

    @Test
    @Order(2)
    void testV2() throws IOException {
        // AWS SDK v2
        var s3 = S3Client
                .builder()
                .endpointOverride(lsc.getEndpointOverride(S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(lsc.getAccessKey(), lsc.getSecretKey())))
                .build();

        // Create a test file and add some text to it
        testFile = tempPathDirectory.resolve("testFile.txt");
        var lines = Arrays.asList("1", "2", "3");
        Files.write(testFile, lines);

        // Create the bucket, and add the object
        s3.createBucket(b -> b.bucket("foo"));
        s3.putObject(b -> b.bucket("foo").key(testFile.toString()), testFile);

        assertThat(s3.listBuckets().buckets())
                .as("Check one bucket exists")
                .asList()
                .hasSize(1)
                .first()
                .extracting("name")
                .isEqualTo("foo");


        var x = service.getResourcesById(testFile.toString());
        assertThat(x.next())
                .as("Check the iterator has an item in")
                .isNotNull();

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
        assertThrows(NoSuchKeyException.class, () -> s3.getObject(gOR), "Test should throw an exception");

        // Finally delete the object
        s3.deleteBucket(DeleteBucketRequest.builder().bucket("foo").build());
    }
}
