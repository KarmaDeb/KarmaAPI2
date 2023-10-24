package es.karmadev.api.array;

import java.util.Arrays;
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
    public static <T> boolean containsAny(final Collection<T> array, final T... elements) {
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
    public static <T> boolean containsAny(final T[] array, final T... elements) {
        for (T arrayItem : array) {
            if (arrayItem != null) {
                for (T element : elements) {
                    if (element.equals(arrayItem)) return true;
                }
            }
        }

        return false;
    }

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
    public static <T> boolean containsOnAny(final Collection<T> array, final T... elements) {
        for (T element : elements) {
            if (element == null) continue;
            return array.stream().anyMatch((s) -> s != null && s.toString().contains(element.toString()));
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
    public static <T> boolean containsOnAny(final T[] array, final T... elements) {
        for (T arrayItem : array) {
            if (arrayItem != null) {
                for (T element : elements) {
                    if (arrayItem.toString().contains(element.toString())) return true;
                }
            }
        }

        return false;
    }

    /**
     * Put all the arrays elements into the specified
     * single array
     *
     * @param array the array to put elements at
     * @param arrays the arrays to get values from
     * @return the mapped array
     * @param <T> the array type
     */
    @SafeVarargs
    public static <T> T[] putAll(final T[] array, final T[]... arrays) {
        int totalLength = 0;
        for (T[] elements : arrays) {
            totalLength += elements.length;
        }

        T[] targetArray = array;
        if (array.length < totalLength) {
            targetArray = Arrays.copyOf(targetArray, totalLength);
        }

        int vIndex = 0;
        for (T[] elements : arrays) {
            for (T element : elements) {
                targetArray[vIndex++] = element;
            }
        }

        return targetArray;
    }
}
