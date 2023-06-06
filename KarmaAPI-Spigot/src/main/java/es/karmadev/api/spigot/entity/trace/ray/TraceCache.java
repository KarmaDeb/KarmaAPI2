package es.karmadev.api.spigot.entity.trace.ray;

import es.karmadev.api.strings.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

class TraceCache {

    private final Map<PointCache, List<TracePointCache>> locations = new ConcurrentHashMap<>();

    final Location point1;
    final Location point2;

    public TraceCache(final Location p1, final Location p2) {
        point1 = p1;
        point2 = p2;
    }

    public boolean hasCache(final double max, final double precision) {
        PointCache pCache = new PointCache(point1, point2);
        if (locations.containsKey(pCache)) {
            List<TracePointCache> caches = locations.getOrDefault(pCache, new CopyOnWriteArrayList<>());
            try (Stream<TracePointCache> stream = caches.stream().filter((c) -> c.precision == precision && c.distance == max)) {
                return stream.findAny().isPresent();
            }
        }

        return false;
    }

    public TracePointCache getCache(final double max, final double precision) {
        PointCache pCache = new PointCache(point1, point2);
        List<TracePointCache> caches = locations.getOrDefault(pCache, new CopyOnWriteArrayList<>());
        try (Stream<TracePointCache> stream = caches.stream().filter((c) -> c.precision == precision && c.distance == max)) {
            return stream.findAny().orElse(null);
        }
    }

    public TracePointCache getMaxCache(final double precision) {
        PointCache pCache = new PointCache(point1, point2);
        List<TracePointCache> caches = locations.getOrDefault(pCache, new CopyOnWriteArrayList<>());
        double max = 0;
        TracePointCache cache = null;
        for (TracePointCache tpc : caches) {
            if (tpc.precision == precision && tpc.distance > max) {
                cache = tpc;
            }
        }

        return cache;
    }

    public TracePointCache getMinCache(final double precision) {
        PointCache pCache = new PointCache(point1, point2);
        List<TracePointCache> caches = locations.getOrDefault(pCache, new CopyOnWriteArrayList<>());
        double max = Integer.MAX_VALUE;
        TracePointCache cache = null;
        for (TracePointCache tpc : caches) {
            if (tpc.precision == precision && tpc.distance < max) {
                cache = tpc;
            }
        }

        return cache;
    }

    public void definePoints(final double distance, final double precision, final Location... traces) {
        TracePointCache tpc = new TracePointCache(distance, precision, traces);
        PointCache pCache = new PointCache(point1, point2);

        if (hasCache(distance, precision)) {
            List<TracePointCache> caches = locations.get(pCache);
            int index = 0;
            for (TracePointCache trace : caches) {
                if (trace.distance == distance && trace.precision == precision) {
                    break;
                }

                index++;
            }

            caches.set(index, tpc); //Update the existing cache
            locations.put(pCache, caches);
        } else {
            List<TracePointCache> caches = locations.getOrDefault(pCache, new CopyOnWriteArrayList<>());
            caches.add(tpc);
            locations.put(pCache, caches);
        }
    }
}

class TracePointCache {

    final double distance;
    final double precision;
    final Location[] locations;

    public TracePointCache(final double distance, final double precision, final Location... traces) {
        this.distance = distance;
        this.precision = precision;
        this.locations = traces;
    }
}

class PointCache {

    final Location point1;
    final Location point2;

    public PointCache(final Location point1, final Location point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link Object#equals(Object)}
     *     method, then calling the {@code hashCode} method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class {@code Object} does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java&trade; programming language.)
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        World w = point1.getWorld();
        assert w != null;

        int wHash = 0;
        Number[] numbers = StringUtils.extractNumbers(w.getUID().toString());
        for (Number n : numbers) wHash += n.intValue();

        return wHash + point1.hashCode() + point2.hashCode();
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        World w = point1.getWorld();
        assert w != null;

        int wHash = 0;
        Number[] numbers = StringUtils.extractNumbers(w.getUID().toString());
        for (Number n : numbers) wHash += n.intValue();

        return String.valueOf(wHash + point1.hashCode() + point2.hashCode());
    }
}
