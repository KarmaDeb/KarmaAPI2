package es.karmadev.api.kson;

/**
 * Represents an exception related to
 * a json operation
 */
public class KsonException extends RuntimeException {

    /**
     * Initialize the exception
     *
     * @param reason the exception reason
     */
    public KsonException(final String reason) {
        super(reason);
    }

    /**
     * Initialize the exception
     *
     * @param other the exception cause
     */
    public KsonException(final Throwable other) {
        super(other);
    }

    /**
     * Initialize the exception
     *
     * @param reason the exception reason
     * @param other the exception cause
     */
    public KsonException(final String reason, final Throwable other) {
        super(reason, other);
    }
}
