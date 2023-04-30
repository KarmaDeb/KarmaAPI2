package es.karmadev.api.spigot.entity.trace.result.raw.trace;

import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import es.karmadev.api.spigot.entity.trace.result.raw.RawTraceResult;
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

    /**
     * Initialize the raw entity result
     *
     * @param data the raw data
     */
    RawEntityResult(final Map<Entity, HitPosition> data) {
        data.keySet().forEach((key) -> this.data.put(key.getUniqueId(), data.get(key)));
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
     * @return the raw entity result
     */
    public static RawEntityResult fromMap(final Map<Entity, HitPosition> data) {
        return new RawEntityResult(data);
    }
}
