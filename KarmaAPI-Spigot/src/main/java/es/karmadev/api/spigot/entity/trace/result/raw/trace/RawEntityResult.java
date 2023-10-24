package es.karmadev.api.spigot.entity.trace.result.raw.trace;

import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import es.karmadev.api.spigot.entity.trace.result.raw.RawTraceResult;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raw entity raytrace result
 */
public class RawEntityResult implements RawTraceResult<UUID> {

    private final Map<UUID, HitPosition> data = new LinkedHashMap<>();
    private final Map<UUID, Location[]> traces = new LinkedHashMap<>();

    /**
     * Initialize the raw entity result
     *
     * @param data the raw data
     */
    RawEntityResult(final Map<Entity, HitPosition> data, final Map<Entity, Location[]> traces) {
        data.keySet().forEach((key) -> this.data.put(key.getUniqueId(), data.get(key)));
        traces.keySet().forEach((key) -> this.traces.put(key.getUniqueId(), traces.get(key)));
    }

    /**
     * Copy the raw data to the specified
     * map
     *
     * @param other the other map
     */
    @Override
    public void copyTo(final Map<UUID, HitPosition> other) {
        other.putAll(data);
    }

    /**
     * Get the trace result trace
     *
     * @return the trace result trace
     */
    @Override
    public Map<UUID, Location[]> trace() {
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
    public UUID[] keys() {
        return data.keySet().toArray(new UUID[0]);
    }

    /**
     * Create a raw entity result
     * from the map
     *
     * @param data the data
     * @param traces the traces
     * @return the raw entity result
     */
    public static RawEntityResult fromMap(final Map<Entity, HitPosition> data, final Map<Entity, Location[]> traces) {
        return new RawEntityResult(data, traces);
    }
}
