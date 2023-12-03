package es.karmadev.api.collect.map;

import es.karmadev.api.collect.model.IndexedMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Linked indexed map, which extends LinkedHashMap
 * and allows to obtain an entry by index
 *
 * @param <T> the key type
 * @param <V> the value type
 */
public class LinkedIndexedMap<T, V> extends LinkedHashMap<T, V> implements IndexedMap<T, V> {

    /**
     * Get a key by index
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the key
     */
    @Override
    public T getKey(final int index, final T defaultValue) {
        Map.Entry<T, V> entry = getEntry(index, null);
        if (entry == null) return defaultValue;

        return entry.getKey();
    }

    /**
     * Get a value
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the value
     */
    @Override
    public V getValue(final int index, final V defaultValue) {
        Map.Entry<T, V> entry = getEntry(index, null);
        if (entry == null) return defaultValue;

        return entry.getValue();
    }

    /**
     * Get an entry
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the entry
     */
    @Override
    public Map.Entry<T, V> getEntry(final int index, final Map.Entry<T, V> defaultValue) {
        Set<Map.Entry<T, V>> entries = entrySet();
        int vIndex = 0;
        for (Map.Entry<T, V> entry : entries) {
            if (vIndex++ == index) {
                return entry;
            }
        }

        return defaultValue;
    }
}
