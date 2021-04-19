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

package uk.gov.gchq.palisade.service.resource.hadoop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.service.resource.common.resource.LeafResource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HadoopResourceDetailsTest {

    @BeforeEach
    public void setup() {
        HadoopResourceDetails.addTypeSupport("type", "TYPE");
    }

    @Test
    void testAcceptsSchemelessUri() throws URISyntaxException {
        // Given
        URI uri = new URI("/home/hadoop/resources/type_file.format");
        HadoopResourceDetails expected = new HadoopResourceDetails(uri, "TYPE", "format");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void testAcceptsAbsolutePath() throws URISyntaxException {
        // Given
        URI absolute = new URI("file:/home/hadoop/resources/type_file.format");
        HadoopResourceDetails expected = new HadoopResourceDetails(absolute, "TYPE", "format");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(absolute);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void testAcceptsRelativePath() throws URISyntaxException {
        // Given
        URI relative = new URI("file:./type_file.format");
        HadoopResourceDetails expected = new HadoopResourceDetails(relative, "TYPE", "format");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(relative);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void testNoTypeThrowsException() {
        // Given
        URI invalidType = new File(".").toURI().resolve("file.format");

        // When the resource is retrieved with no type
        Exception illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidType), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage()).contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testEmptyTypeThrowsException() {
        // Given
        URI invalidType = new File(".").toURI().resolve("_file.format");

        // When the resource is retrieved with an empty type
        Exception illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidType), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage()).contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testNoSerialisedFormatThrowsException() {
        // Given
        URI invalidFormat = new File(".").toURI().resolve("type_file");

        // When the resource is retrieved with no serialised format
        Exception illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidFormat), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage()).contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testEmptySerialisedFormatThrowsException() {
        // Given
        URI invalidFormat = new File(".").toURI().resolve("type_file.");

        // When the resource is retrieved with an empty serialised format
        Exception illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidFormat), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage()).contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testFormatStringIsConsistent() {
        // Given
        URI uri = new File(".").toURI().resolve(HadoopResourceDetails.FORMAT_STRING);
        HadoopResourceDetails expected = new HadoopResourceDetails(uri, "TYPE", "FORMAT");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void testDetailsReturnConsistentResource() {
        URI uri = new File(".").toURI().resolve(HadoopResourceDetails.FORMAT_STRING);
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // When
        LeafResource resource = details.getResource();

        // Then
        assertThat(resource.getId()).isEqualTo(details.getFileName().toString());
        assertThat(resource.getType()).isEqualTo(details.getType());
        assertThat(resource.getSerialisedFormat()).isEqualTo(details.getFormat());
    }

    @Test
    void testNoSupportedTypeTest() {
        // Given
        URI uri = new File(".").toURI().resolve("a_file.txt");

        // When
        Exception illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(uri), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage()).contains("Type 'a' is not supported");
    }

    @Test
    void testIsValidResourceNameUsingInvalidFileNameButValidPath() {
        // Given
        URI uri = URI.create("file:///edge_case/that-matches/if.using/fullPath");

        // When
        boolean details = HadoopResourceDetails.isValidResourceName(uri);

        assertThat(details).isFalse();
    }
}
