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


import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.data.s3.config.AkkaSystemConfig;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.gov.gchq.palisade.service.data.s3.S3Initializer.localStackContainer;

@SpringBootTest(classes = {S3Configuration.class, AkkaSystemConfig.class})
@ContextConfiguration(initializers = {S3Initializer.class})
@ActiveProfiles({"s3", "testcontainers"})
class S3DataReaderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3DataReaderTest.class);

    @Autowired
    private S3Properties s3Properties;

    @Autowired
    private ActorSystem akkaActorSystem;

    @Autowired
    private Materializer akkaMaterialiser;
    private String bucketName = "testbucketname";

    private static String resourceId = "/test/resourceId";

    private static LeafResource leafResource;


    @BeforeAll
    static void setup() {

        var resourceType = "uk.gov.gchq.palisade.test.TestType";
        var resourceFormat = "avro";
        var dataServiceName = "test-data-service";
        var resourceParent = "/test";
        leafResource = new FileResource()
                .id(resourceId)
                .type(resourceType)
                .serialisedFormat(resourceFormat)
                .connectionDetail(new SimpleConnectionDetail().serviceName(dataServiceName))
                .parent(new SystemResource().id(resourceParent));



    }


    @Test
    void testContextLoads() {

        assertThat(localStackContainer)
                .as("Check that the LocalStackContainer has been started successfully")
                .isNotNull();

        assertThat(akkaActorSystem)
                .as("Check that the ActorSystem bean has been created successfully")
                .isNotNull();

        assertThat(s3Properties)
                .as("Check that the s3Properties has been created successfully")
                .isNotNull();

    }


    @Test
    public void testReadingS3Bucket() {

        //gotta come up wit a better way
        s3Properties.setBucketName(bucketName);


        //using AWS and LocalStackContainer create and populate the S3 bucket

        S3Client s3Client = S3Client
                .builder()
                .endpointOverride(localStackContainer.getEndpointOverride(S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .region(Region.of(localStackContainer.getRegion()))
                .build();

        s3Client.createBucket(b -> b.bucket(bucketName));
        s3Client.putObject(b -> b.bucket(bucketName).key(resourceId), RequestBody.fromBytes("Now is the time for all good men to come to aide of their country".getBytes()));

        // use the S3DataReader to retrieve the data
        // under the hood it is using Alpakka connector for S3 to retrieve the data

        S3DataReader dataReader = new S3DataReader(s3Properties, akkaMaterialiser);
        InputStream stream = dataReader.readRaw(leafResource);


    }


}