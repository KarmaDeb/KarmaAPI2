package es.karmadev.api.kson.object.type;

import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a native boolean
 * element
 */
public final class NativeBoolean implements JsonNative {

    private final String path;
    private final char pathSeparator;
    private final String key;
    private final boolean bool;

    /**
     * Initialize the native boolean
     *
     * @param bool the boolean value
     */
    public NativeBoolean(final boolean bool) {
        this("", '.', bool);
    }

    /**
     * Initialize the native boolean
     *
     * @param path the element path
     * @param pathSeparator the path separator
     * @param bool the boolean
     */
    public NativeBoolean(final String path, final char pathSeparator, final boolean bool) {
        this.path = path;
        this.pathSeparator = pathSeparator;

        String key = path;
        if (path.contains(".")) {
            String[] data = path.split("\\.");
            key = data[data.length - 1];
        }
        this.key = key;

        this.bool = bool;
    }

    /**
     * Get the instance path separator. A path
     * separator is a special character which
     * splits the path into a tree of paths.
     * For instance "this.is.a.path" would split
     * into:
     * <code>
     * "this": {
     * "is": {
     * "a": {
     * "path: #Element here
     * }
     * }
     * }
     * </code>
     * A path separator can be specified during the
     * initialization of a {@link JsonInstance parent}
     * element. In order to include the char on the path,
     * for example, if we have our value in "this.is.a.path"
     * instead of on a tree-map, we can escape the special
     * character, so it will be treated as a path character instead
     * of a separator
     *
     * @return the object path separator.
     */
    @Override
    public char getPathSeparator() {
        return pathSeparator;
    }

    /**
     * Get the key this instance
     * pertains to
     *
     * @return the key associated with
     * that instance
     */
    @Override
    public @NotNull String getKey() {
        return key;
    }

    /**
     * Get the element path
     *
     * @return the element path
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * Get if the object is empty. The expected
     * behaviours are the following:
     * <ul>
     *     <li>{@link JsonObject objects} - Return true if the object has no keys defined</li>
     *     <li>{@link JsonArray arrays} - Return true if the array has no elements</li>
     *     <li>{@link JsonNative natives} - Returns true if the native type is string, and is empty, or if the native type is null</li>
     * </ul>
     *
     * @return if the object is empty
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns whether the element
     * is a string
     *
     * @return if the element is a string
     */
    @Override
    public boolean isString() {
        return false;
    }

    /**
     * Returns whether the element
     * is a number
     *
     * @return if the element is a number
     */
    @Override
    public boolean isNumber() {
        return false;
    }

    /**
     * Returns whether the element is a
     * boolean
     *
     * @return if the element is a boolean
     */
    @Override
    public boolean isBoolean() {
        return true;
    }

    /**
     * Get the string value of the element
     *
     * @return the string value
     * @throws UnsupportedOperationException if the element
     *                                       is not a string
     */
    @Override
    public @NotNull String getString() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot cast boolean to string");
    }

    /**
     * Get the number value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     *                                       is not a number
     */
    @Override
    public @NotNull Number getNumber() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot cast boolean to number");
    }

    /**
     * Get the boolean value of the element
     *
     * @return the boolean element
     * @throws UnsupportedOperationException if the element
     *                                       is not a number
     */
    @Override
    public boolean getBoolean() throws UnsupportedOperationException {
        return bool;
    }

    /**
     * Get the string value of the element. The
     * main difference between this method and
     * {@link #getString()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the string value
     */
    @Override
    public @Nullable String getAsString() {
        return String.valueOf(bool);
    }

    /**
     * Get the number value of the element. The
     * main difference between this method and
     * {@link #getNumber()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the number element
     */
    @Override
    public @Nullable Number getAsNumber() {
        return (bool ? (byte) 1 : (byte) 0);
    }

    /**
     * Get the boolean value of the element. The
     * main difference between this method and
     * {@link #getBoolean()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the boolean element
     */
    @Override
    public @Nullable Boolean getAsBoolean() {
        return bool;
    }
}
