package es.karmadev.api.spigot.tracker;

import es.karmadev.api.spigot.entity.trace.ray.PointRayTrace;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Optional;

/**
 * Tracker entity
 */
public abstract class TrackerEntity {

    private static int globalId = 0;
    protected final int id = ++globalId;
    protected final Plugin plugin;

    /**
     * Initialize the tracker entity
     *
     * @param plugin the plugin
     */
    public TrackerEntity(final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the tracker ID
     *
     * @return the tracker ID
     */
    public final int getId() {
        return id;
    }

    /**
     * Get the tracker plugin
     *
     * @return the plugin
     */
    public final Plugin getPlugin() {
        return plugin;
    }

    /**
     * Get the tracker world
     *
     * @return the tracker world
     */
    public abstract World getWorld();

    /**
     * Get the tracker position X
     *
     * @return the tracker X position
     */
    public abstract double getX();

    /**
     * Get the tracker position Y
     *
     * @return the tracker Y position
     */
    public abstract double getY();

    /**
     * Get the tracker position Z
     *
     * @return the tracker Z position
     */
    public abstract double getZ();

    /**
     * Set the tracker property
     *
     * @param key the key
     * @param value the value
     */
    public abstract void setProperty(final String key, final Object value);

    /**
     * Get a property
     *
     * @param key the property key
     * @param deffault the property default value
     * @return the property value
     * @param <T> the property type
     */
    public abstract <T> T getProperty(final String key, final T deffault);

    /**
     * Get track target
     *
     * @return the target
     */
    public abstract Optional<LivingEntity> getTarget();

    /**
     * Get the tracker auto tracker
     *
     * @return the auto tracker
     */
    public abstract Optional<AutoTracker> getTracker();

    /**
     * Get the entity that is the
     * tracker
     *
     * @return the entity tracker
     */
    public abstract Entity getEntity();

    /**
     * Create a raytrace from this tracker
     * to an entity
     *
     * @param target the entity
     * @return the raytrace
     */
    public abstract Optional<PointRayTrace> createRayTrace(final LivingEntity target);

    /**
     * Get the current track direction
     *
     * @return the track direction
     */
    public abstract Vector getDirection();

    /**
     * Set the tracker auto tracker
     *
     * @param tracker the auto tracker
     */
    public abstract void setTracker(final AutoTracker tracker);

    /**
     * Set the tracker target
     *
     * @param entity the target
     */
    public abstract void setTrackTarget(final LivingEntity entity);

    /**
     * Start the track task
     */
    public final void start() {
        start(20);
    }

    /**
     * Start the track task
     *
     * @param period the track period in ticks
     */
    public abstract void start(final long period);

    /**
     * Stop the track task
     */
    public abstract void stop();

    /**
     * Destroy the tracker
     */
    public abstract void destroy();

    /**
     * Get if the track task is stopped
     *
     * @return if the track task is stopped
     */
    public abstract boolean isStopped();

    /**
     * Get if the tracker entity is valid
     *
     * @return if the tracker entity is valid
     */
    public abstract boolean isValid();
}
