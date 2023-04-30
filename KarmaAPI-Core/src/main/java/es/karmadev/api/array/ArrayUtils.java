package es.karmadev.api.array;

import java.util.Collection;

/**
 * Array utilities
 */
public class ArrayUtils {

    /**
     * Get if the array contains any of the
     * elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     * @param <T> the element type
     */
    @SafeVarargs
    public static <T> boolean containsAny(final Collection<T> array, T... elements) {
        for (T element : elements) {
            if (array.contains(element)) return true;
        }

        return false;
    }

    /**
     * Get if the array contains any of
     * the elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     * @param <T> the element type
     */
    @SafeVarargs
    public static <T> boolean containsAny(final T[] array, T... elements) {
        for (T arrayItem : array) {
            if (arrayItem != null) {
                for (T element : elements) {
                    if (element.equals(arrayItem)) return true;
                }
            }
        }

        return false;
    }
}
