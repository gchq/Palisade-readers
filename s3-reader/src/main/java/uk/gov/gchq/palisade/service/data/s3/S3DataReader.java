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


import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.Attributes;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.ApiVersion;
import akka.stream.alpakka.s3.BucketAccess;
import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.S3Attributes;
import akka.stream.alpakka.s3.S3Ext;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.data.reader.SerialisedDataReader;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;


/**
 * An S3DataReader is an implementation of {@link SerialisedDataReader} for S3 that opens a file and returns
 * a single {@link InputStream} containing all the records for a given {@link LeafResource}.
 */
public class S3DataReader extends SerialisedDataReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3DataReader.class);

    private final S3Bucket bucket;
    private final Materializer materialiser;

    //  ActorSystem system;


    public S3DataReader(final S3Bucket bucket, final Materializer materialiser) {
        this.bucket = bucket;
        this.materialiser = materialiser;
        //?? could tests to validate, see if the bucket exists
        // at least non-null

    }

    @Override
    protected InputStream readRaw(final LeafResource resource) {
        LOGGER.info("readRaw!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // LOGGER.info( "materializer: " +materializer);
        // LOGGER.info( "bucket: " +bucket);

        BucketAccess bucketAccess = S3.checkIfBucketExists(bucket.getBucketName(), materialiser).toCompletableFuture().join();
        LOGGER.info("bucketAccess: " + bucketAccess);


        // ActorSystem actorSystem = materializer.system();
        //  Config config  = actorSystem.settings().config();
        //  LOGGER.info("actorSystem: " +actorSystem );
        //  LOGGER.info("config: " +actorSystem.settings());
        //  LOGGER.info("config: " +actorSystem.settings().config() );

        //pseudo code -copy and paste of the 2.0.2 Alpakka S3
        //config, properties -injected in via dependency injection
        //want to extract the information from the bucket for the Leaf resource
        //materializer.system().settings().config(). .getCredentialsProvider();
        //  LOGGER.info("actorSystem: " +actorSystem );

        //need to retrieve the data from S3
        //bucket will be unique to the client request.
        //will need for this to come in as part of the creation of this instance.

        //  final S3Settings settings = S3Settings
        //   final Attributes sampleAttributes = S3Attributes.settings();

        // S3Settings settings = S3Settings.create(ActorSystem)

        //  final Attributes sampleAttributes = S3Attributes.settings();

        // ActorSystem system = ActorSystem.create("TestSystem");


        //  S3Settings s3Settings = S3Ext.get(system).settings()
        //       .withCredentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("accessKey", "secretKey")));

        //        LOGGER.info("s3Settings: " +s3Settings);

        // AwsCredentialsProvider awsCredentialsProvider =S3Ext.get(system).settings().getCredentialsProvider();

        //  LOGGER.info("awsCredentialsProvider: " +awsCredentialsProvider);

        // awsCredentialsProvider.
        //S3Ext.get(system).settings("test")
/*
        try {

           //check if bucket exists
          //  final CompletionStage<BucketAccess> existRequest = S3.checkIfBucketExists(bucket.getBucketName(), materializer);
          //  existRequest.toCompletableFuture().get(5, TimeUnit.SECONDS);


         //   LOGGER.info("awsCredentialsProvider: " +awsCredentialsProvider);



//get the data from the bucket

            LOGGER.info("before : "  );

            CompletionStage<Boolean> bucketBol  = S3.checkIfBucketExists(bucket.getBucketName(), materializer)
                    .thenApply(bucketAccess -> bucketAccess.equals(BucketAccess.accessGranted()));
            LOGGER.info( "bucketBol: " +bucketBol.toCompletableFuture().join());

            Source<Optional<ObjectMetadata>, NotUsed> source =
                    S3.getObjectMetadata(bucket.getBucketName(), bucket.getBucketKey());
            LOGGER.info( "source: " +source);

            final Source<Optional<Pair<Source<ByteString, NotUsed>, ObjectMetadata>>, NotUsed>
                    sourceAndMeta = S3.download(bucket.getBucketName(), bucket.getBucketKey());

            final Pair<Source<ByteString, NotUsed>, ObjectMetadata> dataAndMetadata =
                    sourceAndMeta
                            .runWith(Sink.head(), materializer)
                            .toCompletableFuture()
                            .get(5, TimeUnit.SECONDS)
                            .get();

            final Source<ByteString, NotUsed> data = dataAndMetadata.first();
            final ObjectMetadata metadata = dataAndMetadata.second();

            final CompletionStage<String> resultCompletionStage =
                    data.map(ByteString::utf8String).runWith(Sink.head(), materializer);

            String result = resultCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);



        } catch (Exception e) {
            throw new RuntimeException("Something went wrong", e);
        }
*/
        return null;
    }


}
