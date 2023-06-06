package es.karmadev.api.spigot.tracker.event;

import es.karmadev.api.spigot.tracker.TrackerEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a tracker changes
 * the target entity
 */
public class TrackerSwitchTrackEvent extends EntityEvent {

    private final static HandlerList handlerList = new HandlerList();

    private final TrackerEntity tracker;
    private final LivingEntity oldEntity;
    private final SwitchReason reason;

    private LivingEntity newEntity;

    /**
     * Create the start track event
     *
     * @param tracker th tracker
     * @param oldEntity the old entity
     * @param newEntity the new entity
     * @param reason the switch reason
     */
    public TrackerSwitchTrackEvent(final TrackerEntity tracker, final LivingEntity oldEntity, final LivingEntity newEntity, final SwitchReason reason) {
        super(oldEntity);
        this.tracker = tracker;
        this.oldEntity = oldEntity;
        this.newEntity = newEntity;
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
    public SwitchReason getReason() {
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
        return oldEntity;
    }

    /**
     * Get the entity tha is being
     * replaced by
     *
     * @return the new entity
     */
    public LivingEntity getNewEntity() {
        return newEntity;
    }

    /**
     * Set the new entity
     *
     * @param entity the entity
     */
    public void setNewEntity(final LivingEntity entity) {
        if (entity == null) return;
        newEntity = entity;
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
    public enum SwitchReason {
        NO_LONGER_VISIBLE,
        NO_LONGER_ALIVE,
        AUTO_TRACKER,
        FORCED
    }
}
