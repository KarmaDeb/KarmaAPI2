package es.karmadev.api.spigot.entity.trace.result;

import es.karmadev.api.spigot.entity.trace.result.raw.RawTraceResult;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ray trace result
 */
public class RayTraceResult {

    private final Reference<World> world;
    private final Map<UUID, HitPosition> entities = new ConcurrentHashMap<>();
    private final Map<Location, HitPosition> blocks = new ConcurrentHashMap<>();
    private final Map<UUID, Location[]> entityTrace = new ConcurrentHashMap<>();
    private final Map<Location, Location[]> blockTrace = new ConcurrentHashMap<>();
    private final Location[] trace;

    /**
     * Initialize the ray trace result
     *
     * @param world the ray trace world
     * @param entities the entities hit
     * @param blocks the hit blocks
     */
    public RayTraceResult(final World world,
                          final RawTraceResult<UUID> entities,
                          final RawTraceResult<Location> blocks,
                          final RawTraceResult<Location> trace) {

        this.world = new WeakReference<>(world);
        entities.copyTo(this.entities);
        blocks.copyTo(this.blocks);
        this.trace = trace.keys();

        entityTrace.putAll(entities.trace());
        blockTrace.putAll(blocks.trace());
    }

    /**
     * Get the traced entities
     *
     * @return the traced entities
     */
    public Entity[] entities() {
        World world = this.world.get();
        if (world != null) {
            List<Entity> entities = new ArrayList<>();
            world.getEntities().forEach((entity) -> {
                if (this.entities.containsKey(entity.getUniqueId())) {
                    entities.add(entity);
                }
            });

            return entities.toArray(new Entity[0]);
        }

        return new Entity[0];
    }

    /**
     * Get the traced blocks
     *
     * @return the traced blocks
     */
    public Block[] blocks() {
        World world = this.world.get();
        if (world != null) {
            List<Block> blocks = new ArrayList<>();
            this.blocks.keySet().forEach((location) -> blocks.add(location.getBlock()));

            return blocks.toArray(new Block[0]);
        }

        return new Block[0];
    }

    /**
     * Get the trace
     *
     * @return the trace
     */
    public Location[] trace() {
        return trace.clone();
    }

    /**
     * Get the trace from the origin
     * to the entity
     *
     * @param entity the entity
     * @param straight if the trace should use the
     *                 target Y
     * @return the trace-to entity
     */
    public Location[] trace(final Entity entity, final boolean straight) {
        if (entity == null || !entityTrace.containsKey(entity.getUniqueId())) return new Location[0];
        Location[] trace = entityTrace.get(entity.getUniqueId());

        if (trace.length == 0) return new Location[0];

        double yModifier = Double.MIN_VALUE;
        if (straight) {
            yModifier = trace[trace.length - 1].getY();
        }

        List<Location> traceBuilder = new ArrayList<>();
        for (Location position : trace) {
            Location clone = position.clone();
            clone.setY(Math.max(yModifier, clone.getY()));

            traceBuilder.add(clone);
        }

        return traceBuilder.toArray(new Location[0]);
    }

    /**
     * Get the trace from the origin
     * to the block
     *
     * @param block the block
     * @param straight if the trace should use the
     *                 target Y
     * @return the trace-to block
     */
    public Location[] trace(final Block block, final boolean straight) {
        if (block == null || !blockTrace.containsKey(block.getLocation())) return new Location[0];
        Location[] trace = blockTrace.get(block.getLocation());

        if (trace.length == 0) return new Location[0];

        double yModifier = Double.MIN_VALUE;
        if (straight) {
            yModifier = trace[trace.length - 1].getY();
        }

        List<Location> traceBuilder = new ArrayList<>();
        for (Location position : trace) {
            Location clone = position.clone();
            clone.setY(Math.max(yModifier, clone.getY()));

            traceBuilder.add(clone);
        }

        return traceBuilder.toArray(new Location[0]);
    }

    /**
     * Get the trace from the origin
     * to the location
     *
     * @param location the location
     * @param straight if the trace should use the
     *                 target Y
     * @return the trace-to location
     */
    public Location[] trace(final Location location, final boolean straight) {
        if (location == null || !blockTrace.containsKey(location)) return new Location[0];
        Location[] trace = blockTrace.get(location);

        if (trace.length == 0) return new Location[0];

        double yModifier = Double.MIN_VALUE;
        if (straight) {
            yModifier = trace[trace.length - 1].getY();
        }

        List<Location> traceBuilder = new ArrayList<>();
        for (Location position : trace) {
            Location clone = position.clone();
            clone.setY(Math.max(yModifier, clone.getY()));

            traceBuilder.add(clone);
        }

        return traceBuilder.toArray(new Location[0]);
    }

    /**
     * Get the hit position
     *
     * @param entity the entity to
     *               get hit position for
     * @return the hit position
     */
    public Optional<HitPosition> getHitPosition(final Entity entity) {
        return Optional.ofNullable(entities.getOrDefault(entity.getUniqueId(), null));
    }

    /**
     * Get the hit position
     *
     * @param block the block to
     *              get hit position for
     * @return the hit position
     */
    public Optional<HitPosition> getHitPosition(final Block block) {
        return Optional.ofNullable(blocks.getOrDefault(block.getLocation(), null));
    }

    /**
     * Get the hit position
     *
     * @param location the location to
     *                 get hit position for
     * @return the hit position
     */
    public Optional<HitPosition> getHitPosition(final Location location) {
        if (blocks.containsKey(location)) return Optional.ofNullable(blocks.getOrDefault(location, null));

        HitPosition position = null;
        World world = this.world.get();
        if (world != null) {
            for (Entity entity : world.getEntities()) {
                if (entity.getLocation().equals(location)) {
                    position = entities.getOrDefault(entity.getUniqueId(), null);
                }
            }
        }

        return Optional.ofNullable(position);
    }
}
