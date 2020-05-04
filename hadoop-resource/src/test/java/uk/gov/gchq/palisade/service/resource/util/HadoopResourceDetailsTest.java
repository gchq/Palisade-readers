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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.File;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JUnit4.class)
public class HadoopResourceDetailsTest {

    @Test
    public void formatStringIsConsistent() {
        // Given
        URI uri = new File(".").toURI().resolve(HadoopResourceDetails.FORMAT_STRING);
        HadoopResourceDetails expected = new HadoopResourceDetails(uri, "TYPE", "FORMAT");

        // When
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // Then
        assertThat(details, equalTo(expected));
    }

    @Test
    public void detailsReturnConsistentResource() {
        URI uri = new File(".").toURI().resolve(HadoopResourceDetails.FORMAT_STRING);
        HadoopResourceDetails details = HadoopResourceDetails.getResourceDetailsFromFileName(uri);

        // When
        LeafResource resource = details.getResource();

        // Then
        assertThat(resource.getId(), equalTo(details.getFileName().toString()));
        assertThat(resource.getType(), equalTo(details.getType()));
        assertThat(resource.getSerialisedFormat(), equalTo(details.getFormat()));
    }

}
