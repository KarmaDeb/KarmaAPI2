package es.karmadev.api.channel.exception.connection;

import es.karmadev.api.channel.exception.NetException;

/**
 * This exception is thrown when either an
 * invalid address or invalid port is given
 * when creating an address
 */
public class UnresolvableException extends NetException {

    /**
     * Initializes the exception
     *
     * @param address the address
     * @param port the port
     */
    public UnresolvableException(final String address, final int port) {
        super(String.format("Cannot create address %s:%d. (Either address is null, or port is out of range [1025-65535])", address, port));
    }
}
