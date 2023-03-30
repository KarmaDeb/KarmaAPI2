package es.karmadev.api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KarmaAPI exception collector
 */
public class ExceptionCollector {

    private final static Map<String, List<Throwable>> caught = new ConcurrentHashMap<>();

    /**
     * Catch an exception
     *
     * @param clazz the class catching the exception
     * @param exception the exception
     */
    public static void catchException(final Class<?> clazz, final Throwable exception) {
        List<Throwable> errors = caught.computeIfAbsent(clazz.getCanonicalName(), (list) -> new ArrayList<>());
        errors.add(exception);

        caught.put(clazz.getCanonicalName(), errors);
    }

    /**
     * Get all the exceptions
     *
     * @param clazz the class to get
     *              from
     * @return the caught exceptions
     */
    public static Throwable[] getExceptions(final Class<?> clazz) {
        List<Throwable> errors = new ArrayList<>();
        if (caught.containsKey(clazz.getCanonicalName())) {
            errors = caught.remove(clazz.getCanonicalName());
        }

        return errors.toArray(new Throwable[0]);
    }
}
