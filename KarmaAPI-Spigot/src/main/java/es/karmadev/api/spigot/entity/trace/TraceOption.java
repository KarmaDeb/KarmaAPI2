package es.karmadev.api.spigot.entity.trace;

/**
 * Entity ray trace options
 */
public enum TraceOption {
    /**
     * Stop the raytrace task when an
     * entity is hit
     */
    STOP_ON_ENTITY,
    /**
     * Stop the raytrace task when a
     * block is hit
     */
    STOP_ON_BLOCK,
    /**
     * Stop the raytrace task when a
     * solid block is hit
     */
    STOP_ON_SOLID_BLOCK,
    /**
     * Stop the raytrace task when a
     * block or entity is hit
     */
    STOP_ON_HIT,
    /**
     * Stop the raytrace task when a
     * solid block or entity is hit
     */
    STOP_ON_SOLID_HIT
}
