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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.data.s3.config.AkkaSystemConfig;

import static org.assertj.core.api.Assertions.assertThat;
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



    @Test
    void testContextLoads() {
        assertThat(localStackContainer)
                .as("Check that the service has been started successfully")
                .isNotNull();
        //bean
    }


    @Test
    public void testCreateS3Bucket() {

        var bucketName = "testBucketName";

        var resourceId = "/test/resourceId";
        var resourceType = "uk.gov.gchq.palisade.test.TestType";
        var resourceFormat = "avro";
        var dataServiceName = "test-data-service";
        var resourceParent = "/test";
        var leafResource = new FileResource()
                .id(resourceId)
                .type(resourceType)
                .serialisedFormat(resourceFormat)
                .connectionDetail(new SimpleConnectionDetail().serviceName(dataServiceName))
                .parent(new SystemResource().id(resourceParent));


        //use AWS and LocalStack to create and populate the test bucket
        var endpointOverride = localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3);
        var awsBasicCredentials = AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey());
        var staticCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
      /*
        var s3Client = S3Client
                .builder()
                .endpointOverride(endpointOverride)
                .credentialsProvider(staticCredentialsProvider)
                .region(Region.of(localStackContainer.getRegion()))
                .build();


       */

        /*
            s3Client.createBucket(b -> b.bucket(bucketName));
            s3Client.putObject(b -> b.bucket(bucketName).key(resourceId), RequestBody.fromBytes("Now is the time for all good men to come to aide of their country".getBytes()));
            LOGGER.info("finished creating bucket: ");
            LOGGER.info("s3Client.listBuckets(): " + s3Client.listBuckets());

         */
/*
        } catch (Throwable e) {
            LOGGER.info("Error occurred :", e);
        }
*/
        // akkaMaterializer.system().settings().config()

        //  BucketAccess bucketAccess = akka.stream.alpakka.s3.javadsl.S3.checkIfBucketExists(s3Bucket.getBucketName(), akkaMaterializer).toCompletableFuture().join();
        //  LOGGER.info("bucketAccess: " +bucketAccess);

        // using the S3DataReader to retrieve the data
        // under the hood it is using Alpakka connector for S3 to retrieve the data
        //  LOGGER.info("\n\n\n!!!!!!!!!!!!!! Alpakka ");

        //  S3DataReader dataReader = new S3DataReader(s3Bucket, akkaMaterializer);
        //  InputStream stream = dataReader.readRaw(LEAF_RESOURCE);

//        assertThat(stream.toString())
//                .as("Check that read will provide data in the output stream")
//                .isEqualTo("set this later");


        //  BasicAWSCredentials awsCreds = new BasicAWSCredentials("access_key_id", "secret_key_id");
        //  AwsCredentialsProvider credentialsProvider  = S3Ext.get(akkaActorSystem).settings().getCredentialsProvider();
        //  LOGGER.info("credentialsProvider: " +credentialsProvider);
        //  AwsCredentialsProvider credentialsProvider  = S3Ext.get(akkaActorSystem).settings().withCredentialsProvider();

        //  ActorSystem actor = ActorSystem.create("S3Tester");
        //  Materializer materializer = SystemMaterializer.get(actor).materializer();
        //  S3DataReader dataReader = new S3DataReader(s3Bucket, akkaMaterializer);

        //  InputStream stream = dataReader.readRaw(LEAF_RESOURCE);

//        AwsRegionProvider awsRegionProvider = new AwsRegionProvider(){
//            Region region = Region.EU_CENTRAL_1;
//            @Override
//            public Region getRegion() {
//                return region;
//            }
//        };

        //    S3Settings s3Settings = S3Ext.get(actor).settings()
        //            .withCredentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));


        //  S3DataReader dataReader = new S3DataReader(s3Bucket, materializer);

        //  LOGGER.info("actor: " + actor);
        //  LOGGER.info("materializer: " + materializer);
        //  LOGGER.info("dataReader: " + dataReader);


        //  InputStream stream = dataReader.readRaw(LEAF_RESOURCE);

    }
}