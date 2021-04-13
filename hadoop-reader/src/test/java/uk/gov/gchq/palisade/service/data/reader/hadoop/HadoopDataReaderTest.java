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
package uk.gov.gchq.palisade.service.data.reader.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import uk.gov.gchq.palisade.service.data.common.Context;
import uk.gov.gchq.palisade.service.data.common.data.DataFlavour;
import uk.gov.gchq.palisade.service.data.common.data.reader.DataReaderRequest;
import uk.gov.gchq.palisade.service.data.common.data.reader.DataReaderResponse;
import uk.gov.gchq.palisade.service.data.common.resource.impl.FileResource;
import uk.gov.gchq.palisade.service.data.common.rule.Rules;
import uk.gov.gchq.palisade.service.data.common.user.User;
import uk.gov.gchq.palisade.service.data.serialise.SimpleStringSerialiser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


public class HadoopDataReaderTest {
    @TempDir
    Path testFolder;

    @Test
    public void testShouldReadTextFileWithNoRules() throws IOException {
        // Given
        var tmpFile = testFolder.resolve("file1.txt");
        Files.write(tmpFile, "some data\nsome more data".getBytes(StandardCharsets.UTF_8));

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        final DataReaderRequest request = new DataReaderRequest()
                .resource(new FileResource().id(tmpFile.toString()).type("string").serialisedFormat("string"))
                .user(new User())
                .context(new Context())
                .rules(new Rules<>());

        // When
        final DataReaderResponse response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();

        assertThat(lines.collect(Collectors.toList()))
                .as("Check that the file has the correct data")
                .isEqualTo(Arrays.asList("some data", "some more data"));
    }

    @Test
    public void testShouldReadTextFileWithRules() throws IOException {
        // Given
        var tmpFile = testFolder.resolve("file2.txt");
        Files.write(tmpFile, "some data\nsome more data".getBytes(StandardCharsets.UTF_8));

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        final FileResource resource = new FileResource().id(tmpFile.toString()).type("string").serialisedFormat("string");
        // Redact any records containing the word 'more'
        final Rules<String> rules = new Rules<String>().addPredicateRule("1", (r, u, j) -> !r.contains("more"));

        final DataReaderRequest request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .context(new Context())
                .rules(rules);

        // When
        final DataReaderResponse response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();

        assertThat(lines.collect(Collectors.toList()))
                .as("Check that the file has the correct data")
                .isEqualTo(Collections.singletonList("some data"));
    }

    @Test
    public void testShouldDecodeURIEncodedFilesCorrectly() throws IOException {
        // Given
        var tmpFile = testFolder.resolve("file3.txt");
        Files.write(tmpFile, "some data\nsome more data".getBytes(StandardCharsets.UTF_8));

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        final DataReaderRequest request = new DataReaderRequest()
                .resource(new FileResource().id(tmpFile.toString()).type("string").serialisedFormat("string"))
                .user(new User())
                .context(new Context())
                .rules(new Rules<>());

        // When
        final DataReaderResponse response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();

        assertThat(lines.collect(Collectors.toList()))
                .as("Check that the file has the correct data")
                .isEqualTo(Arrays.asList("some data", "some more data"));
    }

    private static HadoopDataReader getReader(final Configuration conf) throws IOException {
        return new HadoopDataReader().conf(conf);
    }
}
