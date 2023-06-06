package es.karmadev.api.spigot.entity.trace.ray;

import com.github.yeetmanlord.reflection_api.entity.NMSAxisAlignedBBReflection;
import com.github.yeetmanlord.reflection_api.entity.NMSEntityReflection;
import com.google.common.util.concurrent.AtomicDouble;
import es.karmadev.api.array.ArrayUtils;
import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.spigot.entity.trace.RayDirection;
import es.karmadev.api.spigot.entity.trace.TraceOption;
import es.karmadev.api.spigot.entity.trace.event.RayTraceCollideEvent;
import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import es.karmadev.api.spigot.entity.trace.result.RayTraceResult;
import es.karmadev.api.spigot.entity.trace.result.raw.RawTraceBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Slab;
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

    private RayDirection direction = RayDirection.DOWN_TO_UP;
    private final Set<UUID> entityFilter = ConcurrentHashMap.newKeySet();
    private final Set<EntityType> entityTypeFilter = ConcurrentHashMap.newKeySet();
    private final Set<Location> blockFilter = ConcurrentHashMap.newKeySet();
    private final Set<Material> blockTypeFilter = ConcurrentHashMap.newKeySet();
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
     * Set the raytrace direction
     *
     * @param direction the direction
     */
    @Override
    public void setDirection(final RayDirection direction) {
        this.direction = direction;
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

        Map<Integer, Entity[]> locationChunk = new LinkedHashMap<>();
        if (caches != null) {
            ConsoleColor.GREEN.print("Using cache");

            TraceCache cacheItem = null;
            for (TraceCache cache : caches) {
                if ((cache.point1.equals(source) || cache.point1.equals(target)) && (cache.point2.equals(source) || cache.point2.equals(target))) {
                    cacheItem = cache;
                }
            }

            if (cacheItem != null) {
                if (cacheItem.hasCache(maxDistance, precision)) {
                    TracePointCache pointCache = cacheItem.getCache(maxDistance, precision);
                    int index = 0;
                    for (Location location : pointCache.locations) {
                        iterations.add(location.clone());
                        locationChunk.put(index++, location.getChunk().getEntities());
                    }
                }
            }
        }

        if (iterations.isEmpty()) {
            Location start = source.clone().add(sourceOffsets[0], sourceOffsets[1], sourceOffsets[2]);
            Location end = target.clone().add(targetOffsets[0], targetOffsets[1], targetOffsets[2]);
            World world = start.getWorld();
            assert world != null;

            Vector direction = end.toVector().subtract(start.toVector()).normalize();
            int index = 0;
            for (double i = precision; i < maxDistance; i += precision) {
                direction.multiply(i);
                start.add(direction).clone();

                iterations.add(start.clone());
                locationChunk.put(index++, start.getChunk().getEntities());

                start.subtract(direction);
                direction.normalize();
            }

            if (caches != null) {
                TraceCache tmpCache = new TraceCache(source.clone(), target.clone());
                tmpCache.definePoints(maxDistance, precision, iterations.toArray(new Location[0]));

                caches.add(tmpCache);
            }
        }

        RawTraceBuilder<Block> blockResultBuilder = RawTraceBuilder.blockBuilder();
        RawTraceBuilder<Entity> entityResultBuilder = RawTraceBuilder.entityBuilder();
        RawTraceBuilder<Location> traceResultBuilder = RawTraceBuilder.traceBuilder();

        AtomicBoolean preventExecution = new AtomicBoolean(false);
        AtomicReference<HitPosition> currentPosition = new AtomicReference<>(HitPosition.FEET);
        AtomicDouble yOffset = new AtomicDouble(0.1);

        if (direction.equals(RayDirection.UP_TO_DOWN)) {
            currentPosition.set(HitPosition.HEAD);
            yOffset.set(1.25);
        }

        World world = iterations.get(0).getWorld();
        Set<Block> checked = new HashSet<>();

        int index = 0;
        for (Location locationIteration : iterations) {
            if (preventExecution.get() || cancelled.get()) break;
            Location location = locationIteration.clone().add(0, yOffset.get(), 0);
            world = location.getWorld();

            traceResultBuilder.assign(location, currentPosition.get());
            Block block = location.getBlock();

            Material blockType = block.getType();

            boolean isSolid = blockType.isSolid();
            boolean isAir = blockType.name().endsWith("AIR"); //Adds support for "CAVE_AIR"

            boolean stop = false;
            if (!checked.contains(block)) {
                BlockData data = block.getBlockData();
                if (data instanceof Slab) {
                    Slab slab = (Slab) data;
                    double y = location.getY();
                    BigDecimal decimal = BigDecimal.valueOf(y).subtract(BigDecimal.valueOf(Math.floor(y)));

                    boolean isLow = decimal.compareTo(new BigDecimal("0.5")) < 0;
                    if (slab.getType().equals((isLow ? Slab.Type.TOP : Slab.Type.BOTTOM))) {
                        isSolid = false;
                        isAir = true;
                    }
                }
                if (data instanceof Openable) {
                    isSolid = false;
                    isAir = true;
                }

                if (blockType.name().contains("PRESSURE") || blockType.name().contains("GLASS")) {
                    isSolid = false;
                    isAir = true;
                }

                if (blockFilter.contains(block.getLocation()) || blockTypeFilter.contains(blockType)) {
                    isSolid = false;
                    isAir = true;
                    //We parse the block as air
                }

                if (!isAir) {
                    blockResultBuilder.assign(block, currentPosition.get());
                    RayTraceCollideEvent event = new RayTraceCollideEvent(null, block, currentPosition.get());
                    Bukkit.getServer().getPluginManager().callEvent(event);
                }
                if (direction.hasHit(currentPosition.get())) {
                    stop =
                            (ArrayUtils.containsAny(options, TraceOption.STOP_ON_HIT, TraceOption.STOP_ON_BLOCK) && !isAir) ||
                                    (ArrayUtils.containsAny(options, TraceOption.STOP_ON_SOLID_HIT, TraceOption.STOP_ON_SOLID_BLOCK) && isSolid);
                }
            }

            if (blockType.name().endsWith("AIR")) checked.add(block);
            if (stop) {
                preventExecution.set(true);
            } else {
                if (isSolid) {
                    switch (currentPosition.get()) {
                        case HEAD:
                            if (direction.equals(RayDirection.DOWN_TO_UP)) {
                                yOffset.set(yOffset.get() + 0.25);
                            } else {
                                currentPosition.set(HitPosition.TORSO);
                                yOffset.set(0.75);
                            }
                            break;
                        case TORSO:
                            if (direction.equals(RayDirection.DOWN_TO_UP)) {
                                currentPosition.set(HitPosition.HEAD);
                                yOffset.set(1);
                            } else {
                                currentPosition.set(HitPosition.FEET);
                                yOffset.set(0.5);
                            }
                            break;
                        case FEET:
                        default:
                            if (direction.equals(RayDirection.DOWN_TO_UP)) {
                                currentPosition.set(HitPosition.TORSO);
                                yOffset.set(0.75);
                            } else {
                                yOffset.set(yOffset.get() - 0.25);
                            }
                            break;
                    }
                }
            }

            if (preventExecution.get()) {
                break;
            }

            Entity[] entities = locationChunk.get(index++);
            for (Entity entity : entities) {
                if (!entityFilter.contains(entity.getUniqueId()) && !entityTypeFilter.contains(entity.getType())) {
                    NMSEntityReflection reflectedEntity = new NMSEntityReflection(entity);
                    NMSAxisAlignedBBReflection bounding = reflectedEntity.getBoundingBox();

                    Location entityLocation = entity.getLocation();
                    double feeY = entityLocation.getY() + 0.25; //Legs center
                    double midY = feeY + (entity.getHeight() / 2); //Torso
                    double maxY = feeY + entity.getHeight() - 0.25; //Head center
                    double locationY = location.getY();

                    if (bounding.isWithinBoundingBox(location.getX(), locationY, location.getZ())) {
                        entityFilter.add(entity.getUniqueId());
                        double feetDiff = Math.abs(locationY - feeY);
                        double torsoDiff = Math.abs(locationY - midY);
                        double headDiff = Math.abs(locationY - maxY);

                        boolean closestToFeet = feetDiff < torsoDiff && feetDiff < headDiff;
                        boolean closestToTorso = torsoDiff < feetDiff && torsoDiff < headDiff;
                        //boolean closestToHead = headDiff < feetDiff && headDiff < torsoDiff;

                        HitPosition position = (closestToFeet ? HitPosition.FEET : (closestToTorso ? HitPosition.TORSO : HitPosition.HEAD));

                        entityResultBuilder.assign(entity, position);
                        RayTraceCollideEvent event = new RayTraceCollideEvent(entity, null, position);
                        Bukkit.getServer().getPluginManager().callEvent(event);

                        if (ArrayUtils.containsAny(options, TraceOption.STOP_ON_ENTITY, TraceOption.STOP_ON_SOLID_HIT)) {
                            preventExecution.set(true);
                        }
                        break;
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
