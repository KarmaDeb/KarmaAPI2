package es.karmadev.api.kson;

import es.karmadev.api.kson.object.SimpleObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a JSON object instance, which is a key-value map.
 * This interface extends {@link Iterable} to provide convenient
 * iteration over the elements of the JSON object. This can be
 * particularly useful for scenarios where you want to iterate
 * through the key-value pairs without explicitly calling
 * {@link #getKeys(boolean)} and then using {@link #getChild(String)}
 * for each key.
 *
 * <p>Note: While this interface extends {@link Iterable},
 * the semantics of the iteration may differ from traditional
 * collections. The iteration includes both the direct key-value
 * pairs of this object and the key-value pairs of its child objects
 * in a recursive manner, providing a comprehensive traversal
 * of the JSON structure. For a real collection json, refer to {@link JsonArray}</p>
 */
@SuppressWarnings("unused")
public interface JsonObject extends JsonInstance, Iterable<JsonInstance> {

    /**
     * Create a new json object
     *
     * @return the object
     */
    static JsonObject newObject() {
        return newObject("", '.');
    }

    /**
     * Create a new json object
     *
     * @param path the object path
     * @return the new object
     */
    static JsonObject newObject(final String path) {
        return newObject(path, '.');
    }

    /**
     * Create a new json object
     *
     * @param pathSeparator the path separator
     * @return the new object
     */
    static JsonObject newObject(final char pathSeparator) {
        return newObject("", '.');
    }

    /**
     * Create a new json object
     *
     * @param path the object path
     * @param pathSeparator the object path separator
     * @return the new object
     */
    static JsonObject newObject(final String path, final char pathSeparator) {
        return new SimpleObject(path, pathSeparator);
    }

    /**
     * Get a child element on the object
     *
     * @param path the path to the element
     * @return the child element
     */
    @Nullable
    default JsonInstance getChild(final String path) {
        return getChild(path, null);
    }

    /**
     * Get a child element
     *
     * @param path the path to the element
     * @param defaultElement the default element if there's no
     *                       one set
     * @return the instance
     */
    JsonInstance getChild(final String path, final JsonInstance defaultElement);

    /**
     * Get if the object has a child
     * element
     *
     * @param path the path
     * @return if the element has the
     * child element
     */
    boolean hasChild(final String path);

    /**
     * Get all the object keys. When deep
     * is true, this will also iterate through
     * the children's key on cascade. What does
     * cascade implies? It means the recursion
     * is performed exclusively down, so only child
     * keys are recurse, no parent key will be returned
     * by the call of this method, never.
     *
     * @param deep if the search should be recursive
     * @return the object keys
     */
    Collection<String> getKeys(final boolean deep);

    /**
     * Get the json object as a map object.
     * The map returned by this method, contains,
     * exclusively, the keys hold in the current path,
     * meaning no recursion is performed during this
     * call.
     *
     * @return the json object as a map
     * object.
     */
    Map<String, JsonInstance> getAsMap();

    /**
     * Put a string into the object element
     *
     * @param path the path to set the string at
     * @param value the string
     */
    default void put(final String path, final String value) {
        put(path, JsonNative.forSequence(path, value));
    }

    /**
     * Put a number into the object element
     *
     * @param path the path to set the number at
     * @param number the number
     */
    default void put(final String path, final Number number) {
        put(path, JsonNative.forNumber(path, number));
    }

    /**
     * Put a boolean into the object element
     *
     * @param path the path to set the boolean at
     * @param bool the boolean
     */
    default void put(final String path, final Boolean bool) {
        put(path, JsonNative.forBoolean(path, bool));
    }

    /**
     * Put an element into the object.
     * The path of the element will be ignored and
     * replaced with the one specified in the path
     * parameter. In order to keep the instance element
     * path, refer to the {@link #insert(JsonInstance) insertion}
     * method.
     *
     * @param path the element path
     * @param element the element to write
     */
    void put(final String path, final JsonInstance element);

    /**
     * Insert an element into this element. The
     * expected behaviour of this element is that
     * the element to write path is keep under the
     * current path. Writing an instance on this
     * element won't make the other element (if any)
     * that holds the element to lose the element.
     *
     * @param instance the instance to write
     */
    void insert(final JsonInstance instance);

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
    default boolean isEmpty() {
        return getKeys(false).isEmpty();
    }

    /**
     * Execute an operation for each element of
     * this object and their child elements
     *
     * @param consumer the element consumer
     */
    default void deepForEach(final Consumer<JsonInstance> consumer) {
        Collection<String> keys = getKeys(false);

        List<JsonObject> childObjects = new ArrayList<>();
        for (String key : keys) {
            JsonInstance instance = getChild(key);
            if (instance == null) continue;

            if (instance instanceof JsonObject) {
                JsonObject object = (JsonObject) instance;
                childObjects.add(object);
                continue;
            }

            consumer.accept(instance);
        }

        for (JsonObject object : childObjects) {
            object.deepForEach(consumer);
        }
    }
}
