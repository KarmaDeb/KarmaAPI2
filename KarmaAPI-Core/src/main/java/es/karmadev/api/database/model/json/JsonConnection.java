package es.karmadev.api.database.model.json;

import com.google.gson.*;
import es.karmadev.api.database.DatabaseConnection;
import es.karmadev.api.database.result.QueryResult;
import es.karmadev.api.file.util.PathUtilities;
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

    private final Path file;
    private final String table;
    private final JsonConnection parent;
    private JsonObject database;
    private boolean pretty = false;

    public JsonConnection(final @NotNull Path file, final @Nullable JsonConnection parent, final @Nullable String table) {
        this.file = file;
        this.parent = parent;
        this.table = table;

        String raw = "";
        if (Files.exists(file)) {
            raw = PathUtilities.read(file);
        }
        JsonObject tmpObject = new JsonObject();
        if (raw.startsWith("{") && raw.endsWith("}")) {
            Gson gson = new GsonBuilder().create();
            tmpObject = gson.fromJson(raw, JsonObject.class);
        }

        database = tmpObject;
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
        return false;
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
        throw new UnsupportedOperationException("Json database cannot perform queries!");
    }

    /**
     * Get if the database has the specified table
     *
     * @param name the table name
     * @return if the database has the table
     */
    public boolean hasTable(final String name) {
        return database.has(name);
    }

    /**
     * Remove the table
     *
     * @param name the table name
     */
    public void removeTable(final String name) {
        if (database.has(table)) {
            JsonElement element = database.get(name);
            if (element.isJsonObject()) database.remove(name);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(name);
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
        JsonObject child = new JsonObject();
        if (database.has(name)) {
            JsonElement element = database.get(name);
            if (!element.isJsonObject()) throw new UnsupportedOperationException("Cannot create a table " + name + " because another field with that name already exists");

            JsonObject typesObject = database.getAsJsonObject("types");
            if (typesObject.has(name)) {
                String type = typesObject.get(name).getAsString();
                if (!type.equals("table")) throw new UnsupportedOperationException("Cannot create a table " + name + " because another field with that name already exists");
            }

            child = element.getAsJsonObject();
        }

        database.add(name, child);
        JsonConnection connection = new JsonConnection(file, this, name);
        connection.database = child;

        JsonObject typesObject = database.getAsJsonObject("types");
        typesObject.addProperty(name, "table");

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
        JsonArray array = new JsonArray();

        if (this.database.has(database)) {
            JsonElement element = this.database.get(database);
            if (!element.isJsonArray()) throw new UnsupportedOperationException("Cannot redefine field " + database + " because existing type is not a database");

            array = element.getAsJsonArray();
        }

        boolean check = !array.isEmpty();
        Map<String, JsonObject> map = new HashMap<>();
        if (check) {
            for (JsonElement element : array) {
                if (!element.isJsonObject()) throw new UnsupportedOperationException("Cannot add object to non-object list!");

                JsonObject object = element.getAsJsonObject();
                if (!object.has("name")) throw new UnsupportedOperationException("Cannot add table to non-table list!");

                map.put(object.get("name").getAsString(), element.getAsJsonObject());
            }
        }

        List<JsonConnection> connections = new ArrayList<>();
        for (String name : names) {
            JsonObject child = new JsonObject();
            if (map.containsKey(name)) child = map.get(name);

            array.add(child);

            JsonConnection connection = new JsonConnection(file, this, name);
            connection.database = child;

            JsonObject typesObject = this.database.getAsJsonObject("types");
            typesObject.addProperty(name, "table");

            connections.add(connection);
        }
        this.database.add(database, array);

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
        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonObject()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");
        }

        if (value != null) {
            Gson gson = new GsonBuilder().create();
            JsonElement element = gson.toJsonTree(value);

            database.add(key, element);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.addProperty(key, "map");
        } else {
            database.remove(key);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
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
        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isString()) throw new UnsupportedOperationException("Cannot set string to non-string field!");
        }

        if (value != null) {
            database.addProperty(key, value);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.addProperty(key, "string");
        } else {
            database.remove(key);
            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
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
        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isNumber()) throw new UnsupportedOperationException("Cannot set number to non-number field!");
        }

        if (value != null) {
            database.addProperty(key, value);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.addProperty(key, "number");
        } else {
            database.remove(key);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
        }
    }

    /**
     * Set a value
     *
     * @param key the value key
     * @param value the value
     * @throws UnsupportedOperationException if the field is already occupied by another non-primitive object
     */
    public void set(final String key, final Boolean value) throws UnsupportedOperationException {
        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isBoolean()) throw new UnsupportedOperationException("Cannot set boolean to non-boolean field!");
        }

        if (value != null) {
            database.addProperty(key, value);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.addProperty(key, "boolean");
        } else {
            database.remove(key);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
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
        JsonArray array = new JsonArray();
        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonArray()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            array = element.getAsJsonArray();
        }

        boolean check = !array.isEmpty();
        if (check) {
            JsonElement firstElement = array.get(0);
            if (!firstElement.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot add primitive to non-primitive list!");

            JsonPrimitive primitive = firstElement.getAsJsonPrimitive();
            if (!primitive.isString()) throw new UnsupportedOperationException("Cannot add string to non-string list!");
        }

        if (value != null) {
            for (String s : value) array.add(s);
            database.add(key, array);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.addProperty(key, "stringList");
        } else {
            database.remove(key);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
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
        JsonArray array = new JsonArray();
        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonArray()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            array = element.getAsJsonArray();
        }

        boolean check = !array.isEmpty();
        if (check) {
            JsonElement firstElement = array.get(0);
            if (!firstElement.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot add primitive to non-primitive list!");

            JsonPrimitive primitive = firstElement.getAsJsonPrimitive();
            if (!primitive.isNumber()) throw new UnsupportedOperationException("Cannot add number to non-number list!");
        }

        if (value != null) {
            for (Number n : value) array.add(n);
            database.add(key, array);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.addProperty(key, "numberList");
        } else {
            database.remove(key);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
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
        JsonArray array = new JsonArray();
        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonArray()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            array = element.getAsJsonArray();
        }

        boolean check = !array.isEmpty();
        if (check) {
            JsonElement firstElement = array.get(0);
            if (!firstElement.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot add primitive to non-primitive list!");

            JsonPrimitive primitive = firstElement.getAsJsonPrimitive();
            if (!primitive.isBoolean()) throw new UnsupportedOperationException("Cannot add boolean to non-boolean list!");
        }

        if (value != null) {
            for (boolean b : value) array.add(b);
            database.add(key, array);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.addProperty(key, "booleanList");
        } else {
            database.remove(key);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
        }
    }

    /**
     * Get a string
     *
     * @param key the string key
     * @return the string
     */
    public String getString(final String key) {
        JsonPrimitive primitive = getPrimitive(key);
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
        JsonPrimitive primitive = getPrimitive(key);
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
        JsonPrimitive primitive = getPrimitive(key);
        if (primitive == null) return false;

        if (primitive.isBoolean()) return primitive.getAsBoolean();
        return false;
    }

    /**
     * Get a list of strings
     *
     * @param key the list key
     * @return the list
     */
    public List<String> getStringList(final String key) {
        if (!database.has(key)) return null;
        JsonElement element = database.get(key);

        if (!element.isJsonArray()) return null;
        JsonArray array = element.getAsJsonArray();

        List<String> strings = new ArrayList<>();
        for (JsonElement child : array) {
            if (!child.isJsonPrimitive()) continue;
            JsonPrimitive primitive = child.getAsJsonPrimitive();

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
        if (!database.has(key)) return null;
        JsonElement element = database.get(key);

        if (!element.isJsonArray()) return null;
        JsonArray array = element.getAsJsonArray();

        List<Number> numbers = new ArrayList<>();
        for (JsonElement child : array) {
            if (!child.isJsonPrimitive()) continue;
            JsonPrimitive primitive = child.getAsJsonPrimitive();

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
        if (!database.has(key)) return null;
        JsonElement element = database.get(key);

        if (!element.isJsonArray()) return null;
        JsonArray array = element.getAsJsonArray();

        List<Boolean> booleans = new ArrayList<>();
        for (JsonElement child : array) {
            if (!child.isJsonPrimitive()) continue;
            JsonPrimitive primitive = child.getAsJsonPrimitive();

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
        if (!database.has(key)) return null;
        JsonElement element = database.get(key);

        if (!element.isJsonArray()) return null;
        JsonArray array = element.getAsJsonArray();

        List<JsonConnection> tables = new ArrayList<>();
        for (JsonElement child : array) {
            if (!child.isJsonObject()) continue;
            JsonObject object = child.getAsJsonObject();

            if (!object.has("name")) continue;
            String tableName = object.get("name").getAsString();

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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.has(key)) {
            return typesObject.get(key).getAsString();
        }

        return "null";
    }

    /**
     * Get all the database keys
     *
     * @return the database keys
     */
    public Set<String> getKeys() {
        JsonObject typesObject = database.getAsJsonObject("types");
        return typesObject.keySet();
    }

    /**
     * Get the table name
     *
     * @return the table name
     */
    public String getTable() {
        return table;
    }

    /**
     * Get the parent connection
     *
     * @return the parent connection
     */
    public JsonConnection getParent() {
        return parent;
    }

    /**
     * Save all changes into the local database
     */
    public boolean save() {
        if (parent == null) {
            GsonBuilder builder = new GsonBuilder();
            if (pretty) builder.setPrettyPrinting();
            Gson gson = builder.create();
            String raw = gson.toJson(database);

            return PathUtilities.write(file, raw);
        }

        return parent.save();
    }

    private JsonPrimitive getPrimitive(final String key) {
        if (!database.has(key)) return null;
        JsonElement element = database.get(key);

        if (!element.isJsonPrimitive()) return null;
        return element.getAsJsonPrimitive();
    }
}
