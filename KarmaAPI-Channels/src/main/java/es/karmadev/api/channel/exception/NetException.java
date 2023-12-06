package es.karmadev.api.channel.exception;

/**
 * Represents a generic network exception.
 * The amount of exceptions that can be thrown
 * of this type are infinite
 */
public class NetException extends Exception {

    /**
     * Initializes the network exception
     *
     * @param message the message
     */
    protected NetException(final String message) {
        super(message);
    }

    /**
     * Initialize the network exception
     *
     * @param error the error that caused this
     *              issue
     */
    protected NetException(final Throwable error) {
        super(error);
    }

    /**
     * Initializes the network exception
     *
     * @param message the message
     * @param other the exception that caused this
     *              issue
     */
    protected NetException(final String message, final Throwable other) {
        super(message, other);
    }
}
