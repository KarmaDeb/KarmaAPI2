package es.karmadev.api.channel.subscription.event;

import es.karmadev.api.channel.IChannel;

/**
 * Represents a network event
 */
public interface NetworkEvent {

    /**
     * Get the event channel
     *
     * @return the channel
     */
    IChannel getChannel();
}
