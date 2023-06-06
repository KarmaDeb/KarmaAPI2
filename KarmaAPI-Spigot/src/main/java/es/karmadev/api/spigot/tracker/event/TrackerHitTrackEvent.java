package es.karmadev.api.spigot.tracker.event;

import es.karmadev.api.spigot.entity.trace.result.RayTraceResult;
import es.karmadev.api.spigot.tracker.TrackerEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a tracker
 * hits a tracking entity
 */
public class TrackerHitTrackEvent extends EntityEvent {

    private final static HandlerList handlerList = new HandlerList();

    private final TrackerEntity tracker;
    private final LivingEntity entity;
    private final RayTraceResult rayTrace;

    /**
     * Create the hit track event
     *
     * @param tracker th tracker
     * @param entity the entity
     * @param rayTrace the hit raytrace
     */
    public TrackerHitTrackEvent(final TrackerEntity tracker, final LivingEntity entity, final RayTraceResult rayTrace) {
        super(entity);
        this.tracker = tracker;
        this.entity = entity;
        this.rayTrace = rayTrace;
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

    /**
     * Get the hit raytrace result
     *
     * @return the raytrace result
     */
    public RayTraceResult getRayTrace() {
        return rayTrace;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
