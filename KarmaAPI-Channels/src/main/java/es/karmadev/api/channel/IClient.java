package es.karmadev.api.channel;

import es.karmadev.api.channel.com.IConnection;
import es.karmadev.api.schedule.task.completable.BiTaskCompletor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Represents a client
 */
public interface IClient {

    /**
     * Get the client ID
     *
     * @return the client ID
     */
    long getId();

    /**
     * Connect the client to a server
     *
     * @param address the server address
     * @param port the server port
     * @return the connection task
     */
    default BiTaskCompletor<Boolean, IConnection> connect(final String address, final int port) {
        InetSocketAddress sa = new InetSocketAddress(address, port);
        return connect(sa, true);
    }

    /**
     * Connect the client to a server
     *
     * @param address the server address
     * @return the connection task
     */
    default BiTaskCompletor<Boolean, IConnection> connect(final SocketAddress address) {
        return connect(address, true);
    }

    /**
     * Connect the client to a server
     *
     * @param address the server address
     * @param port the server port
     * @param bridge if the connection supports
     *               bridging
     * @return the connection task
     */
    default BiTaskCompletor<Boolean, IConnection> connect(final String address, final int port, final boolean bridge) {
        InetSocketAddress sa = new InetSocketAddress(address, port);
        return connect(sa, bridge);
    }

    /**
     * Connect the client to a server
     *
     * @param address the server address
     * @param bridge if the connection supports
     *               bridging
     * @return the connection task
     */
    BiTaskCompletor<Boolean, IConnection> connect(final SocketAddress address, final boolean bridge);

    /**
     * Get if the client is connected
     *
     * @return the client connection
     * status
     */
    boolean isConnected();

    /**
     * Get the client connection
     *
     * @return the client connection
     */
    IConnection getConnection();

    /**
     * Get if the connection supports bridging.
     * Bridging allows two connections to be
     * directly connected through a virtual channel
     *
     * @return if the connection supports bridging
     */
    boolean supportsBridging();
}
