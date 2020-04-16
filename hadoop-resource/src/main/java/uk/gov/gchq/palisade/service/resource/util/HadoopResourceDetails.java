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
    private String fileName;
    private String type;
    private String format;

    public HadoopResourceDetails(final String fileName, final String type, final String format) {
        this.fileName = fileName;
        this.type = type;
        this.format = format;
    }

    public static HadoopResourceDetails getResourceDetailsFromFileName(final String fileName) {
        //get filename component
        final String[] split = fileName.split(Pattern.quote("/"));
        final String fileString = split[split.length - 1];
        //check match
        Matcher match = validateNameRegex(fileString);
        if (!match.matches()) {
            throw new IllegalArgumentException("Filename doesn't comply with " + FORMAT_STRING + ": " + fileName);
        }

        return new HadoopResourceDetails(fileName, match.group("type"), match.group("format"));
    }

    public static boolean isValidResourceName(final String fileName) {
        requireNonNull(fileName);
        return validateNameRegex(fileName).matches();
    }

    private static Matcher validateNameRegex(final String fileName) {
        return FILENAME_PATTERN.matcher(fileName);
    }

    @Generated
    public String getFileName() {
        return fileName;
    }

    @Generated
    public void setFileName(final String fileName) {
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
