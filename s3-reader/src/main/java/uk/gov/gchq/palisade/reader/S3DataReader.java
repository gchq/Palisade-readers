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

package uk.gov.gchq.palisade.reader;

import akka.NotUsed;
import akka.actor.ActorSystem;
//import akka.japi.Pair;
//import akka.stream.Materializer;
//import akka.stream.alpakka.s3.ObjectMetadata;
//import akka.stream.alpakka.s3.javadsl.S3;
//import akka.stream.javadsl.Sink;
//import akka.stream.javadsl.Source;
//import akka.util.ByteString;
//import com.typesafe.config.Config;
//import com.typesafe.config.ConfigFactory;
import uk.gov.gchq.palisade.reader.common.SerialisedDataReader;
import uk.gov.gchq.palisade.reader.common.resource.LeafResource;
//import uk.gov.gchq.palisade.reader.exception.ReadResourceException;

import java.io.InputStream;
//import java.util.Optional;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;

/**
 * An S3DataReader is an implementation of {@link SerialisedDataReader} for S3 that opens a file and returns
 * a single {@link InputStream} containing all the records for a given {@link LeafResource}.
 */
public class S3DataReader extends SerialisedDataReader {


    @Override
    protected InputStream readRaw(final LeafResource resource) {

        //pseudo code

        //need to retrieve the data from S3
        //bucketURL will be unique to the client request.
        //will need for this to come in as part of the creation of this instance.
      final Source<Optional<Pair<Source<ByteString, NotUsed>, ObjectMetadata>>, NotUsed> sourceAndMeta = S3.download("bucketURL", resource.getId());
        //

       //config, properties -injected in via dependency injection

       ActorSystem system = ActorSystem.create();
      //?  Materializer materializer = Materializer.createMaterializer(system);
       // Materializer materializer = null;
       /* try {
            final Pair<Source<ByteString, NotUsed>, ObjectMetadata> dataAndMetadata =
                    sourceAndMeta
                            .runWith(Sink.head(), materializer)
                            .toCompletableFuture()
                            .get(5, TimeUnit.SECONDS)
                            .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            //will want to move this to common
            throw new ReadResourceException("Error reading S3 resource", e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new ReadResourceException("Error reading S3 resource", e);

        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new ReadResourceException("Error reading S3 resource", e);

        }

        */

        //if it is empty, then what

        return null;
    }
}
