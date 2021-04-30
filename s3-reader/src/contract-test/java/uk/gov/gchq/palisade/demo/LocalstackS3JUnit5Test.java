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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;


/**
 * Tinkering with the test framework to better understand how it works.
 */
@Testcontainers
class LocalstackS3JUnit5Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalstackS3JUnit5Test.class);

    @Container
    private static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(S3);

    /*
    @BeforeAll
    static void beforeEach() throws IOException, InterruptedException {
        LOGGER.debug("!!!!!!!!!!!!  start ");
        localstack.start();
    }

    @AfterAll
    static void afterEach() {
        localstack.stop();
        localstack.close();
        LOGGER.debug("!!!!!!!!!!!! after stop");
    }

     */


    @Test
    public void basicLocalStackTest() {

         LOGGER.debug("!!!!!!!!!!Starting the test");
         assertThat(localstack.isRunning());
    }


    @Test
    public void testDemoS3Bucket() {

        LOGGER.debug("!!!!!!!!!!Starting the testCreateBucket");
        S3Client s3Client = S3Client
                .builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        localstack.getAccessKey(), localstack.getSecretKey()
                )))
                .region(Region.of(localstack.getRegion()))
                .build();

        s3Client.createBucket(b -> b.bucket("bucketname"));

        s3Client.putObject(b -> b.bucket("bucketname").key("bucketkey"), RequestBody.fromBytes("Now is the time for all good men to come to aide of their country".getBytes()));

        assertTrue("bucket was created", s3Client.listBuckets().buckets().stream().anyMatch(b -> b.name().equals("bucketname")));

    //s3Client.getObjectAsBytes(); ??? get text



    }


}
