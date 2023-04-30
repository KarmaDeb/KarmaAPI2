package es.karmadev.api.spigot.entity.trace.result.raw;

import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import es.karmadev.api.spigot.entity.trace.result.raw.trace.RawBlockResult;
import es.karmadev.api.spigot.entity.trace.result.raw.trace.RawEntityResult;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raw trace result builder
 */
public class RawTraceBuilder<T> {

    private final int type;
    private final Map<T, HitPosition> data = new ConcurrentHashMap<>();

    /**
     * Initialize the ray trace result
     *
     * @param type the builder type
     */
    RawTraceBuilder(final int type) {
        this.type = type;
    }

    /**
     * Assign the data
     *
     * @param key the data key
     * @param value the data value
     */
    public void assign(final T key, final HitPosition value) {
        data.put(key, value);
    }

    /**
     * Build the trace result
     *
     * @return the trace result
     */
    @SuppressWarnings("unchecked")
    public <RType> RawTraceResult<RType> build() {
        switch (type) {
            case 0:
                Object entityResult = RawEntityResult.fromMap((Map<Entity, HitPosition>) data);
                return (RawTraceResult<RType>) entityResult;
            case 1:
                Object blockResult = RawBlockResult.fromMap((Map<Block, HitPosition>) data);
                return (RawTraceResult<RType>) blockResult;
            case 2:
            default:
                Object traceResult = es.karmadev.api.spigot.entity.trace.result.raw.trace.RawTraceResult.fromMap((Map<Location, HitPosition>) data);
                return (RawTraceResult<RType>) traceResult;
        }
    }

    /**
     * Create a new raw entity result builder
     *
     * @return the raw trace builder
     */
    public static RawTraceBuilder<Entity> entityBuilder() {
        return new RawTraceBuilder<>(0);
    }

    /**
     * Create a new raw block result builder
     *
     * @return the raw trace builder
     */
    public static RawTraceBuilder<Block> blockBuilder() {
        return new RawTraceBuilder<>(1);
    }

    /**
     * Create a new raw trace result builder
     *
     * @return the raw trace builder
     */
    public static RawTraceBuilder<Location> traceBuilder() {
        return new RawTraceBuilder<>(2);
    }
}
