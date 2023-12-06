package es.karmadev.api.kson.object;

import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents a simple json array
 */
public class SimpleArray implements JsonArray {

    private final static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    private final List<JsonInstance> childElements = new ArrayList<>();
    private final String path;
    private final String key;
    private final char pathSeparator;

    /**
     * Create a new array
     *
     * @param path the array path
     * @param pathSeparator the array path separator
     */
    public SimpleArray(final @NotNull String path, final char pathSeparator) {
        this(path, pathSeparator, new ArrayList<>());
    }

    /**
     * Create a new array
     *
     * @param path the array path
     * @param pathSeparator the array path separator
     * @param elements the array elements
     */
    public SimpleArray(final String path, final char pathSeparator, final List<JsonInstance> elements) {
        this.path = path;
        this.pathSeparator = pathSeparator;

        String sanitized = sanitizedSeparator();
        String key = path;
        if (path.contains(String.valueOf(pathSeparator))) {
            String[] data = path.split(sanitized);
            key = data[data.length - 1];
        }
        this.key = key;

        if (!childElements.isEmpty())
            childElements.addAll(elements);
    }

    /**
     * Get all the array elements
     *
     * @return the array elements
     */
    @Override
    public Collection<JsonInstance> getElements() {
        return Collections.unmodifiableList(childElements);
    }

    /**
     * Get the array size
     *
     * @return the array size
     */
    @Override
    public int size() {
        return childElements.size();
    }

    /**
     * Get if the array contains the element.
     * This method is not compatible with calls in
     * where the element is a {@link JsonArray array}. That's
     * because JSON does not allow to store json arrays
     * in arrays, take also in mind that, in case of
     * {@link JsonObject object} checks, this method will
     * check that all the keys matches the array object key
     * values, including child elements, which can lead to high
     * computing usage. We instead recommend to iterate through
     * the elements and use a schema validator on the element
     * to validate that the element is the one you are expecting
     *
     * @param element the element to check with
     * @return if the array contains an element with
     * the same values
     */
    @Override
    public boolean contains(final JsonInstance element) {
        if (element == null || element.isNull()) return false;
        if (element.isNativeType()) {
            JsonNative jsonNative = element.asNative();
            if (jsonNative.isString()) {
                String stringValue = jsonNative.getString();
                return childElements.stream().anyMatch((child) -> {
                    if (!child.isNativeType()) return false;
                    JsonNative childNative = child.asNative();

                    if (!childNative.isString()) return false;
                    return childNative.getString().equals(stringValue);
                });
            } else if (jsonNative.isNumber()) {
                Number numberValue = jsonNative.getNumber();
                return childElements.stream().anyMatch((child) -> {
                    if (!child.isNativeType()) return false;
                    JsonNative childNative = child.asNative();

                    if (!childNative.isNumber()) return false;
                    return childNative.getNumber().equals(numberValue);
                });
            } else if (jsonNative.isBoolean()) {
                boolean bool = jsonNative.getBoolean();
                return childElements.stream().anyMatch((child) -> {
                    if (!child.isNativeType()) return false;
                    JsonNative childNative = child.asNative();

                    if (!childNative.isBoolean()) return false;
                    return childNative.getBoolean() == bool;
                });
            }
        }

        if (element.isObjectType()) {
            JsonObject object = element.asObject();
            return childElements.stream().anyMatch((instance) -> {
               if (!instance.isObjectType()) return false;

               JsonObject cObj = instance.asObject();
               return compareObjects(object, cObj);
            });
        }

        if (element.isArrayType()) {
            JsonArray other = element.asArray();
            for (JsonInstance otherElement : other) {
                if (!contains(otherElement)) return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Get an instance by its index on the
     * array. The index is nothing but the
     * position on the array of the element.
     *
     * @param index the element index
     * @return the element on the specified index
     * @throws IndexOutOfBoundsException if the index
     *                                   is out of bounds of the array
     */
    @Override
    public JsonInstance get(final int index) throws IndexOutOfBoundsException {
        return childElements.get(index);
    }

    /**
     * Set the instance at the specified array
     * position
     *
     * @param index       the index to modify
     * @param newInstance the new value
     * @return if the operation was successful
     * @throws IndexOutOfBoundsException if the index
     *                                   is out of bounds of the array
     */
    @Override
    public boolean set(final int index, final JsonInstance newInstance) throws IndexOutOfBoundsException {
        if (newInstance == null || newInstance.isNull()) return false;
        childElements.set(index, newInstance);

        return true;
    }

    /**
     * Remove an element on the specified
     * array position
     *
     * @param index the array position
     * @return if the operation was successful
     * @throws IndexOutOfBoundsException if the index
     *                                   is out of bounds of the array
     */
    @Override
    public boolean remove(final int index) throws IndexOutOfBoundsException {
        return childElements.remove(index) != null;
    }

    /**
     * remove an element from the array
     *
     * @param instance the element to remove
     * @return if the operation was successful
     */
    @Override
    public boolean remove(final JsonInstance instance) {
        return childElements.remove(instance);
    }

    /**
     * Add an instance to the array
     *
     * @param instance the instance to add
     * @return if the operation was successful
     */
    @Override
    public boolean add(final JsonInstance instance) {
        return childElements.add(instance);
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
     * Get the object sanitized path
     *
     * @return the sanitized path
     */
    private String sanitizedPath() {
        return SPECIAL_REGEX_CHARS.matcher(path).replaceAll("\\\\$0");
    }

    /**
     * Get the object sanitized path
     * separator
     *
     * @return the sanitized separator
     */
    private String sanitizedSeparator() {
        return SPECIAL_REGEX_CHARS.matcher(String.valueOf(pathSeparator)).replaceAll("\\\\$0");
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
        return childElements.isEmpty();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<JsonInstance> iterator() {
        return new ArrayList<>(childElements).iterator();
    }

    /**
     * Compare the object1 with the
     * object 2
     *
     * @param object1 the object 1
     * @param object2 the object 2
     * @return if the object matches
     */
    private static boolean compareObjects(final JsonObject object1, final JsonObject object2) {
        Collection<String> o1keys = object1.getKeys(true);
        Collection<String> o2keys = object2.getKeys(true);

        if (!o1keys.equals(o2keys)) return false;
        for (String key : o1keys) {
            JsonInstance instance1 = object1.getChild(key);
            JsonInstance instance2 = object2.getChild(key);

            if (!Objects.equals(instance1, instance2)) return false;
        }

        return true;
    }
}
