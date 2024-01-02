package es.karmadev.api.array;

import es.karmadev.api.object.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * Array utilities
 */
public class ArrayUtils {

    /**
     * Get if the array contains the provided element
     * ignore case. This method will make use of {@link es.karmadev.api.object.ObjectUtils#equalsIgnoreCase(Object, Object)}
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     * @param <T> the element type
     */
    public static <T> boolean containsIgnoreCase(final Collection<? super T> array, final T element) {
        for (Object item : array) {
            if (ObjectUtils.equalsIgnoreCase(item, element)) return true;
        }

        return false;
    }

    /**
     * Get if the array contains the provided element
     * ignore case. This method will make use of {@link es.karmadev.api.object.ObjectUtils#equalsIgnoreCase(Object, Object)}
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     * @param <T> the element type
     */
    public static <T, A extends T> boolean containsIgnoreCase(final A[] array, final T element) {
        for (Object item : array) {
            if (ObjectUtils.equalsIgnoreCase(item, element)) return true;
        }

        return false;
    }

    /**
     * Get if the array contains the provided element.
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean contains(final byte[] array, final byte element) {
        Byte[] objectArray = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsAny(objectArray, element);
    }

    /**
     * Get if the array contains the provided element.
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean contains(final short[] array, final short element) {
        Short[] objectArray = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsAny(objectArray, element);
    }

    /**
     * Get if the array contains the provided element.
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean contains(final int[] array, final int element) {
        Integer[] objectArray = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsAny(objectArray, element);
    }

    /**
     * Get if the array contains the provided element.
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean contains(final long[] array, final long element) {
        Long[] objectArray = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsAny(objectArray, element);
    }

    /**
     * Get if the array contains the provided element.
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean contains(final float[] array, final float element) {
        Float[] objectArray = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsAny(objectArray, element);
    }

    /**
     * Get if the array contains the provided element.
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean contains(final double[] array, final float element) {
        Double[] objectArray = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsAny(objectArray, element);
    }

    /**
     * Get if the array contains the provided element.
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean contains(final char[] array, final char element) {
        Character[] objectArray = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsAny(objectArray, element);
    }

    /**
     * Get if the array contains the provided element.
     * This method ignores the floating point of the values
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean containsIgnoreCase(final float[] array, final float element) {
        Integer[] objectArray = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            float pointValue = array[i];
            objectArray[i] = (int) pointValue;
        }

        return containsAny(objectArray, (int) element);
    }

    /**
     * Get if the array contains the provided element.
     * This method ignores the floating point of the values
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean containsIgnoreCase(final double[] array, final double element) {
        Long[] objectArray = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            double pointValue = array[i];
            objectArray[i] = (long) pointValue;
        }

        return containsAny(objectArray, (long) element);
    }

    /**
     * Get if the array contains the provided element.
     * This method ignores the floating point of the values
     *
     * @param array the array to check for
     * @param element the element to check with
     * @return if the array contains the element
     */
    public static boolean containsIgnoreCase(final char[] array, final char element) {
        Character[] objectArray = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        return containsIgnoreCase(objectArray, element);
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
    public static <T> boolean containsAny(final Collection<? super T> array, final T... elements) {
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
    public static <T, A extends T> boolean containsAny(final A[] array, final T... elements) {
        for (A arrayItem : array) {
            if (arrayItem != null) {
                for (T element : elements) {
                    if (element.equals(arrayItem)) return true;
                }
            }
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
     */
    public static boolean containsAny(final byte[] array, final byte... elements) {
        Byte[] objectArray = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        Byte[] elementsArray = new Byte[elements.length];
        for (int i = 0; i < elements.length; i++) {
            elementsArray[i] = elements[i];
        }

        return containsAny(objectArray, elementsArray);
    }

    /**
     * Get if the array contains any of
     * the elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     */
    public static boolean containsAny(final short[] array, final short... elements) {
        Short[] objectArray = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        Short[] elementsArray = new Short[elements.length];
        for (int i = 0; i < elements.length; i++) {
            elementsArray[i] = elements[i];
        }

        return containsAny(objectArray, elementsArray);
    }

    /**
     * Get if the array contains any of
     * the elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     */
    public static boolean containsAny(final int[] array, final int... elements) {
        Integer[] objectArray = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        Integer[] elementsArray = new Integer[elements.length];
        for (int i = 0; i < elements.length; i++) {
            elementsArray[i] = elements[i];
        }

        return containsAny(objectArray, elementsArray);
    }

    /**
     * Get if the array contains any of
     * the elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     */
    public static boolean containsAny(final long[] array, final long... elements) {
        Long[] objectArray = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        Long[] elementsArray = new Long[elements.length];
        for (int i = 0; i < elements.length; i++) {
            elementsArray[i] = elements[i];
        }

        return containsAny(objectArray, elementsArray);
    }

    /**
     * Get if the array contains any of
     * the elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     */
    public static boolean containsAny(final float[] array, final float... elements) {
        Float[] objectArray = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        Float[] elementsArray = new Float[elements.length];
        for (int i = 0; i < elements.length; i++) {
            elementsArray[i] = elements[i];
        }

        return containsAny(objectArray, elementsArray);
    }

    /**
     * Get if the array contains any of
     * the elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     */
    public static boolean containsAny(final double[] array, final double... elements) {
        Double[] objectArray = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        Double[] elementsArray = new Double[elements.length];
        for (int i = 0; i < elements.length; i++) {
            elementsArray[i] = elements[i];
        }

        return containsAny(objectArray, elementsArray);
    }

    /**
     * Get if the array contains any of
     * the elements
     *
     * @param array the array
     * @param elements the elements
     * @return if the array contains the element
     */
    public static boolean containsAny(final char[] array, final char... elements) {
        Character[] objectArray = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            objectArray[i] = array[i];
        }

        Character[] elementsArray = new Character[elements.length];
        for (int i = 0; i < elements.length; i++) {
            elementsArray[i] = elements[i];
        }

        return containsAny(objectArray, elementsArray);
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
    public static <T> boolean containsOnAny(final Collection<? super T> array, final T... elements) {
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
    public static <T, A extends T> boolean containsOnAny(final A[] array, final T... elements) {
        for (A arrayItem : array) {
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
    public static <T, A extends T> T[] putAll(final A[] array, final T[]... arrays) {
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

    /**
     * Put all the arrays elements into the specified
     * single array
     *
     * @param array the array to put elements at
     * @param arrays the arrays o get values from
     * @return the mapped array
     */
    public static byte[] putAll(final byte[] array, final byte[]... arrays) {
        int totalLength = 0;
        for (byte[] elements : arrays) {
            totalLength += elements.length;
        }

        byte[] targetArray = array;
        if (array.length < totalLength) {
            targetArray = Arrays.copyOf(targetArray, totalLength);
        }

        int vIndex = 0;
        for (byte[] elements : arrays) {
            for (byte element : elements) {
                targetArray[vIndex++] = element;
            }
        }

        return targetArray;
    }

    /**
     * Put all the arrays elements into the specified
     * single array
     *
     * @param array the array to put elements at
     * @param arrays the arrays o get values from
     * @return the mapped array
     */
    public static short[] putAll(final short[] array, final short[]... arrays) {
        int totalLength = 0;
        for (short[] elements : arrays) {
            totalLength += elements.length;
        }

        short[] targetArray = array;
        if (array.length < totalLength) {
            targetArray = Arrays.copyOf(targetArray, totalLength);
        }

        int vIndex = 0;
        for (short[] elements : arrays) {
            for (short element : elements) {
                targetArray[vIndex++] = element;
            }
        }

        return targetArray;
    }

    /**
     * Put all the arrays elements into the specified
     * single array
     *
     * @param array the array to put elements at
     * @param arrays the arrays o get values from
     * @return the mapped array
     */
    public static int[] putAll(final int[] array, final int[]... arrays) {
        int totalLength = 0;
        for (int[] elements : arrays) {
            totalLength += elements.length;
        }

        int[] targetArray = array;
        if (array.length < totalLength) {
            targetArray = Arrays.copyOf(targetArray, totalLength);
        }

        int vIndex = 0;
        for (int[] elements : arrays) {
            for (int element : elements) {
                targetArray[vIndex++] = element;
            }
        }

        return targetArray;
    }

    /**
     * Put all the arrays elements into the specified
     * single array
     *
     * @param array the array to put elements at
     * @param arrays the arrays o get values from
     * @return the mapped array
     */
    public static long[] putAll(final long[] array, final long[]... arrays) {
        int totalLength = 0;
        for (long[] elements : arrays) {
            totalLength += elements.length;
        }

        long[] targetArray = array;
        if (array.length < totalLength) {
            targetArray = Arrays.copyOf(targetArray, totalLength);
        }

        int vIndex = 0;
        for (long[] elements : arrays) {
            for (long element : elements) {
                targetArray[vIndex++] = element;
            }
        }

        return targetArray;
    }

    /**
     * Put all the arrays elements into the specified
     * single array
     *
     * @param array the array to put elements at
     * @param arrays the arrays o get values from
     * @return the mapped array
     */
    public static float[] putAll(final float[] array, final float[]... arrays) {
        int totalLength = 0;
        for (float[] elements : arrays) {
            totalLength += elements.length;
        }

        float[] targetArray = array;
        if (array.length < totalLength) {
            targetArray = Arrays.copyOf(targetArray, totalLength);
        }

        int vIndex = 0;
        for (float[] elements : arrays) {
            for (float element : elements) {
                targetArray[vIndex++] = element;
            }
        }

        return targetArray;
    }

    /**
     * Put all the arrays elements into the specified
     * single array
     *
     * @param array the array to put elements at
     * @param arrays the arrays o get values from
     * @return the mapped array
     */
    public static double[] putAll(final double[] array, final double[]... arrays) {
        int totalLength = 0;
        for (double[] elements : arrays) {
            totalLength += elements.length;
        }

        double[] targetArray = array;
        if (array.length < totalLength) {
            targetArray = Arrays.copyOf(targetArray, totalLength);
        }

        int vIndex = 0;
        for (double[] elements : arrays) {
            for (double element : elements) {
                targetArray[vIndex++] = element;
            }
        }

        return targetArray;
    }
}
