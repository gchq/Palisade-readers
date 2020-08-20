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

package uk.gov.gchq.palisade.service.resource.exception;

/**
 * A {@link RuntimeException} that is thrown when an iterator encounters an issue.
 */
public class IteratorException extends RuntimeException {

    /**
     * Constructs a new {@link IteratorException} with the cause.
     *
     * @param cause     a {@link Throwable} that caused the error
     */
    public IteratorException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link IteratorException} with the specified detail message.
     *
     * @param message   a {@link String} value detailing the error
     */
    public IteratorException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@link IteratorException} with the specified detail message and cause.
     *
     * @param message   a {@link String} value detailing the error
     * @param cause     a {@link Throwable} that caused the issue
     */
    public IteratorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
