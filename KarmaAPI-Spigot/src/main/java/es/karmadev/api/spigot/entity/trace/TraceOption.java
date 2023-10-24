package es.karmadev.api.spigot.entity.trace;

/**
 * Entity ray trace options
 */
public enum TraceOption {
    /**
     * Rollback the location when a block
     * is hit, meaning it will heave the
     * new Y, but the old x and z
     */
    ROLLBACK_ON_HIT,
    /**
     * Stop as soon as something
     * gets hit
     */
    STOP_ON_FIRST_HIT,
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
