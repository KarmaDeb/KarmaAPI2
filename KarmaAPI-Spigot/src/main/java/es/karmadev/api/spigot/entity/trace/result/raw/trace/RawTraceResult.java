package es.karmadev.api.spigot.entity.trace.result.raw.trace;

import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Raw trace raytrace result
 */
public class RawTraceResult implements es.karmadev.api.spigot.entity.trace.result.raw.RawTraceResult<Location> {

    private final Map<Location, HitPosition> data = new LinkedHashMap<>();

    /**
     * Initialize the raw block result
     *
     * @param data the raw data
     */
    RawTraceResult(final Map<Location, HitPosition> data) {
        data.keySet().forEach((key) -> this.data.put(key, data.get(key)));
    }

    /**
     * Copy the raw data to the specified
     * map
     *
     * @param other the other map
     */
    @Override
    public void copyTo(final Map<Location, HitPosition> other) {
        other.putAll(data);
    }

    /**
     * Get the trace size
     *
     * @return the trace size
     */
    @Override
    public int size() {
        return data.size();
    }

    /**
     * Get the trace keys
     *
     * @return the trace keys
     */
    @Override
    public Location[] keys() {
        return data.keySet().toArray(new Location[0]);
    }

    /**
     * Create a raw block result
     * from the map
     *
     * @param data the data
     * @return the raw block result
     */
    public static RawTraceResult fromMap(final Map<Location, HitPosition> data) {
        return new RawTraceResult(data);
    }
}
