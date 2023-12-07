package es.karmadev.api.kson;

import es.karmadev.api.kson.object.JsonNull;
import es.karmadev.api.kson.io.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.StringWriter;

/**
 * Represents a json element.
 * This element can be of any type
 */
@SuppressWarnings("unused")
public interface JsonInstance {

    /**
     * Get the instance path separator. A path
     * separator is a special character which
     * splits the path into a tree of paths.
     * For instance "this.is.a.path" would split
     * into:
     * <code>
     *     "this": {
     *         "is": {
     *             "a": {
     *                 "path: #Element here
     *             }
     *         }
     *     }
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
    char getPathSeparator();

    /**
     * Get the key this instance
     * pertains to
     *
     * @return the key associated with
     * that instance
     */
    @NotNull
    String getKey();

    /**
     * Get the element path
     *
     * @return the element path
     */
    String getPath();

    /**
     * Get if the instance is a native
     * json type. A native json type is
     * a json type which holds a native
     * java type (including string, which
     * in java are not natives)
     *
     * @return if the instance is a native type
     */
    default boolean isNativeType() {
        return this instanceof JsonNative;
    }

    /**
     * Get if the instance is an array
     * json type. A json array type is
     * a json type which holds data
     * like a collections does.
     *
     * @return if the instance is an array
     */
    default boolean isArrayType() { return this instanceof JsonArray; }

    /**
     * Get if the instance is an object
     * json type. An object json type
     * is a json type which holds data
     * like a map does, with a key-value
     * data schema, in where the key is always
     * a string, and the value is always a
     * native type or another object
     *
     * @return if the instance is an object
     */
    default boolean isObjectType() {
        return this instanceof JsonObject;
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
    boolean isEmpty();

    /**
     * Get if the object is a {@link es.karmadev.api.kson.object.JsonNull null}
     * json instance
     *
     * @return if the element is null
     */
    default boolean isNull() {
        return this.equals(JsonNull.get());
    }

    /**
     * Get the current element as a
     * json object
     *
     * @return the element as a json object.
     * @throws UnsupportedOperationException if the element
     * cannot be converted into the requested element.
     */
    default JsonObject asObject() throws UnsupportedOperationException {
        if (this instanceof JsonObject) {
            return (JsonObject) this;
        }

        String type = getClass().getSimpleName();
        throw new UnsupportedOperationException(String.format(
                "Cannot converse from %s to object",
                type
        ));
    }

    /**
     * Get the current element as a
     * json array
     *
     * @return the element as a json array.
     * @throws UnsupportedOperationException if the element
     * cannot be converted into the requested element.
     */
    default JsonArray asArray() throws UnsupportedOperationException {
        if (this instanceof JsonArray) {
            return (JsonArray) this;
        }

        String type = getClass().getSimpleName();
        throw new UnsupportedOperationException(String.format(
                "Cannot converse from %s to array",
                type
        ));
    }

    /**
     * Get the current element as a
     * json native
     *
     * @return the element as a json native
     * @throws UnsupportedOperationException if the element
     * cannot be converted into the requested element
     */
    default JsonNative asNative() throws UnsupportedOperationException {
        if (this instanceof JsonNative) {
            return (JsonNative) this;
        }

        String type = getClass().getSimpleName();
        throw new UnsupportedOperationException(String.format(
                "Cannot converse from %s to native",
                type
        ));
    }

    /**
     * Get the string representation of
     * the element
     *
     * @param pretty if the value should be pretty
     * @return the string representation
     */
    default String toString(final boolean pretty) {
        return toString(pretty, 0);
    }

    /**
     * Get the string representation of
     * the element
     *
     * @param indentation the indentation level
     * @return the string representation
     */
    default String toString(final int indentation) {
        return toString(true, indentation);
    }

    /**
     * Get the string representation of
     * the element
     *
     * @param pretty if the value should be pretty
     * @param indentation the indentation level
     * @return the string representation
     */
    default String toString(final boolean pretty, final int indentation) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(this);
        jsonWriter.setPrettyPrinting(pretty);
        jsonWriter.setIndentation(indentation);

        jsonWriter.export(writer);
        return writer.toString();
    }

    /**
     * Clone the element on the new path and
     * the new path separator
     *
     * @param newPath the path
     * @param pathSeparator the path separator
     * @return the new instance
     */
    JsonInstance clone(final String newPath, final char pathSeparator);
}
