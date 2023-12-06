package es.karmadev.api.channel.exception.connection;

import es.karmadev.api.channel.exception.NetException;

/**
 * This exception is raised when
 * a close operation fails to complete.
 */
public class CloseException extends NetException {

    /**
     * Initialize the exception
     *
     * @param message the error message
     */
    public CloseException(final String message) {
        super(message);
    }

    /**
     * Initialize the exception
     *
     * @param error the error that caused this
     *              exception
     */
    public CloseException(final Throwable error) {
        super(error);
    }
}
