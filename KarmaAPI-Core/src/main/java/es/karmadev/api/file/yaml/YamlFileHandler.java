package es.karmadev.api.file.yaml;

import es.karmadev.api.file.RawType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;

/**
 * Karma YAML file manager
 */
public interface YamlFileHandler {

    /**
     * Get the yaml file raw data
     *
     * @return the yaml file raw data
     */
    Map<String, Object> rawData();

    /**
     * Import data from the other file handler
     *
     * @param other the other file handler
     * @param replaceExisting replace the current data with
     *                        the other yaml data
     */
    void importFrom(final YamlFileHandler other, final boolean replaceExisting);

    /**
     * Validate the current file
     */
    void validate();

    /**
     * Reload the current file handle
     *
     * @return if the file was able to be reloaded
     */
    boolean reload();

    /**
     * Compare the objects
     *
     * @param path the path to the value
     * @param value the value to compare
     * @return if the values are the same type
     */
    @SuppressWarnings("all")
    boolean compareValue(final String path, final Object value);

    /**
     * Transform a value into the specified raw type
     *
     * @param path the path to the value
     * @param type the new value type
     * @return if the value was able to be transformed
     */
    boolean transform(final String path, final RawType type);

    /**
     * Get an object type
     *
     * @param path the path to the value object
     * @return the value type
     */
    RawType getType(final String path);

    /**
     * Get an object
     *
     * @param path the object path
     * @param def the object default value
     * @return the value or default value if
     * not found
     */
    Object get(final String path, final Object def);

    /**
     * Get an object
     *
     * @param path the object path
     * @return the value
     */
    default Object get(final String path) {
        return get(path, null);
    }

    /**
     * Get a string
     *
     * @param path the string path
     * @param def the string default value
     * @return the value or default value if
     * not found
     */
    String getString(final String path, final String def);

    /**
     * Get a string
     *
     * @param path the string path
     * @return the value
     */
    default String getString(final String path) {
        return getString(path, null);
    }

    /**
     * Get a character
     *
     * @param path the character path
     * @param def the character default value
     * @return the value or default value if
     * not found
     */
    char getCharacter(final String path, final char def);

    /**
     * Get a character
     *
     * @param path the character path
     * @return the value
     */
    default char getCharacter(final String path) {
        return getCharacter(path, '\0');
    }

    /**
     * Get a byte
     *
     * @param path the byte path
     * @param def the byte default value
     * @return the value or default value if
     * not found
     */
    byte getByte(final String path, final byte def);

    /**
     * Get a byte
     *
     * @param path the byte path
     * @return the value
     */
    default byte getByte(final String path) {
        return getByte(path, (byte) 0x00000000);
    }

    /**
     * Get a short
     *
     * @param path the short path
     * @param def the short default value
     * @return the value or default value if
     * not found
     */
    short getShort(final String path, final short def);

    /**
     * Get a short
     *
     * @param path the short path
     * @return the value
     */
    default short getShort(final String path) {
        return getShort(path, (byte) 0x00000000);
    }

    /**
     * Get an integer
     *
     * @param path the integer path
     * @param def the integer default value
     * @return the value or default value if
     * not found
     */
    int getInteger(final String path, final int def);

    /**
     * Get an integer
     *
     * @param path the integer path
     * @return the value
     */
    default int getInteger(final String path) {
        return getInteger(path, (byte) 0x00000000);
    }

    /**
     * Get a long
     *
     * @param path the long path
     * @param def the long default value
     * @return the value or default value if
     * not found
     */
    long getLong(final String path, final long def);

    /**
     * Get a long
     *
     * @param path the long path
     * @return the value
     */
    default long getLong(final String path) {
        return getLong(path, (byte) 0x00000000);
    }

    /**
     * Get a double
     *
     * @param path the double path
     * @param def the double default value
     * @return the value or default value if
     * not found
     */
    double getDouble(final String path, final double def);

    /**
     * Get a double
     *
     * @param path the double path
     * @return the value
     */
    default double getDouble(final String path) {
        return getDouble(path, (byte) 0x00000000);
    }

    /**
     * Get a float
     *
     * @param path the float path
     * @param def the float default value
     * @return the value or default value if
     * not found
     */
    float getFloat(final String path, final float def);

    /**
     * Get a float
     *
     * @param path the float path
     * @return the value
     */
    default float getFloat(final String path) {
        return getFloat(path, (byte) 0x00000000);
    }

    /**
     * Get a boolean
     *
     * @param path the boolean path
     * @param def the boolean default value
     * @return the value or default value if
     * not found
     */
    boolean getBoolean(final String path, final boolean def);

    /**
     * Get a boolean
     *
     * @param path the boolean path
     * @return the value
     */
    default boolean getBoolean(final String path) {
        return getBoolean(path, false);
    }

    /**
     * Get a serialized instance
     *
     * @param path the instance path
     * @param def the instance default value
     * @return the value or default value if
     * not found
     *
     * @throws IOException if the serialization fails
     * @throws ClassNotFoundException if the serialization loads a class which does not exist anymore
     */
    Object getSerialized(final String path, final Object def) throws IOException, ClassNotFoundException;

    /**
     * Get a serialized instance
     *
     * @param path the instance path
     * @return the value
     *
     * @throws IOException if the unserialization fails
     * @throws ClassNotFoundException if the unserialization loads a class which does not exist anymore
     */
    @SuppressWarnings("all")
    default Object getSerialized(final String path) throws IOException, ClassNotFoundException {
        return getSerialized(path, null);
    }

    /**
     * Get a list
     *
     * @param path the list path
     * @return the list
     */
    default List<String> getList(final String path) {
        return getList(path, Collections.emptyList());
    }

    /**
     * Get a list
     *
     * @param path the list path
     * @param def the default values to add on the
     *            list
     * @return the list
     */
    default List<String> getList(final String path, final String... def) {
        return getList(path, Arrays.asList(def));
    }

    /**
     * Get a list
     *
     * @param path the list path
     * @param ifNull the list if the path does not point
     *               to a list or is not set
     * @return the list
     */
    List<String> getList(final String path, final List<String> ifNull);

    /**
     * Get a section
     *
     * @param path the section path
     * @return the section
     */
    YamlFileHandler getSection(final String path);

    /**
     * Check if the path is set
     *
     * @param path the path
     * @return if the path is set
     */
    boolean isSet(final String path);

    /**
     * Check if none of the paths are set. If the
     * paths are empty, it will return true if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if the paths are set
     */
    default boolean isNoneSet(final String... paths) {
        return isNoneSet(Arrays.asList(paths));
    }

    /**
     * Check if none of the paths are set. If the
     * paths are empty, it will return true if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if the paths are set
     */
    default boolean isNoneSet(final Collection<String> paths) {
        if (paths.isEmpty()) return getKeys(true).isEmpty();

        for (String path : paths) {
            if (isSet(path)) return false;
        }

        return true;
    }

    /**
     * Check if all the paths are set. If the
     * paths are empty, it will return false if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if the paths are set
     */
    default boolean isAllSet(final String... paths) {
        return isNoneSet(Arrays.asList(paths));
    }

    /**
     * Check if all the paths are set. If the
     * paths are empty, it will return false if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if the paths are set
     */
    default boolean isAllSet(final Collection<String> paths) {
        if (paths.isEmpty()) return !getKeys(true).isEmpty();

        for (String path : paths) {
            if (!isSet(path)) return false;
        }

        return true;
    }

    /**
     * Check if any of the paths are not set. If the
     * paths are empty, it will return true if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if any of the paths are not set
     */
    default boolean isAnyMissing(final String... paths) {
        return isNoneSet(Arrays.asList(paths));
    }

    /**
     * Check if any of the paths are not set. If the
     * paths are empty, it will return true if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if any of the paths are not set
     */
    default boolean isAnyMissing(final Collection<String> paths) {
        if (paths.isEmpty()) return getKeys(true).isEmpty();

        for (String path : paths) {
            if (!isSet(path)) return true;
        }

        return false;
    }

    /**
     * Check if any the paths are set. If the
     * paths are empty, it will return false if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if any of the paths are set
     */
    default boolean isAnySet(final String... paths) {
        return isNoneSet(Arrays.asList(paths));
    }

    /**
     * Check if any the paths are set. If the
     * paths are empty, it will return false if the file
     * is empty (has no keys defined)
     *
     * @param paths the paths
     * @return if any of the paths are set
     */
    default boolean isAnySet(final Collection<String> paths) {
        if (paths.isEmpty()) return !getKeys(true).isEmpty();

        for (String path : paths) {
            if (isSet(path)) return true;
        }

        return false;
    }

    /**
     * Returns the first path which is set. If the paths
     * are empty, it will return always null. If no path is
     * found, it will also return null
     *
     * @param paths the paths
     * @return the first set path
     */
    @Nullable
    default String getFirstAvailable(final String... paths) {
        return getFirstAvailable(Arrays.asList(paths));
    }

    /**
     * Returns the first path which is set. If the paths
     * are empty, it will return always null. If no path is
     * found, it will also return null
     *
     * @param paths the paths
     * @return the first set path
     */
    @Nullable
    default String getFirstAvailable(final Collection<String> paths) {
        if (paths.isEmpty()) return null;

        for (String path : paths) {
            if (isSet(path)) {
                return path;
            }
        }

        return null;
    }

    /**
     * Returns the first path which is not set. If the paths
     * are empty, it will return always null. If all paths are
     * found, it will also return null
     *
     * @param paths the paths
     * @return the first missing path
     */
    @Nullable
    default String getFirstMissing(final String... paths) {
        return getFirstAvailable(Arrays.asList(paths));
    }

    /**
     * Returns the first path which is not set. If the paths
     * are empty, it will return always null. If all paths are
     * found, it will also return null
     *
     * @param paths the paths
     * @return the first missing path
     */
    @Nullable
    default String getFirstMissing(final Collection<String> paths) {
        if (paths.isEmpty()) return null;

        for (String path : paths) {
            if (!isSet(path)) {
                return path;
            }
        }

        return null;
    }

    /**
     * Check if the path is a section
     *
     * @param path the path
     * @return if the path is a section
     */
    boolean isSection(final String path);

    /**
     * Check if the path is the specified
     * type
     *
     * @param path the path
     * @param type the expected value
     * @return if the path is the specified
     * value
     */
    boolean isType(final String path, final RawType type);

    /**
     * Get all the keys
     *
     * @param deep get keys and sub keys
     * @return the keys
     */
    Collection<String> getKeys(final boolean deep);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final String value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final char value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final byte value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final short value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final int value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final long value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final double value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final float value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final boolean value);

    /**
     * Save data
     *
     * @param path the data path
     * @param value the data value
     */
    void set(final String path, final Serializable value);

    /**
     * Save data
     *
     * @param path the data path
     * @param values the data value
     */
    void set(final String path, final List<String> values);

    /**
     * Save data
     *
     * @param path the data path
     * @param section the data value
     */
    void set(final String path, final YamlFileHandler section);

    /**
     * Get the yaml file raw data
     *
     * @return the yaml raw data
     */
    Map<String, Object> raw();

    /**
     * Get the yaml handle
     *
     * @return the yaml handle
     */
    @Nullable
    Path handle();

    /**
     * Save the current yaml file handler
     * to the specified file
     *
     * @param path the yaml file path
     * @return the saved yaml file
     * @throws IOException if the file fails to save
     */
    YamlFileHandler saveTo(final Path path) throws IOException;

    /**
     * Save the yaml file
     *
     * @throws IOException if the file fails to save
     */
    void save() throws IOException;

    /**
     * Get the string representation of
     * this yaml file
     *
     * @return the string representation of
     * this yaml file
     */
    String toString();
}
