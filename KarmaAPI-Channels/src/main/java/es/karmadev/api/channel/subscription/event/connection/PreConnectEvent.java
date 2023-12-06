package es.karmadev.api.channel.subscription.event.connection;

import es.karmadev.api.channel.IChannel;
import es.karmadev.api.channel.subscription.event.Cancellable;
import es.karmadev.api.channel.subscription.event.NetworkEvent;
import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;
import java.util.Properties;

/**
 * This event is fired exclusively before
 * a connection is completely made during
 * the {@link IChannel#connect()}
 */
@Getter
public final class PreConnectEvent implements NetworkEvent, Cancellable {

    private final IChannel channel;
    private final SocketAddress address;
    private final Properties connectionProperties;

    @Setter
    private boolean cancelled = false;

    /**
     * Initialize the event
     *
     * @param channel the channel that is calling
     *                the event
     * @param address the address that is starting the
     *                connection
     * @param properties the connection properties
     */
    public PreConnectEvent(final IChannel channel, final SocketAddress address, final Properties properties) {
        this.channel = channel;
        this.address = address;
        this.connectionProperties = properties;
    }
}
