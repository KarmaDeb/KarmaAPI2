package es.karmadev.api.channel;

import es.karmadev.api.channel.com.IBridge;
import es.karmadev.api.channel.subscription.Subscriptor;
import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.schedule.task.completable.TaskCompletor;

/**
 * Represents a channel between two or
 * more connections
 */
public interface IChannel extends Subscriptor {

    /**
     * Get the channel ID
     *
     * @return the channel ID
     */
    long getId();

    /**
     * Get the channel name
     *
     * @return the channel name
     */
    String getName();

    /**
     * Publish the channel on
     * the server
     *
     * @return if the channel could be
     * published
     */
    boolean publish();

    /**
     * Get if the channel has been
     * published
     *
     * @return if the channel has been
     * published
     */
    boolean isPublished();

    /**
     * Creates a bridge between the current
     * client and the desired client
     *
     * @param to the client destination
     * @return the bridge
     */
    TaskCompletor<IBridge> createBridge(final long to);

    /**
     * Write a message to the clients
     * connected on the channel
     *
     * @param message the channel to send
     */
    void write(final BaseMessage message);
}
