package es.karmadev.api.channel.exception.connection;

import es.karmadev.api.channel.exception.NetException;
import org.jetbrains.annotations.NotNull;

/**
 * The unresolved exception is thrown when
 * a socket address cannot be resolved.
 */
public class UnresolvedException extends NetException {

    /**
     * Initialize the exception
     *
     * @param address the address
     * @param port the port
     */
    public UnresolvedException(final @NotNull String address, final int port) {
        super(String.format("Cannot use address %s:%d. (Unresolved)", address, port));
    }
}
