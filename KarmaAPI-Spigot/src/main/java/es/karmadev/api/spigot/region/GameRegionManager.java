package es.karmadev.api.spigot.region;

import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.spigot.region.world.Region;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * KarmaAPI game region manager
 */
public class GameRegionManager {

    private final static Set<Region> regions = ConcurrentHashMap.newKeySet();

    public static boolean register(final Region cuboid) {
        if (regions.stream().anyMatch((region) -> region.intersects(cuboid))) return false;
        regions.add(cuboid);
        return true;
    }

    public static Region[] filterByName(final String name) {
        try (Stream<Region> filtered = regions.stream().filter((region -> ConsoleColor.strip(region.getName()).equals(ConsoleColor.strip(name))))) {
            return filtered.toArray(value -> new Region[0]);
        }
    }

    public static Optional<Region> getById(final UUID id) {
        return regions.stream().filter((region) -> region.getUUID().equals(id)).findAny();
    }

    public static Region[] getRegions() {
        return regions.toArray(new Region[0]);
    }
}
