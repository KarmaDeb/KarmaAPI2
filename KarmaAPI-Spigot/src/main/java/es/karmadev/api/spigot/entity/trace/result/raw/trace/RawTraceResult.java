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
    private final Map<Location, Location[]> traces = new LinkedHashMap<>();

    /**
     * Initialize the raw block result
     *
     * @param data the raw data
     */
    RawTraceResult(final Map<Location, HitPosition> data, final Map<Location, Location[]> traces) {
        data.keySet().forEach((key) -> this.data.put(key, data.get(key)));
        traces.keySet().forEach((key) -> this.traces.put(key, traces.get(key)));
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
     * Get the trace result trace
     *
     * @return the trace result trace
     */
    @Override
    public Map<Location, Location[]> trace() {
        return new LinkedHashMap<>(traces);
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
     * @param traces the traces
     * @return the raw block result
     */
    public static RawTraceResult fromMap(final Map<Location, HitPosition> data, final Map<Location, Location[]> traces) {
        return new RawTraceResult(data, traces);
    }
}
