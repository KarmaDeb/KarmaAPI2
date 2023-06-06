package es.karmadev.api.spigot.tracker.event;

import es.karmadev.api.spigot.tracker.TrackerEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a tracker stops
 * tracking an entity
 */
public class TrackerStopTrackEvent extends EntityEvent {

    private final static HandlerList handlerList = new HandlerList();

    private final TrackerEntity tracker;
    private final LivingEntity entity;
    private final StopReason reason;

    /**
     * Create the start track event
     *
     * @param tracker th tracker
     * @param entity the entity
     * @param reason the stop reason
     */
    public TrackerStopTrackEvent(final TrackerEntity tracker, final LivingEntity entity, final StopReason reason) {
        super(entity);
        this.tracker = tracker;
        this.entity = entity;
        this.reason = reason;
    }

    /**
     * Get the entity tracker
     *
     * @return the tracker
     */
    public TrackerEntity getTracker() {
        return tracker;
    }

    /**
     * Get the stop reason
     *
     * @return the reason
     */
    public StopReason getReason() {
        return reason;
    }

    /**
     * Returns the Entity involved in this event
     *
     * @return Entity who is involved in this event
     */
    @NotNull
    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Track stop reason
     */
    public enum StopReason {
        NO_LONGER_VISIBLE,
        NO_LONGER_ALIVE,
        SWITCH,
        FORCED
    }
}
