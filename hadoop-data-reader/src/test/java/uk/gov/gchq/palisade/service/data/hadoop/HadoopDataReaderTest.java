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
package uk.gov.gchq.palisade.service.data.hadoop;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.data.model.DataReaderRequest;
import uk.gov.gchq.palisade.service.data.model.DataReaderResponse;
import uk.gov.gchq.palisade.service.data.reader.DataFlavour;
import uk.gov.gchq.palisade.user.User;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HadoopDataReaderTest {

    @TempDir
    Path testFolder;

    @Test
    void testReadTextFileWithNoRules() throws IOException {
        // Given
        final File tmpFile = Files.createFile(testFolder.resolve("file1.txt")).toFile();
        FileUtils.write(tmpFile, "some data\nsome more data", StandardCharsets.UTF_8);

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        var resource = new FileResource().id(tmpFile.getAbsolutePath()).type("string").serialisedFormat("string");

        var request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .context(new Context())
                .rules(new Rules<>());

        // When
        var response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();
        assertThat(lines.collect(Collectors.toList()))
                .as("Check the expected values are returned")
                .asList()
                .containsOnly("some data", "some more data");
    }

    @Test
    void testReadTextFileWithRules() throws IOException {
        // Given
        final File tmpFile = Files.createFile(testFolder.resolve("file1.txt")).toFile();
        FileUtils.write(tmpFile, "some data\nsome more data", StandardCharsets.UTF_8);

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        final FileResource resource = new FileResource().id(tmpFile.getAbsolutePath()).type("string").serialisedFormat("string");
        // Redact any records containing the word 'more'
        var rules = new Rules<String>().addRule("1", (PredicateRule<String>) (r, u, j) -> !r.contains("more"));

        var request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .context(new Context())
                .rules(rules);

        // When
        final DataReaderResponse response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        var os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();
        assertThat(lines.collect(Collectors.toList()))
                .as("Check the expected values are returned")
                .asList()
                .containsOnly("some data");
    }

    @Test
    void testDecodeURIEncodedFilesCorrectly() throws IOException {
        // Given
        final File tmpFile = Files.createFile(testFolder.resolve("fi le1.txt")).toFile();
        FileUtils.write(tmpFile, "some data\nsome more data", StandardCharsets.UTF_8);

        final Configuration conf = new Configuration();
        final HadoopDataReader reader = getReader(conf);
        reader.addSerialiser(DataFlavour.of("string", "string"), new SimpleStringSerialiser());

        var resource = new FileResource().id(tmpFile.getAbsolutePath()).type("string").serialisedFormat("string");

        var request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .context(new Context())
                .rules(new Rules<>());

        // When
        var response = reader.read(request, new AtomicLong(0), new AtomicLong(0));

        // Then
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.getWriter().write(os);
        final Stream<String> lines = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray()))).lines();
        assertThat(lines.collect(Collectors.toList()))
                .as("Check the expected values are returned")
                .asList()
                .containsOnly("some data", "some more data");
    }

    @Test
    void testGetConfigMap() throws IOException {
        var dataReader = new HadoopDataReader();
        assertThat(dataReader.getConfMap())
                .as("Check the returned configuration is empty")
                .isEmpty();
    }

    private static HadoopDataReader getReader(final Configuration conf) throws IOException {
        return new HadoopDataReader().conf(conf);
    }
}
