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

package uk.gov.gchq.palisade.reader.common.resource.impl;

import uk.gov.gchq.palisade.reader.common.ConnectionDetail;
import uk.gov.gchq.palisade.reader.common.Generated;
import uk.gov.gchq.palisade.reader.common.resource.AbstractLeafResource;
import uk.gov.gchq.palisade.reader.common.resource.ParentResource;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class StreamResource extends AbstractLeafResource {
    private static final long serialVersionUID = 1L;

    protected long start;
    protected long end;

    public StreamResource() {
        //no-args constructor needed for serialization only
    }

    @Override
    public StreamResource id(final String id) {
        return (StreamResource) super.id(id);
    }

    @Override
    public StreamResource type(final String type) {
        return (StreamResource) super.type(type);
    }

    @Override
    public StreamResource serialisedFormat(final String serialisedFormat) {
        return (StreamResource) super.serialisedFormat(serialisedFormat);
    }

    @Override
    public StreamResource connectionDetail(final ConnectionDetail connectionDetail) {
        return (StreamResource) super.connectionDetail(connectionDetail);
    }

    @Override
    public StreamResource attributes(final Map<String, Serializable> attributes) {
        return (StreamResource) super.attributes(attributes);
    }

    @Override
    public StreamResource attribute(final String attributeKey, final Serializable attributeValue) {
        return (StreamResource) super.attribute(attributeKey, attributeValue);
    }

    @Override
    public StreamResource parent(final ParentResource parent) {
        return (StreamResource) super.parent(parent);
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StreamResource)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final StreamResource that = (StreamResource) o;
        return start == that.start &&
                end == that.end;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), start, end);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", StreamResource.class.getSimpleName() + "[", "]")
                .add("start=" + start)
                .add("end=" + end)
                .toString();
    }
}
