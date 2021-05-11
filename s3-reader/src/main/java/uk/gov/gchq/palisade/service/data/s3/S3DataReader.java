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
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.BucketAccess;
import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final S3Properties s3Properties;
    private final Materializer materialiser;

    //  ActorSystem system;

    public S3DataReader(final S3Properties s3Properties, final Materializer materialiser) {
        this.s3Properties = s3Properties;
        this.materialiser = materialiser;
        //?? could tests to validate, see if the bucket exists
        // at least non-null

    }

    @Override
    protected InputStream readRaw(final LeafResource resource) {
        LOGGER.info("readRaw!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOGGER.info("s3Properties.getBucketName(): " + s3Properties.getBucketName());
        LOGGER.info("resource.getId(): " + resource.getId());

        try {

            //temp -test to see if the bucket is there
            final CompletionStage<BucketAccess> existRequest =
                    S3.checkIfBucketExists(s3Properties.getBucketName(), materialiser);

            BucketAccess  bucketAccess =  existRequest.toCompletableFuture().join();

            //get the data from the bucket


            final Source<Optional<Pair<Source<ByteString, NotUsed>, ObjectMetadata>>, NotUsed>
                    sourceAndMeta = S3.download(s3Properties.getBucketName(), resource.getId());

            final Pair<Source<ByteString, NotUsed>, ObjectMetadata> dataAndMetadata =
                    sourceAndMeta
                            .runWith(Sink.head(), materialiser)
                            .toCompletableFuture()
                            .get(5, TimeUnit.SECONDS)
                            .get();

            final Source<ByteString, NotUsed> data = dataAndMetadata.first();
            final ObjectMetadata metadata = dataAndMetadata.second();

            final CompletionStage<String> resultCompletionStage =
                    data.map(ByteString::utf8String).runWith(Sink.head(), materialiser);

            String result = resultCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new RuntimeException("Something went wrong", e);
        }

        return null;
    }


}
