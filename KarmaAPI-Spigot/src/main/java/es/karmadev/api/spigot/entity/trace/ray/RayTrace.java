package es.karmadev.api.spigot.entity.trace.ray;

import es.karmadev.api.spigot.entity.trace.RayDirection;
import es.karmadev.api.spigot.entity.trace.TraceOption;
import es.karmadev.api.spigot.entity.trace.result.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * KarmaAPI raytrace. More precise but might
 * be more CPU hungry
 */
public interface RayTrace {

    /**
     * Set the raytrace precision
     *
     * @param value the precision
     */
    void setPrecision(final double value);

    /**
     * Set the raytrace bounding box tolerance
     *
     * @param value the tolerance
     */
    void setTolerance(final double value);

    /**
     * Set the raytrace direction
     *
     * @param direction the direction
     */
    void setDirection(final RayDirection direction);

    /**
     * Set the raytrace vector override, this
     * override will define in which direction to
     * face the ray trace. 
     * 
     * NOT THE SAME AS {@link #setDirection(RayDirection)}
     *
     * @param vector the direction vector override
     */
    void setDirection(final Vector vector);

    /**
     * Cancel the raytrace
     */
    void cancel();

    /**
     * Get if the raytrace is cancelled
     *
     * @return if the raytrace is cancelled
     */
    boolean isCancelled();

    /**
     * Start the ray trace
     *
     * @param options the ray trace options
     * @return the trace task
     */
    RayTraceResult trace(final TraceOption... options);

    /**
     * Start the ray trace
     *
     * @param maxDistance the max ray trace distance
     * @param options the ray trace options
     * @return the trace task
     */
    RayTraceResult trace(final double maxDistance, final TraceOption... options);
}
