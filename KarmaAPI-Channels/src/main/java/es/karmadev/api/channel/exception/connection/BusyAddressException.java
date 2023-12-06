package es.karmadev.api.channel.exception.connection;

import es.karmadev.api.channel.exception.NetException;

/**
 * This exception is thrown when an in-use
 * address is tried to be used on a connection
 */
public class BusyAddressException extends NetException {

    /**
     * Initialize the exception
     *
     * @param host the address
     * @param port the port
     */
    public BusyAddressException(final String host, final int port) {
        super(String.format("Failed to build connection. (Host %s:%d already in use)", host, port));
    }
}
