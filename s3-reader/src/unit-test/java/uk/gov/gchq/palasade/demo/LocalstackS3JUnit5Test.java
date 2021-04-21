package uk.gov.gchq.palasade.demo;


import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
//import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
//import software.amazon.auth.credentials.AwsCredentials;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

//import java.io.IOException;
//import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

/**
 * Tinkers with the test framework to better understand how it works.
 */
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = {ServiceName.S3})
@Testcontainers
class LocalstackS3JUnit5Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalstackS3JUnit5Test.class);

    @Container
    private static LocalStackContainer localstack;//  = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))

       static {
           localstack  = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.8"), false)
                   .withServices(S3);

       }

/*
    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localstack  = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
                .withServices(S3);
    }
*/

    @AfterAll
   static  public void afterAll(){

        localstack.stop();
        LOGGER.debug("!!!!!!!!!!!! after stop");
    }

    @Test
    public void basicLocalStackTest() {

        LOGGER.debug("!!!!!!!!!!Starting the test");
        //?????localstack.start();
        assertThat(localstack.isRunning());
    }

    @Test
    public void testCreateS3Bucket() {

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

        s3Client.putObject(b -> b.bucket("bucketname").key("bucketkey"), RequestBody.fromBytes("now is the time for all good men to come to ".getBytes()));
    }



}
