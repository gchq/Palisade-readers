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

import uk.gov.gchq.palisade.resource.impl.FileResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

        var resource = new FileResource().id(tmpFile.getAbsolutePath()).type("string").serialisedFormat("string");

        // When
        var response = reader.read(resource);

        // Then
        final Stream<String> lines = new BufferedReader(new InputStreamReader(response)).lines();
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
