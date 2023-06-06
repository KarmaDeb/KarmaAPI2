package es.karmadev.api.spigot.region.world.part;

import org.bukkit.Location;

/**
 * KarmaAPI region part
 */
public interface RegionPart {

    /**
     * Get the part start point
     *
     * @return the start point
     */
    Location getStart();


    /**
     * Get the part end point
     *
     * @return the end point
     */
    Location getEnd();
}
