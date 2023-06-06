package es.karmadev.api.spigot.region.exception;

import org.bukkit.World;

/**
 * This exception is thrown when a region
 * is tried to be created from different worlds
 */
public class DimensionException extends Exception {

    /**
     * Initialize the exception
     *
     * @param world1 the first world
     * @param world2 the second world
     */
    public DimensionException(final World world1, final World world2) {
        super("Cannot create region using different world dimensions (" + world1.getName() + " | " + world2.getName() + ")");
    }
}
