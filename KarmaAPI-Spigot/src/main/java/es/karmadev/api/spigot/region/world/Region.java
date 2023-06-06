package es.karmadev.api.spigot.region.world;

import es.karmadev.api.spigot.region.world.part.RegionCeiling;
import es.karmadev.api.spigot.region.world.part.RegionFloor;
import es.karmadev.api.spigot.region.world.part.RegionWall;
import es.karmadev.api.spigot.region.world.part.WallPart;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

/**
 * KarmaAPI region
 */
public interface Region extends Serializable {

    /**
     * Get the region name
     *
     * @return the region name
     */
    String getName();

    /**
     * Get the region universal unique
     * identifier
     *
     * @return the region ID
     */
    UUID getUUID();

    /**
     * Get the region world
     *
     * @return the region world
     */
    World getWorld();

    /**
     * Get the region priority
     *
     * @return the region priority
     */
    double getPriority();

    /**
     * Set the region priority
     *
     * @param priority the region new priority
     */
    void setPriority(final double priority);

    /**
     * Get the region blocks
     *
     * @return the region blocks
     */
    Collection<Block> getBlocks();

    /**
     * Get if the specified x, y and z coordinates
     * are out of region bounds
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return if the coordinates are out of bounds
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isOutOfBounds(final double x, final double y, final double z);

    /**
     * Get the distance of x, y and z from the region
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the distance from the region
     */
    double distance(final double x, final double y, final double z);

    /**
     * Get the region ceiling
     *
     * @return the region ceiling
     */
    RegionCeiling getCeiling();

    /**
     * Get the region floor
     *
     * @return the region floor
     */
    RegionFloor getFloor();

    /**
     * Get the region wall
     *
     * @param part the wall part
     * @return the region wall
     */
    RegionWall getWall(final WallPart part);

    /**
     * Get the region center
     *
     * @return the center
     */
    Location getCenter();

    /**
     * Check if this region intersects with
     * another region
     *
     * @param other the other region
     * @return if the region intersects
     */
    boolean intersects(final Region other);

    /**
     * Get the intersecting regions
     *
     * @return the regions which this
     * region intersects with
     */
    Collection<Region> getIntersecting();

    /**
     * Save the region into memory
     *
     * @param name the region name
     * @return if the region was able to be saved
     */
    boolean saveToMemory(final String name);
}
