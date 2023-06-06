package es.karmadev.api.spigot.tracker;

import org.bukkit.entity.LivingEntity;

/**
 * Auto tracer, to track
 * entities automatically
 */
public interface AutoTracker {

    /**
     * Track an entity
     *
     * @param tracker the tracker
     * @param radius the max radius
     * @return the entities to track
     */
    LivingEntity[] track(final TrackerEntity tracker, final double radius);
}
