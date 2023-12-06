package es.karmadev.api.channel.com;

import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.channel.exception.NetException;

/**
 * Represents a connection on a
 * {@link es.karmadev.api.channel.IChannel}
 */
public interface IConnection {

    /**
     * Write a message on the connection
     *
     * @param message the message to write
     */
    void write(final BaseMessage message);

    /**
     * Get if the connection is encrypted.
     *
     * @return if the connection uses
     * encrypted data
     */
    boolean isEncrypted();

    /**
     * Tries to close the connection. Once this
     * method is called, the connection will get
     * closed even though {@link NetException exception} is
     * thrown.
     *
     * @throws NetException if there's a network
     * problem while closing the connection
     */
    void close() throws NetException;

    /**
     * Protect the message. This method will
     * always return null if {@link #isEncrypted()} is
     * false
     *
     * @param message the message to protect
     * @return the protected message
     */
    byte[] protectMessage(final BaseMessage message);
}
