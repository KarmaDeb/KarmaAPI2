package es.karmadev.api.channel.exception.connection;

import es.karmadev.api.channel.exception.NetException;

/**
 * This exception is thrown when no
 * suitable network interface driver
 * is found on the system.
 */
public class NoNetworkException extends NetException {

    /**
     * Initialize the exception
     */
    public NoNetworkException() {
        super("Failed to create connection. (Are we connected to the internet?)");
    }
}