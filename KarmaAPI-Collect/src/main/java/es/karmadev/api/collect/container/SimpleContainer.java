package es.karmadev.api.collect.container;

import es.karmadev.api.collect.reflection.ArrayHelper;
import es.karmadev.api.collect.model.Container;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a fixed-size container
 *
 * @param <T> the container type
 */
public class SimpleContainer<T> implements Container<T> {

    private final int maxCapacity;
    private final Class<T> type;

    protected T[] elements;
    protected long[] insertionTimes;

    /**
     * Initialize the fixed container
     *
     * @param maxCapacity the max capacity
     * @param type the container type
     */
    public SimpleContainer(final int maxCapacity, final Class<T> type) {
        this.type = type;

        int capacity = maxCapacity;
        if (capacity < 0) {
            capacity = Integer.MAX_VALUE;
        }

        this.maxCapacity = Math.max(1, capacity);
        this.elements = ArrayHelper.typedArrayFor(type, 0);
        this.insertionTimes = new long[0];
    }

    /**
     * Get the container type
     *
     * @return the container type
     */
    @Override
    public Class<T> getType() {
        return type;
    }

    /**
     * Get the items on the container
     *
     * @param array the array to put items
     *              at
     * @return the items
     */
    @Override @SuppressWarnings("unchecked")
    public T[] getItems(final T[] array) {
        if (array.length < elements.length)
            return (T[]) Arrays.copyOf(elements, elements.length, array.getClass());

        System.arraycopy(elements, 0, array, 0, elements.length);
        if (array.length > elements.length)
            array[elements.length] = null;

        return array;
    }

    /**
     * Get the element insertion time
     *
     * @param item the item
     * @return the insertion time
     */
    @Override
    public long getInsertionTime(final T item) {
        if (!contains(item)) return -1;

        for (int i = 0; i < elements.length; i++) {
            if (Objects.equals(elements[i], item)) {
                return insertionTimes[i];
            }
        }

        return 0;
    }

    /**
     * Tries to add an item to the
     * container
     *
     * @param item the item to add
     * @return the operation result
     */
    @Override
    public boolean addItem(final T item) {
        if (item == null || elements.length == maxCapacity) {
            return false;
        }

        int writeableIndex = -1;
        for (int i = 0; i < elements.length; i++) {
            Object element = elements[i];
            if (element == null) {
                if (writeableIndex == -1) {
                    writeableIndex = i;
                }

                continue;
            }

            if (Objects.equals(element, item)) {
                return false;
            }
        }

        if (writeableIndex != -1){
            elements[writeableIndex] = item;
            insertionTimes[writeableIndex] = System.currentTimeMillis();
        } else {
            grow();

            elements[elements.length - 1] = item;
            insertionTimes[elements.length - 1] = System.currentTimeMillis();
        }

        return true;
    }

    /**
     * Relocate an item to a new index
     *
     * @param item     the item
     * @param newIndex the new index
     */
    @Override
    public boolean relocate(final T item, final int newIndex) {
        if (newIndex < 0 || newIndex > elements.length) {
            throw new IndexOutOfBoundsException("Cannot write to array at index " + newIndex + " (max capacity is " + maxCapacity + ")");
        }

        if (item == null || !contains(item)) return false;

        int currentIndex = -1;
        for (int i = 0; i < elements.length; i++) {
            T element = elements[i];
            if (Objects.equals(element, item)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) return false;
        elements[currentIndex] = null;

        if (currentIndex > newIndex) {
            for (int i = currentIndex; i > newIndex; i--) {
                T previous = elements[currentIndex - 1];
                elements[currentIndex] = previous;
            }
        } else {
            for (int i = currentIndex; i < newIndex; i++) {
                T next = elements[currentIndex + 1];
                elements[currentIndex] = next;
            }
        }

        elements[newIndex] = item;

        //We won't modify the insertion times, otherwise when sorting
        //the index would be "corrected"
        return true;
    }

    /**
     * Removes an item from the container
     *
     * @param item the item
     * @return the operation result
     */
    @Override
    public boolean removeItem(final T item) {
        if (item == null || elements.length == 0) return false;

        T[] newArray = ArrayHelper.typedArrayFor(type, elements.length);
        long[] newInsertions = new long[elements.length];

        int index = 0;
        for (int i = 0; i < elements.length; i++) {
            T element = elements[i];
            long insertion = insertionTimes[i];

            if (Objects.equals(element, item)) {
                continue;
            }

            newArray[index] = element;
            newInsertions[index++] = insertion;
        }

        T lastElement = newArray[newArray.length - 1];
        if (lastElement == null) {
            elements = Arrays.copyOf(newArray, newArray.length - 1);
            insertionTimes = Arrays.copyOf(newInsertions, newInsertions.length - 1);
            return true;
        }

        return false;
    }

    /**
     * Get if the container contains the
     * specified item
     *
     * @param item the item to search for
     * @return if the container contains
     */
    @Override
    public boolean contains(final T item) {
        if (item == null) return false;

        for (T element : elements) {
            if (Objects.equals(element, item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Merge the container with another
     * container
     *
     * @param other the other container
     * @return the merged container
     */
    @Override
    public SimpleContainer<T> merge(final Container<T> other) {
        T[] otherItems = other.getItems(ArrayHelper.typedArrayFor(type, 0));
        Map<T, Long> times = new HashMap<>();

        for (T item : otherItems) {
            if (item == null) continue;
            times.put(item, other.getInsertionTime(item));
        }
        for (T item : elements) {
            if (item == null) continue;

            long currentInsertion = getInsertionTime(item);
            if (times.containsKey(item)) {
                long insertion = times.get(item);
                if (insertion >= currentInsertion) {
                    continue;
                }
            }

            times.put(item, currentInsertion);
        }

        Map<T, Long> sorted = times.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        SimpleContainer<T> newContainer = new SimpleContainer<>(Math.max(maxCapacity, sorted.size()), type);
        newContainer.elements = sorted.keySet().toArray(ArrayHelper.typedArrayFor(getClass(), 0));

        Long[] values = sorted.values().toArray(new Long[0]);
        long[] nativeValues = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            nativeValues[i] = values[i];
        }

        newContainer.insertionTimes = nativeValues;
        return newContainer;
    }

    /**
     * Clear the container
     */
    @Override
    public void clear() {
        elements = ArrayHelper.typedArrayFor(type, 0);
    }

    /**
     * Get the container size
     *
     * @return the container size
     */
    @Override
    public int getSize() {
        return elements.length;
    }

    /**
     * Get the container maximum capacity
     *
     * @return the container max
     * capacity
     */
    @Override
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Get if the container can grow
     *
     * @return if the container can
     * grow
     */
    @Override
    public boolean canGrow() {
        return elements.length < maxCapacity;
    }

    /**
     * Grows the container if possible,
     * or otherwise throws an {@link UnsupportedOperationException}
     *
     * @throws UnsupportedOperationException if the grow is not
     *                                       supported
     */
    @Override
    public void grow() throws UnsupportedOperationException {
        if (elements.length + 1 > maxCapacity) throw new UnsupportedOperationException();
        this.elements = Arrays.copyOf(elements, elements.length + 1);
        this.insertionTimes = Arrays.copyOf(insertionTimes, elements.length + 1);
    }
}