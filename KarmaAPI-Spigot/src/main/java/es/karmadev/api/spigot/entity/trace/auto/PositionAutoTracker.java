package es.karmadev.api.spigot.entity.trace.auto;

import es.karmadev.api.spigot.tracker.AutoTracker;
import es.karmadev.api.spigot.tracker.TrackerEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Position based auto tracker
 */
public class PositionAutoTracker implements AutoTracker {

    public final boolean prioritizeFarAway;

    /**
     * Initialize the position-based
     * auto tracker
     */
    public PositionAutoTracker() {
        this(false);
    }

    /**
     * Initialize the position-based
     * auto tracker
     *
     * @param prioritizeFarAway if we should prioritize far entities
     */
    public PositionAutoTracker(final boolean prioritizeFarAway) {
        this.prioritizeFarAway = prioritizeFarAway;
    }

    /**
     * Track an entity
     *
     * @param tracker the tracker
     * @param radius  the max radius
     * @return the entities to track
     */
    @Override
    public LivingEntity[] track(final TrackerEntity tracker, final double radius) {
        World world = tracker.getWorld();
        Location trackerLocation = new Location(world, tracker.getX(), tracker.getY(), tracker.getZ());

        List<LivingEntity> data = new ArrayList<>();
        for (LivingEntity entity : world.getLivingEntities()) {
            Location entLoc = entity.getLocation();
            if (entLoc.distance(trackerLocation) <= radius) {
                data.add(entity);
            }
        }

        data.sort(new Comparator<LivingEntity>() {
            @Override
            public int compare(final LivingEntity o1, final LivingEntity o2) {
                double o1Distance = o1.getLocation().distance(trackerLocation);
                double o2Distance = o2.getLocation().distance(trackerLocation);
                return Double.compare(o1Distance, o2Distance);
            }
        });
        if (prioritizeFarAway) {
            Collections.reverse(data);
        }

        return data.toArray(new LivingEntity[0]);
    }

    /**
     * Get if the tracker prioritizes far away
     * entities
     *
     * @return the tracker setting
     */
    public boolean prioritizeFarAway() {
        return prioritizeFarAway;
    }
}
