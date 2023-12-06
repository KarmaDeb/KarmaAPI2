package es.karmadev.api.channel;

import es.karmadev.api.channel.subscription.AChannelSubscription;
import es.karmadev.api.channel.subscription.event.NetworkEvent;
import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.schedule.task.completable.TaskCompletor;

import java.net.SocketAddress;
import java.util.Collection;

/**
 * Represents a server channel
 */
public interface IServer {

    /**
     * Get the server ID
     *
     * @return the server ID
     */
    long getId();

    /**
     * Get the address of the server
     *
     * @return the server address
     */
    SocketAddress getAddress();

    /**
     * Get the server channels
     *
     * @return the server channels
     */
    Collection<? extends IChannel> getChannels();

    /**
     * Create a new channel.
     *
     * @param name the channel name
     * @return the created channel
     */
    IChannel createChannel(final String name);

    /**
     * Add a subscriptor to the server. A subscriptor
     * requires a subscription, the difference between
     * calling this method and not {@link IChannel#subscribe(AChannelSubscription)}
     * is that when called this method, the subscription will
     * be added to every new channel
     *
     * @param subscription the subscription to add
     */
    void addSubscriptor(final AChannelSubscription subscription);

    /**
     * Remove a subscriptor from the
     * server
     *
     * @param subscription the subscription
     */
    void removeSubscriptor(final AChannelSubscription subscription);

    /**
     * Propagate an even through the
     * channels
     *
     * @param event the event to propagate
     */
    void propagate(final NetworkEvent event);

    /**
     * Write a message to all the clients. Unlike the
     * method {@link IChannel#write(BaseMessage)} this
     * method sends the message to all the clients directly
     *
     * @param message the message to send
     */
    void write(final BaseMessage message);

    /**
     * Get if the server is running
     *
     * @return if the server is
     * running
     */
    boolean isRunning();

    /**
     * Start the server
     *
     * @return if the server was started
     */
    TaskCompletor<Boolean> start();

    /**
     * Stop the server
     */
    void stop();
}
