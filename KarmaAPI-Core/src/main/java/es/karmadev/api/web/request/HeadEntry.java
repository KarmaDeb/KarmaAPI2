package es.karmadev.api.web.request;

import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request head entry
 */
@Getter
@Value(staticConstructor = "valueOf")
public class HeadEntry {

    String key;
    String value;

    /**
     * Modify the head entry value, but keep the
     * key
     *
     * @param newValue the new value
     * @return the modified entry
     */
    public HeadEntry setValue(final String newValue) {
        return valueOf(key, newValue);
    }

    /**
     * Modify the head entry key, but keep the
     * value
     *
     * @param newKey the new key
     * @return the modified entry
     */
    public HeadEntry setKey(final String newKey) {
        return valueOf(newKey, value);
    }

    /**
     * Verifies if the head entry is the
     * same as the provided one
     *
     * @param other the other object
     * @return if the other object is the same
     * as this
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HeadEntry)) return false;
        if (other == this) return true;

        HeadEntry entry = (HeadEntry) other;
        return entry.key.equals(key) && entry.value.equals(value);
    }

    /**
     * Hash the entry code
     *
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        return key.hashCode() + value.hashCode();
    }

    /**
     * Get the head entry string
     *
     * @return the entry string
     */
    @Override
    public String toString() {
        return key + ": " + value;
    }

    /**
     * Create a head entry array
     *
     * @param key1 the key 1
     * @param value1 the value
     * @return the head entry array
     */
    public static HeadEntry[] arrayOf(final String key1, final String value1) {
        return new HeadEntry[]{
                valueOf(key1, value1)
        };
    }

    /**
     * Create a head entry array
     *
     * @param key1 the key 1
     * @param value1 the value
     * @param key2 the key 2
     * @param value2 the value 2
     * @return the head entry array
     */
    public static HeadEntry[] arrayOf(
            final String key1, final String value1,
            final String key2, final String value2) {
        return new HeadEntry[]{
                valueOf(key1, value1),
                valueOf(key2, value2)
        };
    }

    /**
     * Create a head entry array
     *
     * @param key1 the key 1
     * @param value1 the value
     * @param key2 the key 2
     * @param value2 the value 2
     * @param key3 the key 3
     * @param value3 the value 3
     * @return the head entry array
     */
    public static HeadEntry[] arrayOf(
            final String key1, final String value1,
            final String key2, final String value2,
            final String key3, final String value3) {
        return new HeadEntry[]{
                valueOf(key1, value1),
                valueOf(key2, value2),
                valueOf(key3, value3)
        };
    }

    /**
     * Create a head entry array
     *
     * @param key1 the key 1
     * @param value1 the value
     * @param key2 the key 2
     * @param value2 the value 2
     * @param key3 the key 3
     * @param value3 the value 3
     * @param key4 the key 4
     * @param value4 the value 4
     * @return the head entry array
     */
    public static HeadEntry[] arrayOf(
            final String key1, final String value1,
            final String key2, final String value2,
            final String key3, final String value3,
            final String key4, final String value4) {
        return new HeadEntry[]{
                valueOf(key1, value1),
                valueOf(key2, value2),
                valueOf(key3, value3),
                valueOf(key4, value4)
        };
    }

    /**
     * Create a head entry array
     *
     * @param key1 the key 1
     * @param value1 the value
     * @param key2 the key 2
     * @param value2 the value 2
     * @param key3 the key 3
     * @param value3 the value 3
     * @param key4 the key 4
     * @param value4 the value 4
     * @param key5 the key 5
     * @param value5 the value 5
     * @return the head entry array
     */
    public static HeadEntry[] arrayOf(
            final String key1, final String value1,
            final String key2, final String value2,
            final String key3, final String value3,
            final String key4, final String value4,
            final String key5, final String value5) {
        return new HeadEntry[]{
                valueOf(key1, value1),
                valueOf(key2, value2),
                valueOf(key3, value3),
                valueOf(key4, value4),
                valueOf(key5, value5)
        };
    }

    /**
     * Create a head entry array
     *
     * @param key1 the key 1
     * @param value1 the value
     * @param key2 the key 2
     * @param value2 the value 2
     * @param key3 the key 3
     * @param value3 the value 3
     * @param key4 the key 4
     * @param value4 the value 4
     * @param key5 the key 5
     * @param value5 the value 5
     * @param key6 the key 6
     * @param value6 the value 6
     * @return the head entry array
     */
    public static HeadEntry[] arrayOf(
            final String key1, final String value1,
            final String key2, final String value2,
            final String key3, final String value3,
            final String key4, final String value4,
            final String key5, final String value5,
            final String key6, final String value6) {
        return new HeadEntry[]{
                valueOf(key1, value1),
                valueOf(key2, value2),
                valueOf(key3, value3),
                valueOf(key4, value4),
                valueOf(key5, value5),
                valueOf(key6, value6)
        };
    }

    /**
     * Create a head entry array
     *
     * @param key1 the key 1
     * @param value1 the value
     * @param key2 the key 2
     * @param value2 the value 2
     * @param key3 the key 3
     * @param value3 the value 3
     * @param key4 the key 4
     * @param value4 the value 4
     * @param key5 the key 5
     * @param value5 the value 5
     * @param key6 the key 6
     * @param value6 the value 6
     * @param key7 the key 7
     * @param value7 the value 7
     * @return the head entry array
     */
    public static HeadEntry[] arrayOf(
            final String key1, final String value1,
            final String key2, final String value2,
            final String key3, final String value3,
            final String key4, final String value4,
            final String key5, final String value5,
            final String key6, final String value6,
            final String key7, final String value7) {
        return new HeadEntry[]{
                valueOf(key1, value1),
                valueOf(key2, value2),
                valueOf(key3, value3),
                valueOf(key4, value4),
                valueOf(key5, value5),
                valueOf(key6, value6),
                valueOf(key7, value7)
        };
    }

    /**
     * Get an array of entries from a map
     *
     * @param prefix appends a "map_" prefix to each
     *               of the key
     * @param map the map
     * @return the map as a head-entry array
     */
    public static HeadEntry[] fromMap(final boolean prefix, final Map<String, String> map) {
        if (prefix) {
            return fromMap("map_", map);
        }

        return fromMap(null, map);
    }

    /**
     * Get an array of entries from a map
     *
     * @param mapPrefix the map prefix
     * @param map the map
     * @return the map as a head-entry array
     */
    public static HeadEntry[] fromMap(final String mapPrefix, final Map<String, String> map) {
        List<HeadEntry> entries = new ArrayList<>();

        synchronized (map) {
            map.keySet().forEach((mapKey) -> {
                String key = (mapPrefix == null ? mapKey : mapPrefix + mapKey);
                String value = map.get(key);

                entries.add(new HeadEntry(key, value));
            });
        }

        return entries.toArray(new HeadEntry[0]);
    }

    /**
     * Get an array of entries from an entry array
     *
     * @param prefix appends a "map_" prefix to each
     *      *               of the key
     * @param entries the entries
     * @return the entry array as a head-entry array
     */
    @SafeVarargs
    public static HeadEntry[] fromEntries(final boolean prefix, final Map.Entry<String, String>... entries) {
        if (entries.length == 0) return new HeadEntry[0];
        if (prefix) {
            return fromEntries("map_", entries);
        }

        return fromEntries(null, entries);
    }

    /**
     * Get an array of entries from an entry array
     *
     * @param mapPrefix the map prefix
     * @param entries the entries
     * @return the entry array as a head-entry array
     */
    @SafeVarargs
    public static HeadEntry[] fromEntries(final String mapPrefix, final Map.Entry<String, String>... entries) {
        if (entries.length == 0) return new HeadEntry[0];
        List<HeadEntry> headEntries = new ArrayList<>();

        for (Map.Entry<String, String> entry : entries) {
            String key = (mapPrefix == null ? entry.getKey() : mapPrefix + entry.getKey());
            String value = entry.getValue();

            headEntries.add(new HeadEntry(key, value));
        }

        return headEntries.toArray(new HeadEntry[0]);
    }
}
