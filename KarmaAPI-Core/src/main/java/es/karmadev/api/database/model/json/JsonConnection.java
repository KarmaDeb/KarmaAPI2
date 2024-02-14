package es.karmadev.api.database.model.json;

import es.karmadev.api.database.DatabaseConnection;
import es.karmadev.api.database.result.QueryResult;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonReader;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * KarmaAPI json connection
 */
@SuppressWarnings("unused")
public class JsonConnection implements DatabaseConnection {

    final Path file;
    /**
     * -- GETTER --
     *  Get the table name
     */
    @Getter
    final String table;
    /**
     * -- GETTER --
     *  Get the parent connection
     */
    @Getter
    private final JsonConnection parent;
    JsonObject database;
    boolean pretty = false;
    boolean autoSave = false;

    public JsonConnection(final @NotNull Path file, final @Nullable JsonConnection parent, final @Nullable String table) {
        this.file = file;
        this.parent = parent;
        this.table = table;

        String raw = "";
        if (Files.exists(file)) {
            raw = PathUtilities.read(file);
        }

        String path = "";
        if (parent != null) {
            String parentPath = parent.database.getPath();
            if (parentPath.isEmpty()) {
                path = parent.database.getKey();
            } else {
                path = parentPath + parent.database.getPathSeparator() + parent.database.getKey();
            }
        }

        JsonObject tmpObject = JsonObject.newObject(path, table);
        if (raw.startsWith("{") && raw.endsWith("}")) {
            tmpObject = JsonReader.read(raw).asObject();
        }

        database = tmpObject;
        if (!database.hasChild("types") || !database.getChild("types").isObjectType()) {
            JsonObject typesObject = JsonObject.newObject((parent == null ? "" : parent.database.getPath()), "types");
            typesObject.put("schemed", false);
            database.put("types", typesObject);
            save();
        }
    }

    /**
     * Set the pretty save status
     *
     * @param status if the json database
     *               stores in pretty format
     */
    public void setPrettySave(final boolean status) {
        if (parent != null) {
            parent.setPrettySave(status);
            return;
        }

        pretty = status;
    }

    /**
     * Return if the connection supports
     * queries
     *
     * @return if the connection supports queries
     */
    @Override
    public boolean querySupported() {
        return true;
    }

    /**
     * Execute a query
     *
     * @param query the query to run
     * @return the query
     * @throws UnsupportedOperationException always
     */
    @Override
    public QueryResult execute(final String query) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("JSON does not support queries");
    }

    /**
     * Get if the database has the specified table
     *
     * @param name the table name
     * @return if the database has the table
     */
    public boolean hasTable(final String name) {
        return database.hasChild(name);
    }

    /**
     * Remove the table
     *
     * @param name the table name
     */
    public void removeTable(final String name) {
        if (database.hasChild(table)) {
            JsonInstance element = database.getChild(name);
            if (element.isObjectType()) database.removeChild(name);

            JsonObject typesObject = database.getChild("types").asObject();
            typesObject.removeChild(name);
        }
    }

    /**
     * Create a table
     *
     * @param name the table name
     * @return the child connection for the table
     * @throws UnsupportedOperationException if the table name is already taken by a non-table object
     */
    public JsonConnection createTable(final String name) throws UnsupportedOperationException {
        JsonObject typesObject = database.getChild("types").asObject();
        JsonObject child = JsonObject.newObject(database.getPath(), name);
        if (database.hasChild(name)) {
            JsonInstance element = database.getChild(name);
            if (!element.isObjectType()) throw new UnsupportedOperationException("Cannot create a table " + name + " because another field with that name already exists");

            if (typesObject.hasChild(name)) {
                String type = typesObject.getChild(name).asString();
                if (!type.equals("table")) throw new UnsupportedOperationException("Cannot create a table " + name + " because another field with that name already exists");
            }

            child = element.asObject();
        }

        if (!child.hasChild("types")) {
            JsonObject types = JsonObject.newObject(database.getPath(), "types");
            types.put("schemed", false);
            child.put("types", types);
        }

        database.put(name, child);
        JsonConnection connection = new JsonConnection(file, this, name);
        connection.database = child;

        typesObject.put(name, "table");

        return connection;
    }

    /**
     * Create a list of tables
     *
     * @param database the table database name
     * @param names the table names
     * @return the child connection for the table
     * @throws UnsupportedOperationException if the database name is already taken by another non-database object
     */
    public List<JsonConnection> createTables(final String database, final String... names) throws UnsupportedOperationException {
        JsonObject typesObject = this.database.getChild("types").asObject();
        JsonArray array = JsonArray.newArray(this.database.getPath(), database);

        if (this.database.hasChild(database)) {
            JsonInstance element = this.database.getChild(database);
            if (!element.isArrayType()) throw new UnsupportedOperationException("Cannot redefine field " + database + " because existing type is not a database");

            array = element.asArray();
        }

        boolean check = !array.isEmpty();
        Map<String, JsonObject> map = new HashMap<>();
        if (check) {
            for (JsonInstance element : array) {
                if (!element.isObjectType()) throw new UnsupportedOperationException("Cannot add object to non-object list!");

                JsonObject object = element.asObject();
                if (!object.hasChild("name")) throw new UnsupportedOperationException("Cannot add table to non-table list!");

                map.put(object.getChild("name").asString(), element.asObject());
            }
        }

        List<JsonConnection> connections = new ArrayList<>();
        for (String name : names) {
            JsonObject child = JsonObject.newObject(this.database.getPath(), name);
            if (map.containsKey(name)) child = map.get(name);

            if (!child.hasChild("types")) {
                JsonObject types = JsonObject.newObject(this.database.getPath() + '.' + name, "types");
                types.put("schemed", false);
                child.put("types", types);
            }
            array.add(child);

            JsonConnection connection = new JsonConnection(file, this, name);
            connection.database = child;

            typesObject.put(name, "table");
            connections.add(connection);
        }
        this.database.put(database, array);

        return connections;
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-primitive
     * object
     */
    public void set(final String key, final Map<String, Object> value) throws UnsupportedOperationException {
        JsonObject typesObject = this.database.getChild("types").asObject();
        if (database.hasChild(key)) {
            JsonInstance element = database.getChild(key);
            if (!element.isObjectType()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");
        }

        if (value != null) {
            JsonObject element = JsonReader.readTree(value);
            database.put(key, element);
            typesObject.put(key, "map");
        } else {
            database.removeChild(key);
            typesObject.removeChild(key);
        }
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-primitive object
     */
    public void set(final String key, final String value) throws UnsupportedOperationException {
        JsonObject typesObject = this.database.getChild("types").asObject();
        if (database.hasChild(key)) {
            JsonInstance element = database.getChild(key);
            if (!element.isNativeType()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonNative primitive = element.asNative();
            if (!primitive.isString()) throw new UnsupportedOperationException("Cannot set string to non-string field!");
        }

        if (value != null) {
            database.put(key, value);
            typesObject.put(key, "string");
        } else {
            database.removeChild(key);
            typesObject.removeChild(key);
        }
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-primitive object
     */
    public void set(final String key, final Number value) throws UnsupportedOperationException {
        if (database.hasChild(key)) {
            JsonInstance element = database.getChild(key);
            if (!element.isNativeType()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonNative primitive = element.asNative();
            if (!primitive.isNumber()) throw new UnsupportedOperationException("Cannot set number to non-number field!");

            if (value != null) {
                String currentType = getType(key);
                String typeName = getTypeName(value);

                JsonObject typesObject = database.getChild("types").asObject();
                if (!typeName.equalsIgnoreCase(currentType)) {
                    throw new UnsupportedOperationException("Cannot set number of " + key + " to non-" + typeName + " value!");
                }
            }
        }

        if (value != null) {
            database.put(key, value);

            JsonObject typesObject = database.getChild("types").asObject();
            String typeName = getTypeName(value);
            typesObject.put(key, typeName);
        } else {
            database.removeChild(key);

            JsonObject typesObject = database.getChild("types").asObject();
            typesObject.removeChild(key);
        }
    }

    private String getTypeName(final @NotNull Number value) {
        if (value instanceof Byte) {
            return "byte";
        }
        if (value instanceof Short) {
            return "short";
        }
        if (value instanceof Integer) {
            return "integer";
        }
        if (value instanceof Long) {
            return "long";
        }
        if (value instanceof Float) {
            return "float";
        }

        return "double";
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-primitive object
     */
    public void set(final String key, final Boolean value) throws UnsupportedOperationException {
        JsonObject typesObject = database.getChild("types").asObject();
        if (database.hasChild(key)) {
            JsonInstance element = database.getChild(key);
            if (!element.isNativeType()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonNative primitive = element.asNative();
            if (!primitive.isBoolean()) throw new UnsupportedOperationException("Cannot set boolean to non-boolean field!");
        }

        if (value != null) {
            database.put(key, value);

            typesObject.put(key, "boolean");
        } else {
            database.removeChild(key);
            typesObject.removeChild(key);
        }
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-list object
     */
    public void setStringList(final String key, final List<String> value) throws UnsupportedOperationException {
        JsonObject typesObject = database.getChild("types").asObject();
        JsonArray array = JsonArray.newArray(this.database.getPath(), key);
        if (database.hasChild(key)) {
            JsonInstance element = database.getChild(key);
            if (!element.isArrayType()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            array = element.asArray();
        }

        boolean check = !array.isEmpty();
        if (check) {
            JsonInstance firstElement = array.get(0);
            if (!firstElement.isNativeType()) throw new UnsupportedOperationException("Cannot add primitive to non-primitive list!");

            JsonNative primitive = firstElement.asNative();
            if (!primitive.isString()) throw new UnsupportedOperationException("Cannot add string to non-string list!");
        }

        if (value != null) {
            for (String s : value) array.add(s);
            database.put(key, array);

            typesObject.put(key, "stringList");
        } else {
            database.removeChild(key);
            typesObject.removeChild(key);
        }
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-list object
     */
    public void setNumberList(final String key, final List<Number> value) throws UnsupportedOperationException {
        JsonObject typesObject = database.getChild("types").asObject();
        JsonArray array = JsonArray.newArray(this.database.getPath(), key);
        if (database.hasChild(key)) {
            JsonInstance element = database.getChild(key);
            if (!element.isArrayType()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            array = element.asArray();
        }

        boolean check = !array.isEmpty();
        if (check) {
            JsonInstance firstElement = array.get(0);
            if (!firstElement.isNativeType()) throw new UnsupportedOperationException("Cannot add primitive to non-primitive list!");

            JsonNative primitive = firstElement.asNative();
            if (!primitive.isNumber()) throw new UnsupportedOperationException("Cannot add number to non-number list!");
        }

        if (value != null) {
            for (Number n : value) array.add(n);
            database.put(key, array);

            typesObject.put(key, "numberList");
        } else {
            database.removeChild(key);
            typesObject.removeChild(key);
        }
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-list object
     */
    public void setBooleanList(final String key, final List<Boolean> value) throws UnsupportedOperationException {
        JsonObject typesObject = database.getChild("types").asObject();
        JsonArray array = JsonArray.newArray(this.database.getPath(), key);
        if (database.hasChild(key)) {
            JsonInstance element = database.getChild(key);
            if (!element.isArrayType()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            array = element.asArray();
        }

        boolean check = !array.isEmpty();
        if (check) {
            JsonInstance firstElement = array.get(0);
            if (!firstElement.isNativeType()) throw new UnsupportedOperationException("Cannot add primitive to non-primitive list!");

            JsonNative primitive = firstElement.asNative();
            if (!primitive.isBoolean()) throw new UnsupportedOperationException("Cannot add boolean to non-boolean list!");
        }

        if (value != null) {
            for (boolean b : value) array.add(b);
            database.put(key, array);

            typesObject.put(key, "booleanList");
        } else {
            database.removeChild(key);
            typesObject.removeChild(key);
        }
    }

    /**
     * Get a map
     *
     * @param key the map key
     * @return the map
     */
    public Map<String, Object> getMap(final String key) {
        if (!database.hasChild(key)) return null;
        JsonInstance element = database.getChild(key);

        if (getType(key).equals("map")) {
            return element.getTree();
        }

        return null;
    }

    /**
     * Get a string
     *
     * @param key the string key
     * @return the string
     */
    public String getString(final String key) {
        JsonNative primitive = getPrimitive(key);
        if (primitive == null) return null;

        if (primitive.isString()) return primitive.getAsString();
        return null;
    }

    /**
     * Get a number
     *
     * @param key the number key
     * @return the number
     */
    public Number getNumber(final String key) {
        JsonNative primitive = getPrimitive(key);
        if (primitive == null) return null;

        if (primitive.isNumber()) return primitive.getAsNumber();
        return null;
    }

    /**
     * Get a boolean
     *
     * @param key the boolean key
     * @return the boolean
     */
    public boolean getBoolean(final String key) {
        JsonNative primitive = getPrimitive(key);
        if (primitive == null) return false;

        if (primitive.isBoolean()) return primitive.asBoolean();
        return false;
    }

    /**
     * Get a list of strings
     *
     * @param key the list key
     * @return the list
     */
    public List<String> getStringList(final String key) {
        if (!database.hasChild(key)) return null;
        JsonInstance element = database.getChild(key);

        if (!element.isArrayType()) return null;
        JsonArray array = element.asArray();

        List<String> strings = new ArrayList<>();
        for (JsonInstance child : array) {
            if (!child.isNativeType()) continue;
            JsonNative primitive = child.asNative();

            if (!primitive.isString()) continue;
            strings.add(primitive.getAsString());
        }

        return strings;
    }

    /**
     * Get a list of numbers
     *
     * @param key the list key
     * @return the list
     */
    public List<Number> getNumberList(final String key) {
        if (!database.hasChild(key)) return null;
        JsonInstance element = database.getChild(key);

        if (!element.isArrayType()) return null;
        JsonArray array = element.asArray();

        List<Number> numbers = new ArrayList<>();
        for (JsonInstance child : array) {
            if (!child.isNativeType()) continue;
            JsonNative primitive = child.asNative();

            if (!primitive.isNumber()) continue;
            numbers.add(primitive.getAsNumber());
        }

        return numbers;
    }

    /**
     * Get a list of booleans
     *
     * @param key the list key
     * @return the list
     */
    public List<Boolean> getBooleanList(final String key) {
        if (!database.hasChild(key)) return null;
        JsonInstance element = database.getChild(key);

        if (!element.isArrayType()) return null;
        JsonArray array = element.asArray();

        List<Boolean> booleans = new ArrayList<>();
        for (JsonInstance child : array) {
            if (!child.isNativeType()) continue;
            JsonNative primitive = child.asNative();

            if (!primitive.isBoolean()) continue;
            booleans.add(primitive.getAsBoolean());
        }

        return booleans;
    }

    /**
     * Get a list of tables
     *
     * @param key the list key
     * @return the list
     */
    public List<JsonConnection> getTableList(final String key) {
        if (!database.hasChild(key)) return null;
        JsonInstance element = database.getChild(key);

        if (!element.isArrayType()) return null;
        JsonArray array = element.asArray();

        List<JsonConnection> tables = new ArrayList<>();
        for (JsonInstance child : array) {
            if (!child.isObjectType()) continue;
            JsonObject object = child.asObject();

            if (!object.hasChild("name")) continue;
            String tableName = object.getChild("name").asString();

            JsonConnection connection = new JsonConnection(file, this, tableName);
            connection.database = object;

            tables.add(connection);
        }

        return tables;
    }

    /**
     * Get the type of the field key
     *
     * @param key the key
     * @return the key type
     */
    public String getType(final String key) {
        JsonObject typesObject = database.getChild("types").asObject();
        if (typesObject.hasChild(key) && typesObject.getChild(key).isNativeType()) {
            return typesObject.getChild(key).asString();
        }

        return "null";
    }

    /**
     * Get if the key is set
     *
     * @param key the key
     * @return if the key is set
     */
    public boolean isSet(final String key) {
        return database.hasChild(key);
    }

    /**
     * Get all the database keys
     *
     * @return the database keys
     */
    public Collection<String> getKeys() {
        JsonObject typesObject = database.getChild("types").asObject();
        return typesObject.getKeys(true);
    }

    /**
     * Save all changes into the local database
     */
    public boolean save() {
        if (parent == null) {
            String raw = database.toString(pretty);
            return PathUtilities.write(file, raw);
        }

        return parent.save();
    }

    private JsonNative getPrimitive(final String key) {
        if (!database.hasChild(key)) return null;
        JsonInstance element = database.getChild(key);

        if (!element.isNativeType()) return null;
        return element.asNative();
    }
}
