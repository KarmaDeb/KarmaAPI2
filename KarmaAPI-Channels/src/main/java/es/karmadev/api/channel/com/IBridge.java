package es.karmadev.api.channel.com;

import es.karmadev.api.channel.IChannel;
import es.karmadev.api.channel.exception.NetException;

/**
 * Represents a direct bridge between
 * two connections through a channel.
 * The bridge can be used to directly
 * send data to another part of the
 * connection network.
 */
public interface IBridge {

    /**
     * Get the connection that originated
     * the bridge
     *
     * @return the connection that created the
     * bridge
     */
    IConnection getOrigin();

    /**
     * Get the channel in where the data
     * is sent on this bridge
     *
     * @return the bridge channel
     */
    IChannel getChannel();

    /**
     * Get the connection that receives
     * the data on this bridge
     *
     * @return the connection that receives
     * data
     */
    IConnection getTarget();

    /**
     * Get if the bridge connection
     * is encrypted
     *
     * @return if the bridge is encrypted
     * or not
     */
    boolean isEncrypted();

    /**
     * Emit a message though the bridge
     *
     * @param data the data to send
     */
    void emit(final byte[] data);

    /**
     * Close the bridge connection
     *
     * @throws NetException if there's a problem
     * while closing the connection
     */
    void close() throws NetException;
}
