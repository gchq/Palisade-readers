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

package uk.gov.gchq.palisade.service.resource.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HadoopResourceDetailsTest {

    @BeforeEach
    public void setup() {
        HadoopResourceDetails.addTypeSupport("type", "TYPE");
    }

    @Test
    void acceptsSchemelessUri() throws URISyntaxException {
        // Given
        URI uri = new URI("/home/hadoop/resources/type_file.format");
        HadoopResourceDetails expected = new HadoopResourceDetails(uri, "TYPE", "format");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void acceptsAbsolutePath() throws URISyntaxException {
        // Given
        URI absolute = new URI("file:/home/hadoop/resources/type_file.format");
        HadoopResourceDetails expected = new HadoopResourceDetails(absolute, "TYPE", "format");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(absolute);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void acceptsRelativePath() throws URISyntaxException {
        // Given
        URI relative = new URI("file:./type_file.format");
        HadoopResourceDetails expected = new HadoopResourceDetails(relative, "TYPE", "format");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(relative);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void noTypeThrowsException() {
        // Given
        URI invalidType = new File(".").toURI().resolve("file.format");

        // When
        assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidType));

        // Then throw
    }

    @Test
    void emptyTypeThrowsException() {
        // Given
        URI invalidType = new File(".").toURI().resolve("_file.format");

        // When
        assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidType));

        // Then throw
    }

    @Test
    void noSerialisedFormatThrowsException() {
        // Given
        URI invalidFormat = new File(".").toURI().resolve("type_file");

        // When
        assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidFormat));

        // Then throw
    }

    @Test
    void emptySerialisedFormatThrowsException() {
        // Given
        URI invalidFormat = new File(".").toURI().resolve("type_file.");

        // When
        assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(invalidFormat));

        // Then throw
    }

    @Test
    void formatStringIsConsistent() {
        // Given
        URI uri = new File(".").toURI().resolve(HadoopResourceDetails.FORMAT_STRING);
        HadoopResourceDetails expected = new HadoopResourceDetails(uri, "TYPE", "FORMAT");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // Then
        assertThat(details).isEqualTo(expected);
    }

    @Test
    void detailsReturnConsistentResource() {
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
    void noSupportedTypeTest() {
        // Given
        URI uri = new File(".").toURI().resolve("a_file.txt");

        // When
        assertThrows(IllegalArgumentException.class,
                () -> HadoopResourceDetails.getResourceDetailsFromFileName(uri));
    }

    @Test
    void isValidResourceNameUsingInvalidFileNameButValidPath() {
        // Given
        URI uri = URI.create("file:///edge_case/that-matches/if.using/fullPath");

        // When
        boolean details = HadoopResourceDetails.isValidResourceName(uri);

        assertFalse(details, "This should be false as the filename fullPath doesn't follow the pattern");
    }
}
