package es.karmadev.api.channel.subscription.event;

/**
 * Represents a cancellable
 * event
 */
public interface Cancellable {

    /**
     * Mark the event as cancelled
     *
     * @param cancelled the cancellation status
     */
    void setCancelled(final boolean cancelled);

    /**
     * Get if the event is cancelled
     *
     * @return if cancelled
     */
    boolean isCancelled();
}
