package es.karmadev.api.spigot.entity.trace;

import com.google.common.util.concurrent.AtomicDouble;
import es.karmadev.api.array.ArrayUtils;
import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import es.karmadev.api.spigot.entity.trace.result.RayTraceResult;
import es.karmadev.api.spigot.entity.trace.result.raw.RawTraceBuilder;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * KarmaAPI ray trace implementation
 */
public class RayTrace implements PointRayTrace {

    private static Reference<Set<TraceCache>> cache = new WeakReference<>(Collections.newSetFromMap(new ConcurrentHashMap<>()));

    /**
     * The most precise value
     */
    public final static double HIGH_PRECISION = 0.1;
    /**
     * The default and preferred precision
     */
    public final static double MEDIUM_PRECISION = 0.5;
    /**
     * Useful when you have in mind precision and performance
     */
    public final static double LOW_PRECISION = 1;
    /**
     * Only recommended in very exclusive cases
     */
    public final static double FAST_PRECISION = 1.5;

    private final Set<UUID> entityFilter = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<EntityType> entityTypeFilter = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<Location> blockFilter = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<Material> blockTypeFilter = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private double precision = MEDIUM_PRECISION;
    private final Location source;
    private final Location target;
    private final double[] sourceOffsets = new double[]{0, 0, 0};
    private final double[] targetOffsets = new double[]{0, 0, 0};
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * Initialize the raytrace
     *
     * @param source the source raytrace
     * @param target the target raytrace
     */
    public RayTrace(final Entity source, final Location target) {
        this(source.getLocation(), target);
    }

    /**
     * Initialize the raytrace
     *
     * @param source the source raytrace
     * @param target the target raytrace
     */
    public RayTrace(final Location source, final Entity target) {
        this(source, target.getLocation());
    }

    /**
     * Initialize the raytrace
     *
     * @param source the source raytrace
     * @param target the target raytrace
     */
    public RayTrace(final Entity source, final Entity target) {
        this(source.getLocation(), target.getLocation());
    }

    /**
     * Initialize the raytrace
     *
     * @param source the source raytrace
     * @param target the target raytrace
     */
    public RayTrace(final Location source, final Location target) {
        this.source = source;
        this.target = target;

        if (cache.get() == null) cache = new WeakReference<>(Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    /**
     * Filter the entities
     *
     * @param entities the entities to filter
     */
    public void filterEntity(final Entity... entities) {
        for (Entity entity : entities) entityFilter.add(entity.getUniqueId());
    }

    /**
     * Filter the entities
     *
     * @param types the entity types
     */
    public void filterEntity(final EntityType... types) {
        entityTypeFilter.addAll(Arrays.asList(types));
    }

    /**
     * Remove the entity filter
     *
     * @param entities the entities to remove
     *                 from filter
     */
    public void removeEntityFilter(final Entity... entities) {
        for (Entity entity : entities) entityFilter.remove(entity.getUniqueId());
    }

    /**
     * Remove the type filter
     *
     * @param types the entity types to remove
     *              from filter
     */
    public void removeEntityFilter(final EntityType... types) {
        Arrays.asList(types).forEach(entityTypeFilter::remove);
    }

    /**
     * Filter the blocks
     *
     * @param blocks the blocks to filter
     */
    public void filterBlock(final Block... blocks) {
        for (Block block : blocks) blockFilter.add(block.getLocation());
    }

    /**
     * Filter the materials
     *
     * @param materials the materials
     */
    public void filterBlock(final Material... materials) {
        blockTypeFilter.addAll(Arrays.asList(materials));
    }

    /**
     * Remove the block filter
     *
     * @param blocks the blocks to remove
     *               from filter
     */
    public void removeBlockFilter(final Block... blocks) {
        for (Block block : blocks) blockFilter.remove(block.getLocation());
    }

    /**
     * Remove the material filter
     *
     * @param materials the materials to remove
     *                  from filter
     */
    public void removeBlockFilter(final Material... materials) {
        Arrays.asList(materials).forEach(blockTypeFilter::remove);
    }

    /**
     * Set the raytrace precision
     *
     * @param value the precision
     */
    @Override
    public void setPrecision(final double value) {
        this.precision = value;
    }

    /**
     * Cancel the raytrace
     */
    @Override
    public void cancel() {
        cancelled.set(true);
    }

    /**
     * Get if the raytrace is cancelled
     *
     * @return if the raytrace is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Set the source position offsets
     *
     * @param xOffset the source position x offset
     * @param yOffset the source position y offset
     * @param zOffset the source position z offset
     */
    public void setSourceOffsets(final double xOffset, final double yOffset, final double zOffset) {
        sourceOffsets[0] = xOffset;
        sourceOffsets[1] = yOffset;
        sourceOffsets[2] = zOffset;
    }

    /**
     * Set the target position offsets
     *
     * @param xOffset the target position x offset
     * @param yOffset the target position y offset
     * @param zOffset the target position z offset
     */
    public void setTargetOffsets(final double xOffset, final double yOffset, final double zOffset) {
        targetOffsets[0] = xOffset;
        targetOffsets[1] = yOffset;
        targetOffsets[2] = zOffset;
    }

    /**
     * Get the source location
     *
     * @return the source location
     */
    public Location getSource() {
        return source.clone().add(sourceOffsets[0], sourceOffsets[1], sourceOffsets[2]);
    }

    /**
     * Get the target location
     *
     * @return the target location
     */
    public Location getTarget() {
        return target.clone().add(targetOffsets[0], targetOffsets[1], targetOffsets[2]);
    }

    /**
     * Start the ray trace
     *
     * @param options the ray trace options
     * @return the trace task
     */
    @Override
    public RayTraceResult trace(final TraceOption... options) {
        Location start = source.clone().add(sourceOffsets[0], sourceOffsets[1], sourceOffsets[2]);
        Location end = target.clone().add(targetOffsets[0], targetOffsets[1], targetOffsets[2]);

        double iterations = start.distance(end);
        return trace(iterations, options);
    }

    /**
     * Start the ray trace
     *
     * @param maxDistance the max ray trace distance
     * @param options     the ray trace options
     * @return the trace task
     */
    @Override
    public RayTraceResult trace(final double maxDistance, final TraceOption... options) {
        Set<TraceCache> caches = cache.get();
        List<Location> iterations = new ArrayList<>();

        if (caches != null) {
            TraceCache cacheItem = null;
            for (TraceCache cache : caches) {
                if ((cache.point1.equals(source) || cache.point1.equals(target)) && (cache.point2.equals(source) || cache.point2.equals(target))) {
                    cacheItem = cache;
                }
            }

            if (cacheItem != null) {
                if (cacheItem.hasCache(maxDistance, precision)) {
                    TracePointCache pointCache = cacheItem.getCache(maxDistance, precision);
                    for (Location location : pointCache.locations) iterations.add(location.clone());
                }
            }
        }

        if (iterations.isEmpty()) {
            Location start = source.clone().add(sourceOffsets[0], sourceOffsets[1], sourceOffsets[2]);
            Location end = target.clone().add(targetOffsets[0], targetOffsets[1], targetOffsets[2]);
            World world = start.getWorld();
            assert world != null;

            Vector direction = end.toVector().subtract(start.toVector()).normalize();
            for (double i = precision; i < maxDistance; i += precision) {
                direction.multiply(i);
                start.add(direction).clone();

                iterations.add(start.clone());

                start.subtract(direction);
                direction.normalize();
            }
        }

        RawTraceBuilder<Block> blockResultBuilder = RawTraceBuilder.blockBuilder();
        RawTraceBuilder<Entity> entityResultBuilder = RawTraceBuilder.entityBuilder();
        RawTraceBuilder<Location> traceResultBuilder = RawTraceBuilder.traceBuilder();

        AtomicBoolean preventExecution = new AtomicBoolean(false);
        AtomicReference<HitPosition> currentPosition = new AtomicReference<>(HitPosition.FEET);
        AtomicDouble yOffset = new AtomicDouble(0.1);

        World world = null;
        for (Location locationIteration : iterations) {
            if (preventExecution.get() || cancelled.get()) break;
            Location location = locationIteration.add(0, yOffset.get(), 0);
            world = location.getWorld();

            traceResultBuilder.assign(location, currentPosition.get());
            Block block = location.getBlock();
            Material blockType = block.getType();

            boolean isSolid = blockType.isSolid();
            boolean isAir = blockType.name().endsWith("AIR"); //Adds support for "CAVE_AIR"

            if (blockType.name().contains("SLAB")) {
                Slab slab = (Slab) block.getBlockData();
                double y = location.getY();
                BigDecimal decimal = BigDecimal.valueOf(y).subtract(BigDecimal.valueOf(Math.floor(y)));

                boolean isLow = decimal.compareTo(new BigDecimal("0.5")) <= 0;
                //System.out.printf("%s: %b%n", y, isLow); Debug lol
                if (slab.getType().equals((isLow ? Slab.Type.TOP : Slab.Type.BOTTOM))) {
                    isSolid = false;
                    isAir = true;
                }
            }
            if (blockType.name().contains("TRAPDOOR")) {
                TrapDoor trapDoor = (TrapDoor) block.getBlockData();
                if (!trapDoor.isOpen()) {
                    isSolid = false;
                    isAir = true;
                }
            } else {
                if (blockType.name().contains("DOOR")) {
                    Door door = (Door) block.getBlockData();
                    if (door.isOpen()) {
                        isSolid = false;
                        isAir = true;
                    }
                }
            }
            //TODO: Use switch cases or a list instead of if/else
            //TODO: Fix pressure plates

            if (blockFilter.contains(block.getLocation()) || blockTypeFilter.contains(blockType)) {
                isSolid = false;
                isAir = true;
                //We parse the block as air
            }

            if (!isAir) blockResultBuilder.assign(block, currentPosition.get());

            boolean stop =
                    (ArrayUtils.containsAny(options, TraceOption.STOP_ON_HIT, TraceOption.STOP_ON_BLOCK) && !isAir) ||
                            (ArrayUtils.containsAny(options, TraceOption.STOP_ON_SOLID_HIT, TraceOption.STOP_ON_SOLID_BLOCK) && isSolid);

            if (stop) {
                preventExecution.set(true);
            } else {
                if (isSolid) {
                    switch (currentPosition.get()) {
                        case HEAD:
                            yOffset.set(yOffset.get() + 0.25);
                            break;
                        case TORSO:
                            currentPosition.set(HitPosition.HEAD);
                            yOffset.set(1);
                            break;
                        case FEET:
                        default:
                            currentPosition.set(HitPosition.TORSO);
                            yOffset.set(0.75);
                            break;
                    }
                }
            }

            if (preventExecution.get()) {
                break;
            }

            Chunk chunk = block.getChunk();
            for (Entity entity : chunk.getEntities()) {
                if (!entityFilter.contains(entity.getUniqueId()) && entityTypeFilter.contains(entity.getType())) {
                    double distance = entity.getLocation().distance(location);
                    if (distance <= precision) {
                        if (ArrayUtils.containsAny(options, TraceOption.STOP_ON_ENTITY)) {
                            entityResultBuilder.assign(entity, currentPosition.get());
                            preventExecution.set(true);
                        }
                    }
                }
            }
        }

        return new RayTraceResult(world,
                entityResultBuilder.build(),
                blockResultBuilder.build(),
                traceResultBuilder.build());
    }
}
