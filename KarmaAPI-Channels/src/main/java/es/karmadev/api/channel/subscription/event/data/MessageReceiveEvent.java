package es.karmadev.api.channel.subscription.event.data;

import es.karmadev.api.channel.IChannel;
import es.karmadev.api.channel.subscription.event.NetworkEvent;
import es.karmadev.api.channel.data.BaseMessage;
import lombok.Getter;

/**
 * Message receive event
 */
@Getter
public class MessageReceiveEvent implements NetworkEvent {

    private final IChannel channel;
    private final BaseMessage message;

    /**
     * Initialize the event
     *
     * @param channel the channel the message has
     *                been received through
     * @param message the message
     */
    public MessageReceiveEvent(final IChannel channel, final BaseMessage message) {
       this.channel = channel;
       this.message = message;
    }
}
