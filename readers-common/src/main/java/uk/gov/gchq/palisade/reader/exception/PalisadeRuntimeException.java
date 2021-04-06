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
package uk.gov.gchq.palisade.reader.exception;

import uk.gov.gchq.palisade.reader.common.Generated;

import java.util.StringJoiner;

import static uk.gov.gchq.palisade.reader.exception.Status.INTERNAL_SERVER_ERROR;

/**
 * Subtype of {@link RuntimeException} with additional constructors to support the inclusion of a HTTP error message
 * along with the other exception details.
 */
public class PalisadeRuntimeException extends RuntimeException {

    private final Status status;

    public PalisadeRuntimeException(final String message) {
        this(message, INTERNAL_SERVER_ERROR);
    }

    public PalisadeRuntimeException(final Throwable cause) {
        this(cause, INTERNAL_SERVER_ERROR);
    }

    public PalisadeRuntimeException(final String message, final Throwable cause) {
        this(message, cause, INTERNAL_SERVER_ERROR);
    }

    public PalisadeRuntimeException(final String message, final Status status) {
        super(message);
        this.status = status;
    }

    public PalisadeRuntimeException(final Throwable cause, final Status status) {
        super(cause);
        this.status = status;
    }

    public PalisadeRuntimeException(final String message, final Throwable cause, final Status status) {
        super(message, cause);
        this.status = status;
    }

    @Generated
    public Status getStatus() {
        return status;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", PalisadeRuntimeException.class.getSimpleName() + "[", "]")
                .add("status=" + status)
                .add(super.toString())
                .toString();
    }
}
