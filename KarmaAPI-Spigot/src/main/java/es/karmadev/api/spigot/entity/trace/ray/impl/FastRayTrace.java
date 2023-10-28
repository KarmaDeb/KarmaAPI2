package es.karmadev.api.spigot.entity.trace.ray.impl;

import es.karmadev.api.reflection.entity.NMSAxisAlignedBBReflection;
import es.karmadev.api.reflection.entity.NMSEntityReflection;
import com.google.common.util.concurrent.AtomicDouble;
import es.karmadev.api.array.ArrayUtils;
import es.karmadev.api.spigot.entity.trace.RayDirection;
import es.karmadev.api.spigot.entity.trace.TraceOption;
import es.karmadev.api.spigot.entity.trace.event.RayTraceCollideEvent;
import es.karmadev.api.spigot.entity.trace.ray.RayTrace;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FastRayTrace is the fastest but least accurate
 * ray trace, while it does it best to keep its precision,
 * it won't be as accurate as the other ray traces
 */
@SuppressWarnings("unused")
public class FastRayTrace implements RayTrace {

    private RayDirection direction = RayDirection.DOWN_TO_UP;
    private Vector vector;

    private final Set<UUID> entityFilter = ConcurrentHashMap.newKeySet();
    private final Set<EntityType> entityTypeFilter = ConcurrentHashMap.newKeySet();
    private final Set<Location> blockFilter = ConcurrentHashMap.newKeySet();
    private final Set<Material> blockTypeFilter = ConcurrentHashMap.newKeySet();

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
    private FastRayTrace(final Location source, final Location target) {
        this.source = source.clone();
        this.target = target.clone(); //We want to always use a cloned version of the location
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
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setPrecision(final double value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("FastRayTrace does not support precision");
    }

    /**
     * Set the raytrace bounding box tolerance
     *
     * @param value the tolerance
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setTolerance(final double value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("FastRayTrace does not support tolerance");
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
     * Set the raytrace vector override, this
     * override will define in which direction to
     * face the ray trace.
     * <p>
     * NOT THE SAME AS {@link #setDirection(RayDirection)}
     *
     * @param vector the direction vector override
     */
    @Override
    public void setDirection(final Vector vector) {
        this.vector = vector;
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
        final Location start = source.clone().add(sourceOffsets[0], sourceOffsets[1], sourceOffsets[2]);
        final Location end = target.clone().add(targetOffsets[0], targetOffsets[1], targetOffsets[2]);
        final Vector traceDirection = (vector != null ? vector : end.toVector().subtract(start.toVector()).normalize());

        final Set<Block> checked = new HashSet<>();

        final RawTraceBuilder<Block> blockResultBuilder = RawTraceBuilder.blockBuilder();
        final RawTraceBuilder<Entity> entityResultBuilder = RawTraceBuilder.entityBuilder();
        final RawTraceBuilder<Location> traceResultBuilder = RawTraceBuilder.traceBuilder();

        final AtomicBoolean preventExecution = new AtomicBoolean(false);
        final AtomicReference<HitPosition> currentPosition = new AtomicReference<>(HitPosition.FEET);

        final AtomicDouble yOffset = new AtomicDouble(0.1);
        final AtomicDouble forcedX = new AtomicDouble(Double.MIN_VALUE);
        final AtomicDouble forcedZ = new AtomicDouble(Double.MIN_VALUE);

        if (this.direction.equals(RayDirection.UP_TO_DOWN)) {
            currentPosition.set(HitPosition.HEAD);
            //yOffset.set(1.25);
        }

        final World world = start.getWorld();
        assert world != null;

        final List<Location> playedPositions = new ArrayList<>();
        double middleX = (source.getX() + target.getX()) / 2;
        double middleY = (source.getY() + target.getY()) / 2;
        double middleZ = (source.getZ() + target.getZ()) / 2;

        final Location center = new Location(world, middleX, middleY, middleZ);
        final Collection<Entity> preCachedNearby = world.getNearbyEntities(center, maxDistance, maxDistance, maxDistance); //This should avoid performance issues

        for (double i = 1; i < maxDistance; i += 1) {
            traceDirection.multiply(i);
            Location locationIteration = start.add(traceDirection).clone()
                    .subtract(0.5, 0.5, 0.5); //Center

            if (preventExecution.get() || cancelled.get()) break;
            Location location = locationIteration.clone().add(0, yOffset.get(), 0);

            if (forcedX.get() != Double.MIN_VALUE && forcedZ.get() != Double.MIN_VALUE) {
                location.setX(forcedX.get());
                location.setZ(forcedZ.get());

                forcedX.set(Double.MIN_VALUE);
                forcedZ.set(Double.MIN_VALUE);
            }

            Location[] positions = playedPositions.toArray(new Location[0]);
            traceResultBuilder.assign(location, positions);
            traceResultBuilder.assign(location, currentPosition.get());
            Block block = location.getBlock();

            Material blockType = block.getType();

            boolean isSolid = blockType.isSolid();
            boolean isAir = blockType.name().endsWith("AIR"); //Adds support for "CAVE_AIR"

            boolean stop = false;
            if (!checked.contains(block)) {
                BlockData data = block.getBlockData();
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
                    blockResultBuilder.assign(block, positions);

                    RayTraceCollideEvent event = new RayTraceCollideEvent(null, block, currentPosition.get());
                    Bukkit.getServer().getPluginManager().callEvent(event);
                }

                if (direction.hasHit(currentPosition.get()) || ArrayUtils.containsAny(options, TraceOption.STOP_ON_FIRST_HIT)) {
                    stop = (ArrayUtils.containsAny(options, TraceOption.STOP_ON_HIT, TraceOption.STOP_ON_BLOCK) && !isAir ||
                            ArrayUtils.containsAny(options, TraceOption.STOP_ON_SOLID_HIT, TraceOption.STOP_ON_SOLID_BLOCK)) && isSolid;
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

                    if (ArrayUtils.containsAny(options, TraceOption.ROLLBACK_ON_HIT)) {
                        forcedX.set(location.getX());
                        forcedZ.set(location.getZ());
                    }
                }
            }

            if (preventExecution.get()) {
                break;
            }

            for (Entity entity : preCachedNearby) {
                if (entity.getLocation().distance(location) >= 3) continue;

                if (!entityFilter.contains(entity.getUniqueId()) &&
                        !entityTypeFilter.contains(entity.getType())) {
                    NMSEntityReflection reflectedEntity = new NMSEntityReflection(entity);

                    NMSAxisAlignedBBReflection virtualBounding = new NMSAxisAlignedBBReflection(location.clone()
                            .subtract(2, 2, 2), location.clone()
                            .add(2, 2, 2));
                    NMSAxisAlignedBBReflection bounding = reflectedEntity.getBoundingBox();

                    Location entityLocation = entity.getLocation();
                    double feeY = entityLocation.getY() + 0.25; //Legs center
                    double midY = feeY + (entity.getHeight() / 2); //Torso
                    double maxY = feeY + entity.getHeight() - 0.25; //Head center
                    double locationY = location.getY();

                    if (bounding.doesCollide(virtualBounding)) {
                        entityFilter.add(entity.getUniqueId());
                        double feetDiff = Math.abs(locationY - feeY);
                        double torsoDiff = Math.abs(locationY - midY);
                        double headDiff = Math.abs(locationY - maxY);

                        boolean closestToFeet = feetDiff < torsoDiff && feetDiff < headDiff;
                        boolean closestToTorso = torsoDiff < feetDiff && torsoDiff < headDiff;

                        HitPosition position = (closestToFeet ? HitPosition.FEET : (closestToTorso ? HitPosition.TORSO : HitPosition.HEAD));

                        entityResultBuilder.assign(entity, positions);
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

            playedPositions.add(location);

            start.subtract(traceDirection);
            traceDirection.normalize();
        }

        return new RayTraceResult(world,
                entityResultBuilder.build(),
                blockResultBuilder.build(),
                traceResultBuilder.build());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Entity source, final Entity target) {
        return new FastRayTrace(source.getLocation(), target.getLocation());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Entity source, final Location target) {
        return new FastRayTrace(source.getLocation(), target.clone());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Location source, final Entity target) {
        return new FastRayTrace(source.clone(), target.getLocation());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Location source, final Location target) {
        return new FastRayTrace(source.clone(), target.clone());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Entity source, final Block target) {
        return new FastRayTrace(source.getLocation(), target.getLocation());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Block source, final Entity target) {
        return new FastRayTrace(source.getLocation(), target.getLocation());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Location source, final Block target) {
        return new FastRayTrace(source, target.getLocation());
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Block source, final Location target) {
        return new FastRayTrace(source.getLocation(), target);
    }

    /**
     * Create a raytrace
     *
     * @param source the start point
     * @param target the target point
     * @return the ray trace
     */
    public static FastRayTrace createRayTrace(final Block source, final Block target) {
        return new FastRayTrace(source.getLocation(), target.getLocation());
    }
}
