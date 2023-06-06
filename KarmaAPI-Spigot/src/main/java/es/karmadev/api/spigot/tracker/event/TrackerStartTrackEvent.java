package es.karmadev.api.spigot.tracker.event;

import es.karmadev.api.spigot.tracker.TrackerEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a tracker
 * starts tracking an entity
 */
public class TrackerStartTrackEvent extends EntityEvent {

    private final static HandlerList handlerList = new HandlerList();

    private final TrackerEntity tracker;
    private final LivingEntity entity;

    /**
     * Create the start track event
     *
     * @param tracker th tracker
     * @param entity the entity
     */
    public TrackerStartTrackEvent(final TrackerEntity tracker, final LivingEntity entity) {
        super(entity);
        this.tracker = tracker;
        this.entity = entity;
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
}
