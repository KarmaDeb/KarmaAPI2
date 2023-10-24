package es.karmadev.api.spigot.entity.trace.result.raw;

import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import org.bukkit.Location;

import java.util.Map;

/**
 * Raw trace result
 *
 * @param <T> the result type
 */
public interface RawTraceResult<T> {

    /**
     * Copy the raw data to the specified
     * map
     *
     * @param other the other map
     */
    void copyTo(final Map<T, HitPosition> other);

    /**
     * Get the trace result trace
     *
     * @return the trace result trace
     */
    Map<T, Location[]> trace();

    /**
     * Get the trace size
     *
     * @return the trace size
     */
    int size();

    /**
     * Get the trace keys
     *
     * @return the trace keys
     */
    T[] keys();
}
