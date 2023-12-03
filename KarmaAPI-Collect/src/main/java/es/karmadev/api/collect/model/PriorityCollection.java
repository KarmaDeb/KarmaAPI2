package es.karmadev.api.collect.model;

import java.util.Collection;

/**
 * Represents a collection which have
 * prioritized items. The que is automatically
 * ordered based on the item priority
 */
public interface PriorityCollection<T> extends Collection<T> {

    /**
     * Get the item priority
     *
     * @param item the item
     * @return the item priority
     */
    double getPriority(final T item);

    /**
     * Set the priority for the item at
     * the specified index
     *
     * @param index the item index
     * @param newPriority the new priority
     * @return if the operation was successful
     */
    boolean setPriority(final int index, final double newPriority);

    /**
     * Set the priority for all the items
     * matching the item
     *
     * @param item the item
     * @param newPriority the new item priority
     * @return if the operation was successful
     */
    boolean setPriority(final T item, final double newPriority);

    /**
     * Add an item with priority
     *
     * @param item the item to add
     * @param priority the priority
     * @return if the operation was successful
     */
    boolean add(final T item, final double priority);

    /**
     * Add an item with priority
     *
     * @param index the index to add at
     * @param item the item to add
     * @param priority the priority
     * @return if the operation was successful
     */
    boolean add(final int index, final T item, final double priority);

    /**
     * Remove an item at the specified
     * index
     *
     * @param index the index item to remove
     * @return if the operation was successful
     */
    boolean remove(final int index);

    /**
     * Get an element on the specified
     * index
     *
     * @param index the index
     * @return the element
     */
    T get(final int index);

    /**
     * Get the index of an item
     *
     * @param item the item
     * @return the index
     */
    int indexOf(final T item);

    /**
     * Get the index of an item with
     * the specified priority
     *
     * @param item the item
     * @param priority the priority
     * @return the index
     */
    int indexOf(final T item, final double priority);

    /**
     * Get the next element in the
     * sorted version of the priority
     * array
     *
     * @return the next element
     */
    T next();

    /**
     * Removes the current element in
     * the sorted version of the priority
     * array
     *
     * @return if the operation was successful
     */
    boolean consume();

    /**
     * Get the previous element in the
     * sorted version of the priority
     * array
     *
     * @return the previous element
     */
    T previous();

    /**
     * Get if the array has a next
     * element
     *
     * @return if the array has next
     */
    boolean hasNext();

    /**
     * Get if the array has a previous
     * element
     *
     * @return if the array has previous
     */
    boolean hasPrevious();

    /**
     * Get the sorted array copy
     * of the collection
     *
     * @param array the array to write to
     * @return the sorted array
     */
    <T1> T1[] sorted(final T1[] array);

    /**
     * Get the sorted version of
     * the array
     *
     * @return the sorted version
     */
    default Object[] sorted() {
        return sorted(new Object[0]);
    }

    /**
     * Collect the collection into a
     * sorted array
     *
     * @return the sorted array
     */
    Collection<T> sortedCollect();

    /**
     * Get the unsorted collection
     * instance of the priority collection
     *
     * @return the unsorted collection
     */
    Collection<T> unsortedCollect();
}
