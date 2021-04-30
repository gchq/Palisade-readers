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
import akka.stream.SystemMaterializer;
import akka.stream.alpakka.s3.S3Ext;
import akka.stream.alpakka.s3.S3Settings;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.resource.impl.SystemResource;

import java.io.InputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
@SpringBootTest(
        properties = {"aws.accessKeyId=accesskey", "aws.secretAccessKey=secretkey"}
)
@ActiveProfiles({"alpakka-test"})
class S3DataReaderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3DataReaderTest.class);

    @Container
    private static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.10"))
            .withServices(S3);


    @Test
    void contextLoads() {
        LOGGER.debug("Spring Application Context is loading");
    }


    @Test
    public void testCreateS3Bucket() {
        LOGGER.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );

        String TEST_BUCKET_NAME = "testbucketname";
        String TEST_BUCKET_KEY = "testbucketkey";

        S3Bucket s3Bucket = new S3Bucket(TEST_BUCKET_NAME, TEST_BUCKET_KEY);

        String RESOURCE_ID = "/test/resourceId";
        String RESOURCE_TYPE = "uk.gov.gchq.palisade.test.TestType";
        String RESOURCE_FORMAT = "avro";
        String DATA_SERVICE_NAME = "test-data-service";
        String RESOURCE_PARENT = "/test";
        LeafResource LEAF_RESOURCE = new FileResource()
                .id(RESOURCE_ID)
                .type(RESOURCE_TYPE)
                .serialisedFormat(RESOURCE_FORMAT)
                .connectionDetail(new SimpleConnectionDetail().serviceName(DATA_SERVICE_NAME))
                .parent(new SystemResource().id(RESOURCE_PARENT));


        //using the Localstack in Testcontainers to set-up the test environment
        String accessKey = localstack.getAccessKey();
        String secretKey = localstack.getSecretKey();
        String region = localstack.getRegion();
        URI endpointOverride =localstack.getEndpointOverride(LocalStackContainer.Service.S3);

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);

        LOGGER.info("accessKey: " +accessKey);
        LOGGER.info("secretKey: " +secretKey);
        LOGGER.info("region: " +region);
        LOGGER.info("endpointOverride: " +endpointOverride.toString());
        LOGGER.info("awsBasicCredentials: " +awsBasicCredentials.toString());
        LOGGER.info("staticCredentialsProvider: " +staticCredentialsProvider);

        S3Client s3Client = S3Client
                .builder()
                .endpointOverride(endpointOverride)
                .credentialsProvider(staticCredentialsProvider)
                .region(Region.of(region))
                .build();

        s3Client.createBucket(b -> b.bucket(TEST_BUCKET_NAME));

        s3Client.putObject(b -> b.bucket(TEST_BUCKET_NAME).key(TEST_BUCKET_KEY), RequestBody.fromBytes("Now is the time for all good men to come to aide of their country".getBytes()));



        //using Alpakka connector for S3 to retrieve the data
        LOGGER.info("\n\n\n!!!!!!!!!!!!!! Alpakkaa ");


        ActorSystem actor = ActorSystem.create("S3Tester");

        Materializer materializer = SystemMaterializer.get(actor).materializer();

        AwsRegionProvider awsRegionProvider = new AwsRegionProvider(){
            Region region = Region.EU_CENTRAL_1;
            @Override
            public Region getRegion() {
                return region;
            }
        };

        S3Settings s3Settings = S3Ext.get(actor).settings()
                .withCredentialsProvider(staticCredentialsProvider)
                .withS3RegionProvider(awsRegionProvider);



        S3DataReader dataReader = new S3DataReader(s3Bucket, materializer);

        LOGGER.info("actor: " +actor);
        LOGGER.info("materializer: " +materializer);
        LOGGER.info("dataReader: " +dataReader);


        InputStream stream = dataReader.readRaw(LEAF_RESOURCE);

        String outputString = stream.toString();
        assertThat(outputString)
                .as("Check that read will provide data in the output stream")
                .isEqualTo("set this later");



    }


}