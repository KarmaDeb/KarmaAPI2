package es.karmadev.api.spigot.entity.trace.auto;

import es.karmadev.api.spigot.tracker.AutoTracker;
import es.karmadev.api.spigot.tracker.TrackerEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.*;

/**
 * Health based auto tracker
 */
public class HealthAutoTracker implements AutoTracker, Comparator<LivingEntity> {

    public final boolean prioritizeLowest;

    /**
     * Initialize the health-based
     * auto tracker
     */
    public HealthAutoTracker() {
        this(false);
    }

    /**
     * Initialize the health-based
     * auto tracker
     *
     * @param prioritizeLowest if we should prioritize low-health entities
     */
    public HealthAutoTracker(final boolean prioritizeLowest) {
        this.prioritizeLowest = prioritizeLowest;
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

        data.sort(this);
        if (prioritizeLowest) {
            Collections.reverse(data);
        }

        return data.toArray(new LivingEntity[0]);
    }

    /**
     * Get if the tracker prioritizes lowest
     * health entities
     *
     * @return the tracker setting
     */
    public boolean prioritizeLowest() {
        return prioritizeLowest;
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     * <p>
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.<p>
     * <p>
     * The implementor must ensure that <tt>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>compare(x, y)</tt> must throw an exception if and only
     * if <tt>compare(y, x)</tt> throws an exception.)<p>
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
     * <tt>compare(x, z)&gt;0</tt>.<p>
     * <p>
     * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt>
     * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
     * <tt>z</tt>.<p>
     * <p>
     * It is generally the case, but <i>not</i> strictly required that
     * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(final LivingEntity o1, final LivingEntity o2) {
        double o1Health = o1.getHealth();
        double o2Health = o2.getHealth();
        return Double.compare(o1Health, o2Health);
    }
}
