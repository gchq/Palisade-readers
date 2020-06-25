/*
 * Copyright 2018 Crown Copyright
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

package uk.gov.gchq.palisade.reader.common;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.rule.Rules;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * The response writer for the {@link SerialisedDataReader} which will apply the record level rules for Palisade.
 *
 * @param <T>   the type of {@link SerialisingResponseWriter}
 */
public class SerialisingResponseWriter<T> implements ResponseWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialisingResponseWriter.class);
    /**
     * The underlying data stream from the underlying data store.
     */
    private final InputStream stream;

    /**
     * Atomic flag to prevent double reading of the data.
     */
    private final AtomicBoolean written = new AtomicBoolean(false);
    /**
     * The serialiser for processing the input stream.
     */
    private final Serialiser<T> serialiser;
    /**
     * The user data request.
     */
    private final DataReaderRequest request;
    /**
     * Atomic counter to know the number of records that have been processed
     */
    private final AtomicLong recordsProcessed;
    /**
     * Atomic counter to know the number of records that have been returned
     */
    private final AtomicLong recordsReturned;

    /**
     * Create a serialising response writer instance.
     *
     * @param stream           the underlying data stream
     * @param serialiser       the serialiser for the request
     * @param request          the context for the request
     * @param recordsProcessed a counter for the number of records being processed
     * @param recordsReturned  a counter for the number of records being returned
     */
    public SerialisingResponseWriter(final InputStream stream, final Serialiser<T> serialiser, final DataReaderRequest request, final AtomicLong recordsProcessed, final AtomicLong recordsReturned) {
        requireNonNull(stream, "stream");
        requireNonNull(serialiser, "serialiser");
        requireNonNull(request, "request");
        this.stream = stream;
        this.serialiser = serialiser;
        this.request = request;
        this.recordsProcessed = recordsProcessed;
        this.recordsReturned = recordsReturned;
    }

    @Override
    public ResponseWriter write(final OutputStream output) throws IOException {
        requireNonNull(output, "output");

        //atomically get the previous value and set it to true
        boolean previousValue = written.getAndSet(true);

        if (previousValue) {
            throw new IOException("response already written");
        }

        final Rules<T> rules = request.getRules();

        //if nothing to do, then just copy the bytes across
        try {
            if (isNull(rules) || isNull(rules.getRules()) || rules.getRules().isEmpty()) {
                LOGGER.debug("No rules to apply");
                IOUtils.copy(stream, output);
            } else {
                LOGGER.debug("Applying rules: {}", rules);
                LOGGER.debug("Using serialiser {}", serialiser.getClass());
                Stream<T> deserialisedStream = serialiser.deserialise(stream);
                //create stream of filtered objects
                final Stream<T> deserialisedData = Util.applyRulesToStream(
                        deserialisedStream,
                        request.getUser(),
                        request.getContext(),
                        rules,
                        recordsProcessed,
                        recordsReturned
                );
                //write this stream to the output
                serialiser.serialise(deserialisedData, output);
            }
            return this;
        } finally {
            this.close();
        }
    }

    @Override
    public void close() {
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.debug("Error closing stream", e);
        }
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", SerialisingResponseWriter.class.getSimpleName() + "[", "]")
                .add("stream=" + stream)
                .add("written=" + written)
                .add("serialiser=" + serialiser)
                .add("request=" + request)
                .add("recordsProcessed=" + recordsProcessed)
                .add("recordsReturned=" + recordsReturned)
                .toString();
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SerialisingResponseWriter)) {
            return false;
        }
        final SerialisingResponseWriter<?> that = (SerialisingResponseWriter<?>) o;
        return Objects.equals(stream, that.stream) &&
                Objects.equals(written, that.written) &&
                Objects.equals(serialiser, that.serialiser) &&
                Objects.equals(request, that.request) &&
                Objects.equals(recordsProcessed, that.recordsProcessed) &&
                Objects.equals(recordsReturned, that.recordsReturned);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(stream, written, serialiser, request, recordsProcessed, recordsReturned);
    }

}
