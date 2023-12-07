package es.karmadev.api.kson.object;

import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a simple json object
 */
public class SimpleObject implements JsonObject {

    private final static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    private final String path;
    private final char pathSeparator;
    private final String key;

    private final Map<String, JsonInstance> instances = new LinkedHashMap<>();

    /**
     * Create a new simple json
     * object
     *
     * @param path the objet current path
     * @param pathSeparator the object path separator
     */
    public SimpleObject(final @NotNull String path, final char pathSeparator) {
        this(path, pathSeparator, new HashMap<>());
    }

    /**
     * Create a new simple json
     * object
     *
     * @param path the object current path
     * @param pathSeparator the object path separator
     * @param values the object values
     */
    public SimpleObject(final @NotNull String path, final char pathSeparator, final Map<String, JsonInstance> values) {
        this.path = path;
        this.pathSeparator = pathSeparator;

        String sanitized = sanitizedSeparator();
        String key = path;
        if (path.contains(String.valueOf(pathSeparator))) {
            String[] data = path.split(sanitized);
            key = data[data.length - 1];
        }
        this.key = key;

        if (values != null && !values.isEmpty())
            this.instances.putAll(values);
    }

    /**
     * Get the object path separator. A path
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
     * Get the key this object
     * pertains to
     *
     * @return the key associated with
     * that object
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
     * Clone the element on the new path and
     * the new path separator
     *
     * @param newPath       the path
     * @param pathSeparator the path separator
     * @return the new instance
     */
    @Override
    public JsonInstance clone(final String newPath, final char pathSeparator) {
        return new SimpleObject(newPath, pathSeparator, instances);
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
     * Get a child element
     *
     * @param path           the path to the element
     * @param defaultElement the default element if there's no
     *                       one set
     * @return the instance
     */
    @Override
    public JsonInstance getChild(final String path, final JsonInstance defaultElement) {
        if (!instances.containsKey(path)) {
            String subPath = this.path + pathSeparator + path;
            return instances.getOrDefault(subPath, defaultElement);
        }

        return instances.getOrDefault(path, defaultElement);
    }

    /**
     * Get if the object has a child
     * element
     *
     * @param path the path
     * @return if the element has the
     * child element
     */
    @Override
    public boolean hasChild(final String path) {
        if (!instances.containsKey(path)) {
            String subPath = this.path + pathSeparator + path;
            return instances.containsKey(subPath);
        }

        return true;
    }

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
    @Override
    public Collection<String> getKeys(final boolean deep) {
        if (deep) {
            return instances.keySet();
        }

        List<String> strict = new ArrayList<>();

        String sanitized = sanitizedPath();
        String separator = sanitizedSeparator();

        Pattern singlePattern = Pattern.compile("^(?<current>" + sanitized + ")" + separator + "[^" + separator + "]+$");
        for (String key : instances.keySet()) {
            Matcher matcher = singlePattern.matcher(key);
            if (matcher.matches()) {
                strict.add(key.replaceFirst(sanitized + separator, ""));
            }
        }

        return strict;
    }

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
    @Override
    public Map<String, JsonInstance> getAsMap() {
        return Collections.unmodifiableMap(instances);
    }

    /**
     * Put an element into the object.
     * The path of the element will be ignored and
     * replaced with the one specified in the path
     * parameter. In order to keep the instance element
     * path, refer to the {@link #insert(JsonInstance) insertion}
     * method.
     *
     * @param path    the element path
     * @param element the element to write
     */
    @Override
    public void put(final String path, final JsonInstance element) {
        String finalPath = this.path + pathSeparator + path;
        JsonInstance clone = element.clone(finalPath, pathSeparator);

        instances.put(finalPath, clone);
    }

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
    @Override
    public void insert(final JsonInstance instance) {
        String instancePath = instance.getPath();
        if (instancePath.isEmpty()) {
            instancePath = UUID.randomUUID().toString()
                    .replace("-", "");
        }

        put(instancePath, instance);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<JsonInstance> iterator() {
        List<JsonInstance> childInstances = new ArrayList<>();

        String sanitized = sanitizedPath();
        String separator = sanitizedSeparator();

        Pattern singlePattern = Pattern.compile("^(?<current>" + sanitized + ")" + separator + "[^" + separator + "]+$");
        for (String key : instances.keySet()) {
            Matcher matcher = singlePattern.matcher(key);
            if (matcher.matches()) {
                JsonInstance element = instances.get(key);
                childInstances.add(element);
            }
        }

        return childInstances.iterator();
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(this);
        jsonWriter.setPrettyPrinting(true);
        jsonWriter.export(writer);

        return writer.toString();
    }
}
