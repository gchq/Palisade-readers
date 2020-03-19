/*
 * Copyright 2020 Crown Copyright
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

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.reader.common.DataFlavour;
import uk.gov.gchq.palisade.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.reader.util.PathUtils;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class HadoopDataReaderTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(PathUtils.getTestDir(HadoopDataReaderTest.class));

    @Test
    public void shouldReadTextFileWithNoRules() throws IOException {
        // Given
        final File tmpFile = testFolder.newFile("file1.txt");
        FileUtils.write(tmpFile, "some data\nsome more data", StandardCharsets.UTF_8);

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        final FileResource resource = new FileResource().type("string").id(tmpFile.getAbsolutePath()).serialisedFormat("string");
        final Rules<String> rules = new Rules<>();

        final DataReaderRequest request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .context(new Context())
                .rules(rules);
        request.setOriginalRequestId(new RequestId().id("test"));

        // When
        final DataReaderResponse response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();
        assertEquals(Arrays.asList("some data", "some more data"), lines.collect(Collectors.toList()));
    }

    @Test
    public void shouldReadTextFileWithRules() throws IOException {
        // Given
        final File tmpFile = testFolder.newFile("file1.txt");
        FileUtils.write(tmpFile, "some data\nsome more data", StandardCharsets.UTF_8);

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        final FileResource resource = new FileResource().id(tmpFile.getAbsolutePath()).type("string").serialisedFormat("string");
        // Redact any records containing the word 'more'
        final Rules<String> rules = new Rules<String>().predicateRule("1", (r, u, j) -> !r.contains("more"));

        final DataReaderRequest request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .context(new Context())
                .rules(rules);
        request.setOriginalRequestId(new RequestId().id("test"));

        // When
        final DataReaderResponse response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();
        assertEquals(Collections.singletonList("some data"), lines.collect(Collectors.toList()));
    }

    @Test
    public void shouldDecodeURIEncodedFilesCorrectly() throws IOException {
        // Given
        final File tmpFile = testFolder.newFile("fi le1.txt");
        FileUtils.write(tmpFile, "some data\nsome more data", StandardCharsets.UTF_8);

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        final FileResource resource = new FileResource().type("string").id(new Path(tmpFile.getAbsolutePath()).toUri().toString()).serialisedFormat("string");
        final Rules<String> rules = new Rules<>();

        final DataReaderRequest request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .context(new Context())
                .rules(rules);
        request.setOriginalRequestId(new RequestId().id("test"));

        // When
        final DataReaderResponse response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();
        assertEquals(Arrays.asList("some data", "some more data"), lines.collect(Collectors.toList()));
    }

    private static HadoopDataReader getReader(final Configuration conf) throws IOException {
        return new HadoopDataReader().conf(conf);
    }
}
