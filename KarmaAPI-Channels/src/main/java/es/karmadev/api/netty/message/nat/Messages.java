package es.karmadev.api.netty.message.nat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents all the messages
 */
@Getter @AllArgsConstructor
public enum Messages {
    KEY_EXCHANGE(0),
    CHANNEL_OPEN(1),
    CHANNEL_CLOSE(2),
    CHANNEL_JOIN(3),
    CHANNEL_LEAVE(4),
    DISCOVER(5);

    private final long id;

    /**
     * Get a message by its ID
     *
     * @param id the message ID
     * @return the message
     */
    public static Messages getById(final long id) {
        for (Messages message : Messages.values()) {
            if (message.id == id) return message;
        }

        return null;
    }
}
