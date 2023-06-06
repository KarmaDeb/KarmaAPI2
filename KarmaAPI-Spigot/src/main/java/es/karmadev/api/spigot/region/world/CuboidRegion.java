package es.karmadev.api.spigot.region.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * KarmaAPI cuboid region
 */
public interface CuboidRegion extends Region {

    /**
     * Get the region top bound
     *
     * @return the top bound
     */
    Location boundTop();

    /**
     * Get the region bottom bound
     *
     * @return the bottom bound
     */
    Location boundBottom();

    /**
     * Check if the entity is inside
     * the region
     *
     * @param entity the entity
     * @return if the entity is inside
     */
    default boolean isInside(final Entity entity) {
        Location location = entity.getLocation();
        return isInside(location);
    }

    /**
     * Check if the block is inside
     * the region
     *
     * @param block the block
     * @return if the block is inside
     */
    default boolean isInside(final Block block) {
        Location location = block.getLocation();
        return isInside(location);
    }

    /**
     * Check if the location is inside
     * the region
     *
     * @param location the location
     * @return if the location is inside
     */
    default boolean isInside(final Location location) {
        if (location == null) return false;
        World world = location.getWorld();
        World regionWorld = getWorld();

        if (world == null || regionWorld == null) return false;
        if (!world.equals(regionWorld)) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return !isOutOfBounds(x, y, z);
    }

    /**
     * Calculate the distance between the
     * entity and the nearest wall
     *
     * @param entity the entity
     * @return the distance between the region
     * and the entity
     */
    default double distance(final Entity entity) {
        Location location = entity.getLocation();
        return distance(location);
    }

    /**
     * Calculate the distance between the
     * block and the nearest wall
     *
     * @param block the block
     * @return the distance between the region
     * and the block
     */
    default double distance(final Block block) {
        Location location = block.getLocation();
        return distance(location);
    }

    /**
     * Calculate the distance between the
     * location and the nearest wall
     *
     * @param location the location
     * @return the distance between the region
     * and the location
     */
    default double distance(final Location location) {
        if (location == null) return 0d;
        World world = location.getWorld();
        World regionWorld = getWorld();

        if (world == null || regionWorld == null) return 0d;
        if (!world.equals(regionWorld)) return 0d;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return distance(x, y, z);
    }
}
