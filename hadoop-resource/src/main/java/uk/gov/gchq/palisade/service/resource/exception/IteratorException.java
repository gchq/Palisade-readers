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
     * @param cause     a {@link Throwable} that caused the iss
     */
    public IteratorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
