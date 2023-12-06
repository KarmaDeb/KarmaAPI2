package es.karmadev.api.channel.subscription;

import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a channel subscription.
 * A subscription is nothing but a contract between
 * a connection and a channel, in where the channel
 * will notice the connection when a new message is
 * received under specific circumstances or vice-versa
 */
@Getter
public abstract class AChannelSubscription {

    private final long id = ThreadLocalRandom.current().nextLong();
}
