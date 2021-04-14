package uk.gov.gchq.palisade.reader;

//import org.apache.commons.math3.stat.inference.TestUtils;
//import org.junit.Rule;
//import org.junit.jupiter.api.Test;

//import org.testcontainers.utility.DockerImageName;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.regions.Region;

//import javax.swing.*;
//import java.util.UUID;

//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import com.amazonaws.services.s3.model.S3Object;

//import org.testcontainers.utility.DockerImageName;

//import cloud.localstack.awssdkv1.TestUtils;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class S3DataReaderTest {

    //used pip install localstack at the command prompt to load localstack
    // then used localstack start to get it up and running
    // could have use a docker-compose up command with a docker-compose.yaml to do this
    //https://dzone.com/articles/useful-tools-for-local-development-with-aws-servic
    //Internal to a test this can be done with the  code that creates a container
   DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:1.15.2");


   @Container
    private  final LocalStackContainer localStackContainer = new LocalStackContainer(localstackImage)
           .withServices(S3);



    /**
     * Returns an InputStream with expected content
     */

    @Test
    public void readRawWithValidId(){


        // AWS SDK v2
        S3Client s3 = S3Client
                .builder()
                .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        localStackContainer.getAccessKey(), localStackContainer.getSecretKey()
                )))
                .region(Region.of(localStackContainer.getRegion()))
                .build();

        s3.createBucket(b -> b.bucket("foo"));
        s3.putObject(b -> b.bucket("foo").key("bar"), RequestBody.fromBytes("baz".getBytes()));



    }



}
