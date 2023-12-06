package es.karmadev.api.channel.subscription;

import es.karmadev.api.channel.subscription.event.NetworkEvent;

/**
 * Represents a subscriptor. A subscriptor
 * is an object which can hold subscriptions
 */
public interface Subscriptor {

    /**
     * Add a subscription
     *
     * @param subscription the subscription to add
     */
    void subscribe(final AChannelSubscription subscription);

    /**
     * Remove a subscription
     *
     * @param subscription the subscription to remove
     */
    void unsubscribe(final AChannelSubscription subscription);

    /**
     * Handle an event
     *
     * @param event the event
     */
    void handle(final NetworkEvent event);
}
