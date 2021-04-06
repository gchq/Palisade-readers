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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.reader.common.Generated;
import uk.gov.gchq.palisade.reader.common.resource.LeafResource;
import uk.gov.gchq.palisade.reader.common.util.ResourceBuilder;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopResourceDetails.class);
    private static final Map<String, String> SUPPORTED_TYPES = new HashMap<>();
    private URI fileName;
    private String type;
    private String format;

    /**
     * Constructs a new {@link HadoopResourceDetails} object using a {@link URI} file name value,
     * a {@link String} type value and a {@link String} format value.
     *
     * @param fileName the {@link URI} value of the file name
     * @param type     a {@link String} value of the file type
     * @param format   a {@link String} value of the file format
     */
    public HadoopResourceDetails(final URI fileName, final String type, final String format) {
        this.fileName = fileName;
        this.type = type;
        this.format = format;
    }

    /**
     * Adds a valid type and type class to a {@link Map} of supported types
     *
     * @param type        A {@link String} value of the type that will be in the resource file name
     * @param classString A {@link String} value of the fully qualified class of the type
     */
    public static void addTypeSupport(final String type, final String classString) {
        if (SUPPORTED_TYPES.containsKey(type)) {
            LOGGER.warn("Type '{}' with value '{}' will be overwritten with the new value '{}'", type, SUPPORTED_TYPES.get(type), classString);
        }
        SUPPORTED_TYPES.put(type, classString);
    }

    /**
     * Creates a new {@link HadoopResourceDetails} object using a {@link URI} file name value.
     *
     * @param fileName the {@link URI} value of the file name
     * @return a new {@link HadoopResourceDetails} object
     */
    public static HadoopResourceDetails getResourceDetailsFromFileName(final URI fileName) {

        Matcher match = validateNameRegex(fileName);
        if (!match.matches()) {
            throw new IllegalArgumentException("Filename doesn't comply with " + FORMAT_STRING + ": " + fileName);
        }

        String type = match.group("type").toLowerCase(Locale.getDefault());

        if (SUPPORTED_TYPES.get(type) != null) {
            return new HadoopResourceDetails(fileName, SUPPORTED_TYPES.get(type), match.group("format"));
        } else {
            throw new IllegalArgumentException(String.format("Type '%s' is not supported", type));
        }
    }

    /**
     * Checks that the {@link URI} file name value matches a defined regex expression
     *
     * @param fileURI the {@link URI} value of the file
     * @return a {@link Boolean} value
     */
    public static boolean isValidResourceName(final URI fileURI) {
        requireNonNull(fileURI);
        Matcher match = validateNameRegex(fileURI);
        return match.matches();
    }

    private static Matcher validateNameRegex(final URI fileURI) {
        //get filename component
        final String[] split = fileURI.toString().split(Pattern.quote("/"));
        final String fileName = split[split.length - 1];
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
