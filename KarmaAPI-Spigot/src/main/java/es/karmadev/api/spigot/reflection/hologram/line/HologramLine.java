package es.karmadev.api.spigot.reflection.hologram.line;

import es.karmadev.api.spigot.reflection.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Hologram text line
 */
@SuppressWarnings("unused")
public interface HologramLine {

    /**
     * Get the line id
     *
     * @return the line id
     */
    int id();

    /**
     * Get the parent hologram
     *
     * @return the parent hologram
     */
    Hologram parent();

    /**
     * Get the hologram height
     *
     * @return the hologram height
     */
    double height();

    /**
     * Remove the line from the hologram
     */
    void remove();

    /**
     * Get if the line exists
     *
     * @return if the line exists
     */
    boolean exists();

    /**
     * Spawn the hologram line
     *
     * @param location the location to spawn at
     */
    default void spawn(final Location location) {
        if (location == null) return;

        World locationWorld = location.getWorld();
        if (locationWorld == null) return;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        spawn(locationWorld, x, y, z);
    }

    /**
     * Spawn the hologram line
     *
     * @param world the world to spawn at
     * @param x the line position x
     * @param y the line position y
     * @param z the line position z
     */
    void spawn(final World world, final double x, final double y, final double z);

    /**
     * Destroy the line (de-spawn)
     */
    void destroy();

    /**
     * Get the attached line entities
     *
     * @return the attached entities
     */
    int[] entities();

    /**
     * Change the line world
     *
     * @param world the new line world
     * @return if the line was able to be
     * moved
     */
    boolean teleportAt(final World world);

    /**
     * Teleport the line
     *
     * @param x the new X location
     * @param y the new Y location
     * @param z the new Z location
     * @return if the line was able to be
     * teleported
     */
    boolean teleport(final double x, final double y, final double z);

    /**
     * Get the line world
     *
     * @return the line world
     */
    World world();

    /**
     * Get the line X position
     *
     * @return the X position
     */
    double x();

    /**
     * Get the line Y position
     *
     * @return the Y position
     */
    double y();

    /**
     * Get the line Z position
     *
     * @return the Z position
     */
    double z();

    /**
     * Make this line a touchable line
     * if it wasn't already
     *
     * @return the touchable line instance
     */
    TouchableLine touchable();

    /**
     * Get if the line is a touchable
     * line
     *
     * @return if the line is touchable
     */
    boolean isTouchable();
}
