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

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Helper class around {@link LeafResource} fields used to validate resource details with what Hadoop
 * expects. Additionally used for predicates filtering Hadoop resource response streams by type or
 * serialised format.
 */
public class HadoopResourceDetails {

    public static final Pattern FILENAME_PATTERN = Pattern.compile("(?<type>.+)_(?<name>.+)\\.(?<format>.+)");
    public static final String FORMAT_STRING = "TYPE_FILENAME.FORMAT";
    protected static final Map<String, String> SUPPORTED_TYPES = new HashMap<>();
    private URI fileName;
    private String type;
    private String format;

    public HadoopResourceDetails(final URI fileName, final String type, final String format) {
        this.fileName = fileName;
        this.type = type;
        this.format = format;
    }

    public static HadoopResourceDetails getResourceDetailsFromFileName(final URI fileName) {
        //get filename component
        SUPPORTED_TYPES.put("employee", "uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee");
        final String[] split = fileName.toString().split(Pattern.quote("/"));
        final String fileString = split[split.length - 1];
        //check match
        Matcher match = validateNameRegex(fileString);
        if (!match.matches()) {
            throw new IllegalArgumentException("Filename doesn't comply with " + FORMAT_STRING + ": " + fileName);
        }

        String type;
        try {
            type = match.group("type").toLowerCase(Locale.getDefault());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new IllegalArgumentException("This type is not supported", ex);
        }

        return new HadoopResourceDetails(fileName, SUPPORTED_TYPES.get(type), match.group("format"));
    }

    public static boolean isValidResourceName(final URI fileName) {
        requireNonNull(fileName);
        return validateNameRegex(fileName.toString()).matches();
    }

    private static Matcher validateNameRegex(final String fileName) {
        return FILENAME_PATTERN.matcher(fileName);
    }

    public LeafResource getResource() {
        return ((LeafResource) ResourceBuilder.create(fileName))
                .type(type)
                .serialisedFormat(format);
    }

    @Generated
    public URI getFileName() {
        return fileName;
    }

    @Generated
    public void setFileName(final URI fileName) {
        requireNonNull(fileName);
        this.fileName = fileName;
    }

    @Generated
    public String getType() {
        return type;
    }

    @Generated
    public void setType(final String type) {
        requireNonNull(type);
        this.type = type;
    }

    @Generated
    public String getFormat() {
        return format;
    }

    @Generated
    public void setFormat(final String format) {
        requireNonNull(format);
        this.format = format;
    }


    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HadoopResourceDetails)) {
            return false;
        }
        HadoopResourceDetails that = (HadoopResourceDetails) o;
        return fileName.equals(that.fileName) &&
                type.equals(that.type) &&
                format.equals(that.format);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(fileName, type, format);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", HadoopResourceDetails.class.getSimpleName() + "[", "]")
                .add("fileName='" + fileName + "'")
                .add("type='" + type + "'")
                .add("format='" + format + "'")
                .toString();
    }
}
