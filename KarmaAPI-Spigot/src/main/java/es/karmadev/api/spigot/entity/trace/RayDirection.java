package es.karmadev.api.spigot.entity.trace;

import es.karmadev.api.spigot.entity.trace.result.HitPosition;

/**
 * Ray directions
 */
public enum RayDirection {
    /**
     * Up to down
     */
    UP_TO_DOWN(HitPosition.FEET),
    /**
     * Down to up
     */
    DOWN_TO_UP(HitPosition.HEAD);

    private final HitPosition target;

    /**
     * Create the ray direction
     *
     * @param position the target position
     */
    RayDirection(final HitPosition position) {
        this.target = position;
    }

    /**
     * Get if the direction has hit
     *
     * @param position the position
     * @return if the direction has hit the
     * target
     */
    public boolean hasHit(final HitPosition position) {
        return target.equals(position);
    }
}
