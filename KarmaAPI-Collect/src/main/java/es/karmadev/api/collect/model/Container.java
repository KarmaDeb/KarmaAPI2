package es.karmadev.api.collect.model;

/**
 * Represents a container which can hold
 * a limited amount of items. A container might
 * be re-sizeable or fixed-size. A container won't
 * also contain any items which contents match, for
 * example, if a container contains "Hello", and you
 * try to store "Hello", the operation will fail
 *
 * @param <T> the container type
 */
public interface Container<T> {

    /**
     * Get the container type
     *
     * @return the container type
     */
    Class<T> getType();

    /**
     * Get the items on the container
     *
     * @param array the array to put items
     *              at
     * @return the items
     */
    T[] getItems(final T[] array);

    /**
     * Get the element insertion time
     *
     * @param item the item
     * @return the insertion time
     */
    long getInsertionTime(final T item);

    /**
     * Tries to add an item to the
     * container
     *
     * @param item the item to add
     * @return the operation result
     */
    boolean addItem(final T item);

    /**
     * Relocate an item to a new index
     *
     * @param item the item
     * @param newIndex the new index
     */
    boolean relocate(final T item, final int newIndex);

    /**
     * Removes an item from the container
     *
     * @param item the item
     * @return the operation result
     */
    boolean removeItem(final T item);

    /**
     * Get if the container contains the
     * specified item
     *
     * @param item the item to search for
     * @return if the container contains
     */
    boolean contains(final T item);

    /**
     * Merge the container with another
     * container
     *
     * @param other the other container
     * @return the merged container
     */
    Container<T> merge(final Container<T> other);

    /**
     * Clear the container
     */
    void clear();

    /**
     * Get the container size
     *
     * @return the container size
     */
    int getSize();

    /**
     * Get the container maximum capacity
     *
     * @return the container max
     * capacity
     */
    int getMaxCapacity();

    /**
     * Get if the container can grow
     *
     * @return if the container can
     * grow
     */
    boolean canGrow();

    /**
     * Grows the container if possible,
     * or otherwise throws an {@link UnsupportedOperationException}
     *
     * @throws UnsupportedOperationException if the grow is not
     * supported
     */
    void grow() throws UnsupportedOperationException;
}
