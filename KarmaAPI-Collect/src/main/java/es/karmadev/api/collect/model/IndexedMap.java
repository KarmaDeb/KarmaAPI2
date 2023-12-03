package es.karmadev.api.collect.model;

import java.util.Map;

/**
 * Represents an indexed map, which allows
 * to obtain an entry by index
 *
 * @param <T> the key type
 * @param <V> the value type
 */
public interface IndexedMap<T, V> extends Map<T, V> {

    /**
     * Get a key by index
     *
     * @param index the index
     * @return the key
     */
    default T getKey(int index) {
        return getKey(index, null);
    }

    /**
     * Get a value
     *
     * @param index the index
     * @return the value
     */
    default V getValue(int index) {
        return getValue(index, null);
    }

    /**
     * Get an entry
     *
     * @param index the index
     * @return the entry
     */
    default Entry<T, V> getEntry(final int index) {
        return getEntry(index, null);
    }

    /**
     * Get a key by index
     *
     * @param index the index
     * @param defaultValue the default value
     * @return the key
     */
    T getKey(int index, final T defaultValue);

    /**
     * Get a value
     *
     * @param index the index
     * @param defaultValue the default value
     * @return the value
     */
    V getValue(int index, final V defaultValue);

    /**
     * Get an entry
     *
     * @param index the index
     * @param defaultValue the default value
     * @return the entry
     */
    Entry<T, V> getEntry(int index, final Entry<T, V> defaultValue);
}
