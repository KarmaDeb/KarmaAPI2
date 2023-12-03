package es.karmadev.api.collect.reflection;

import java.lang.reflect.Array;

/**
 * Array helper utilities
 */
public class ArrayHelper {

    @SuppressWarnings("unchecked")
    public static <T> T[] typedArrayFor(final Class<?> clazz, final int capacity) {
        Class<?> containerType = clazz.getComponentType();
        if (containerType == null) {
            containerType = clazz;
        }

        return (T[]) Array.newInstance(containerType, capacity);
    }
}
