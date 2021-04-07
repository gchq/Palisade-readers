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

package uk.gov.gchq.palisade.reader.request;

import uk.gov.gchq.palisade.reader.common.Generated;
import uk.gov.gchq.palisade.reader.common.resource.LeafResource;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * This is the high level API object that is used to pass back to the client the information it requires to connect to
 * the correct data service implementations and to decide how best to parallelise their job.
 * <p>
 * It is also the object that the client then passes to the data service to access the data. When it is passed to the
 * data service the resources field might have been changed to be a subset of the resources.
 */
public class DataRequestResponse extends Request {
    private String token;
    private Set<LeafResource> resources;

    public DataRequestResponse() {
        //no-args constructor needed for serialization only
    }

    @Generated
    public DataRequestResponse token(final String token) {
        this.setToken(token);
        return this;
    }

    @Generated
    public DataRequestResponse resource(final LeafResource resource) {
        this.addResource(resource);
        return this;
    }

    @Generated
    public DataRequestResponse resources(final Set<LeafResource> resources) {
        this.setResources(resources);
        return this;
    }

    /**
     * Adds a {@link LeafResource} to a set of LeafResources
     * @param resource a LeafResource to add to this class
     */
    public void addResource(final LeafResource resource) {
        requireNonNull(resource);
        if (resources == null) {
            resources = new HashSet<>();
        }
        resources.add(resource);
    }

    @Generated
    public String getToken() {
        return token;
    }

    @Generated
    public void setToken(final String token) {
        requireNonNull(token);
        this.token = token;
    }

    @Generated
    public Set<LeafResource> getResources() {
        return new HashSet<>(resources);
    }

    @Generated
    public void setResources(final Set<LeafResource> resources) {
        requireNonNull(resources);
        this.resources = new HashSet<>(resources);
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataRequestResponse)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DataRequestResponse that = (DataRequestResponse) o;
        return token.equals(that.token) &&
                resources.equals(that.resources);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), token, resources);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", DataRequestResponse.class.getSimpleName() + "[", "]")
                .add("token='" + token + "'")
                .add("resources=" + resources)
                .add(super.toString())
                .toString();
    }
}
