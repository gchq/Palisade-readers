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

package uk.gov.gchq.palisade.service.resource.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        var uri = new URI("/home/hadoop/resources/type_file.format");
        var expected = new HadoopResourceDetails(uri, "TYPE", "format");

        // When
        var details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // Then
        assertThat(details)
                .as("Check that the hadoop resource detail has been populated successfully")
                .isEqualTo(expected);
    }

    @Test
    void testAcceptsAbsolutePath() throws URISyntaxException {
        // Given
        var absolute = new URI("file:/home/hadoop/resources/type_file.format");
        var expected = new HadoopResourceDetails(absolute, "TYPE", "format");

        // When
        var details = HadoopResourceDetails.getResourceDetailsFromFileName(absolute);

        // Then
        assertThat(details)
                .as("Check that the hadoopResourceDetail has been populated successfully when getting from an absolute URL")
                .isEqualTo(expected);
    }

    @Test
    void testAcceptsRelativePath() throws URISyntaxException {
        // Given
        var relative = new URI("file:./type_file.format");
        var expected = new HadoopResourceDetails(relative, "TYPE", "format");

        // When
        var details = HadoopResourceDetails.getResourceDetailsFromFileName(relative);

        // Then
        assertThat(details)
                .as("Check that the hadoopResourceDetail has been populated successfully when getting from an relative URL")
                .isEqualTo(expected);
    }

    @Test
    void testNoTypeThrowsException() {
        // Given
        var invalidType = new File(".").toURI().resolve("file.format");

        // When the resource is retrieved with no type
        var illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidType), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage())
                .as("Check that the error message contains the correct error")
                .contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testEmptyTypeThrowsException() {
        // Given
        var invalidType = new File(".").toURI().resolve("_file.format");

        // When the resource is retrieved with an empty type
        var illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidType), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage())
                .as("Check that the error message contains the correct error")
                .contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testNoSerialisedFormatThrowsException() {
        // Given
        var invalidFormat = new File(".").toURI().resolve("type_file");

        // When the resource is retrieved with no serialised format
        var illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidFormat), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage())
                .as("Check that the error message contains the correct error")
                .contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testEmptySerialisedFormatThrowsException() {
        // Given
        var invalidFormat = new File(".").toURI().resolve("type_file.");

        // When the resource is retrieved with an empty serialised format
        var illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidFormat), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage())
                .as("Check that the error message contains the correct error")
                .contains("Filename doesn't comply with TYPE_FILENAME.FORMAT");
    }

    @Test
    void testFormatStringIsConsistent() {
        // Given
        var uri = new File(".").toURI().resolve(HadoopResourceDetails.FORMAT_STRING);
        var expected = new HadoopResourceDetails(uri, "TYPE", "FORMAT");

        // When
        var details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // Then
        assertThat(details)
                .as("Check that resource Details have been formatted correctly")
                .isEqualTo(expected);
    }

    @Test
    void testDetailsReturnConsistentResource() {
        var uri = new File(".").toURI().resolve(HadoopResourceDetails.FORMAT_STRING);
        var details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // When
        var resource = details.getResource();

        // Then
        assertThat(resource)
                .as("Check that after extracting the id, type and format, they have all been returned successfully")
                .extracting("id", "type", "serialisedFormat")
                .contains(details.getFileName().toString(), details.getType(), details.getFormat());
    }

    @Test
    void testNoSupportedTypeTest() {
        // Given
        var uri = new File(".").toURI().resolve("a_file.txt");

        // When
        var illegalAccessException = assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(uri), "Test should throw an exception");

        // Then check the assertion message
        assertThat(illegalAccessException.getMessage())
                .as("Check the error message has been populated successfully")
                .contains("Type 'a' is not supported");
    }

    @Test
    void testIsValidResourceNameUsingInvalidFileNameButValidPath() {
        // Given
        var uri = URI.create("file:///edge_case/that-matches/if.using/fullPath");

        // When
        var details = HadoopResourceDetails.isValidResourceName(uri);

        assertThat(details)
                .as("Check that the uri is an invalid resource name")
                .isFalse();
    }
}
