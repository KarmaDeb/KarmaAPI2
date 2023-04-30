package es.karmadev.api.spigot.entity.trace;

import es.karmadev.api.schedule.task.completable.TaskCompletor;
import es.karmadev.api.spigot.entity.trace.result.RayTraceResult;

/**
 * KarmaAPI raytrace. More precise but might
 * be more CPU hungry
 */
public interface PointRayTrace {

    /**
     * Set the raytrace precision
     *
     * @param value the precision
     */
    void setPrecision(final double value);

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
