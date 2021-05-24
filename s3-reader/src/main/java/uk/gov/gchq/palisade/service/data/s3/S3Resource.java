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

package uk.gov.gchq.palisade.service.data.s3;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.resource.impl.FileResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * AWS S3 {@code Resource}. Defines the information that uniquely identifies the S3 Resource.
 */
public class S3Resource extends FileResource {
    private static final long serialVersionUID = 1L;

    private Map<String, String> userMetadata;
    private Map<String, String> systemMetadata;

    public S3Resource() {
        // Empty constructor for serialisation
    }

    @Generated
    public Map<String, String> getUserMetadata() {
        return new HashMap<>(userMetadata);
    }

    @Generated
    public void setUserMetadata(final Map<String, String> userMetadata) {
        this.userMetadata = new HashMap<>(userMetadata);
    }

    @Generated
    public S3Resource userMetadata(final Map<String, String> userMetadata) {
        this.setUserMetadata(userMetadata);
        return this;
    }

    @Generated
    public Map<String, String> getSystemMetadata() {
        return new HashMap<>(systemMetadata);
    }

    @Generated
    public void setSystemMetadata(final Map<String, String> systemMetadata) {
        this.systemMetadata = new HashMap<>(systemMetadata);
    }

    @Generated
    public S3Resource systemMetadata(final Map<String, String> systemMetadata) {
        this.setSystemMetadata(systemMetadata);
        return this;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final S3Resource that = (S3Resource) o;
        return Objects.equals(userMetadata, that.userMetadata) &&
                Objects.equals(systemMetadata, that.systemMetadata);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), userMetadata, systemMetadata);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", S3Resource.class.getSimpleName() + "[", "]")
                .add("userMetadata=" + userMetadata)
                .add("systemMetadata=" + systemMetadata)
                .add(super.toString())
                .toString();
    }
}
