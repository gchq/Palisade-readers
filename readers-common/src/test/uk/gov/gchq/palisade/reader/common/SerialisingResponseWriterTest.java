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

package uk.gov.gchq.palisade.reader.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class SerialisingResponseWriterTest {

    public static final UserId USER_ID = new UserId().id("test-user-id");
    public static final User USER = new User().userId(USER_ID);
    public static final String RESOURCE_ID = "/test/resourceId";
    public static final String RESOURCE_TYPE = "uk.gov.gchq.palisade.test.TestType";
    public static final String RESOURCE_FORMAT = "avro";
    public static final String DATA_SERVICE_NAME = "test-data-service";
    public static final String RESOURCE_PARENT = "/test";
    public static final Context CONTEXT = new Context().purpose("test-purpose");

    public static final LeafResource LEAF_RESOURCE = new FileResource()
            .id(RESOURCE_ID)
            .type(RESOURCE_TYPE)
            .serialisedFormat(RESOURCE_FORMAT)
            .connectionDetail(new SimpleConnectionDetail().serviceName(DATA_SERVICE_NAME))
            .parent(new SystemResource().id(RESOURCE_PARENT));

    public static class TestPassThroughRule<T extends Serializable> implements Rule<T> {

        @Override
        public T apply(final T record, final User user, final Context context) {
            return record;
        }


        @Override
        public boolean isApplicable(final User user, final Context context) {
            return false;
        }

    }

    public static class TestApplyRule<T extends Serializable> implements Rule<T> {

        @Override
        public T apply(final T record, final User user, final Context context) {
            return record;
        }

        @Override
        public boolean isApplicable(final User user, final Context context) {
            return true;
        }

    }

    SimpleStringSerialiser stringSeraliser = new SimpleStringSerialiser();
    String testString = "line1\nline2\nline3\n";
    ByteArrayOutputStream outputStream;
    ByteArrayInputStream inputStream;
    AtomicLong recordsProcessed;
    AtomicLong recordsReturned;


    @BeforeEach
    void setUp(){
        stringSeraliser = new SimpleStringSerialiser();
        outputStream = new ByteArrayOutputStream();
        inputStream = new ByteArrayInputStream(testString.getBytes());
        recordsProcessed = new AtomicLong();
        recordsReturned = new AtomicLong();

    }


    /**
     * Test for the the writer method with a set of rules with one that requires its rule is enforced.
     * The response should be call deseralise/seralise methods and return the same data that was in the inputstream.
     * The recordsProcessed and recordsReturned will both be 3 indicating that they were 3 records processed and
     * returned in the output stream.
     *
     * @throws Exception if an error occurs during the running of the test
     */
    @Test
    void testSerialisingResponseWriterWithMixedRules() throws Exception {

        Rules<Serializable> mixOfRules = new Rules<>()
                .addRule("first", new TestPassThroughRule<>())
                .addRule("sec ond", new TestPassThroughRule<>())
                .addRule("third", new TestApplyRule<>())
                .addRule("fourth", new TestPassThroughRule<>());

        DataReaderRequest readerRequestWithMixedRules = new DataReaderRequest()
                .user(USER)
                .resource(LEAF_RESOURCE)
                .context(CONTEXT)
                .rules(mixOfRules);

        SerialisingResponseWriter  serialisingResponseWriter = new SerialisingResponseWriter(inputStream, stringSeraliser, readerRequestWithMixedRules, recordsProcessed, recordsReturned);
        serialisingResponseWriter.write(outputStream);

        assertThat(recordsProcessed.longValue()).isEqualTo((new AtomicLong(3L)).longValue());
        assertThat(recordsReturned.longValue()).isEqualTo((new AtomicLong(3L)).longValue());
        String outputString = new String(outputStream.toByteArray());
        assertThat(outputString).isEqualTo(testString);
    }

    /**
     * Test for the the writer method with a set of rules that all bypass and do not need to be applied.
     * The response should be not deseralise/seralise the data and return the same data that was in the inputstream.
     * The recordsProcessed and recordsReturned will both be -1 indicating that the rules were not used to track the number
     * of records in the stream.
     *
     * @throws Exception if an error occurs during the running of the test
     */
    @Test
    void testSerialisingResponseWriterWithBypassRules() throws Exception {

         Rules<Serializable> passThroughRules = new Rules<>()
                .addRule("first", new TestPassThroughRule<>())
                .addRule("second", new TestPassThroughRule<>())
                .addRule("third", new TestPassThroughRule<>())
                .addRule("fourth", new TestPassThroughRule<>());

        DataReaderRequest readerRequestWithPassThroughRules = new DataReaderRequest()
                .user(USER)
                .resource(LEAF_RESOURCE)
                .context(CONTEXT)
                .rules(passThroughRules);

        SerialisingResponseWriter  serialisingResponseWriter = new SerialisingResponseWriter(inputStream, stringSeraliser, readerRequestWithPassThroughRules, recordsProcessed, recordsReturned);
        serialisingResponseWriter.write(outputStream);

        assertThat(recordsProcessed.longValue()).isEqualTo((new AtomicLong(-1L)).longValue());
        assertThat(recordsReturned.longValue()).isEqualTo((new AtomicLong(-1L)).longValue());
        String outputString = new String(outputStream.toByteArray());
        assertThat(outputString).isEqualTo(testString);
    }
}