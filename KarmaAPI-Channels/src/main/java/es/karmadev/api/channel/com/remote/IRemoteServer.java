package es.karmadev.api.channel.com.remote;

import es.karmadev.api.channel.IChannel;
import es.karmadev.api.channel.com.IConnection;
import es.karmadev.api.schedule.task.completable.BiTaskCompletor;

import java.net.SocketAddress;

/**
 * Represents a remote server
 */
public interface IRemoteServer extends IConnection {

    /**
     * Get the server address
     *
     * @return the server address
     */
    SocketAddress getAddress();

    /**
     * Request access to a server channel
     *
     * @param channelId the channel id
     * @return the channel request
     */
    BiTaskCompletor<Boolean, IChannel> joinChannel(final long channelId);

    /**
     * Leave a channel
     *
     * @param channelId the channel id to leave
     */
    void leaveChannel(final long channelId);
}
