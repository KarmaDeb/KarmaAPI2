package es.karmadev.api.database.model.json;

import com.google.gson.*;
import es.karmadev.api.database.DatabaseConnection;
import es.karmadev.api.database.result.QueryResult;
import es.karmadev.api.database.result.SimpleQueryResult;
import es.karmadev.api.file.util.PathUtilities;
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
        JsonObject tmpObject = new JsonObject();
        if (raw.startsWith("{") && raw.endsWith("}")) {
            Gson gson = new GsonBuilder().create();
            tmpObject = gson.fromJson(raw, JsonObject.class);
        }

        database = tmpObject;
        if (!database.has("types") || !database.get("types").isJsonObject()) {
            JsonObject typesObject = new JsonObject();
            typesObject.addProperty("schemed", false);
            database.add("types", typesObject);
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
        if (query.equalsIgnoreCase("set pretty printing on;")) {
            setPrettySave(true);
            return SimpleQueryResult.builder().database(PathUtilities.getName(file)).table(table).build();
        }
        if (query.equalsIgnoreCase("set pretty printing off;")) {
            setPrettySave(true);
            return SimpleQueryResult.builder().database(PathUtilities.getName(file)).table(table).build();
        }
        if (query.equalsIgnoreCase("toggle pretty printing;")) {
            setPrettySave(!pretty);
            return SimpleQueryResult.builder().database(PathUtilities.getName(file)).table(table).build();
        }

        if (query.equalsIgnoreCase("set query auto save on;")) {
            autoSave = true;
            return SimpleQueryResult.builder().database(PathUtilities.getName(file)).table(table).build();
        }
        if (query.equalsIgnoreCase("set query auto save off;")) {
            autoSave = false;
            return SimpleQueryResult.builder().database(PathUtilities.getName(file)).table(table).build();
        }
        if (query.equalsIgnoreCase("toggle query auto save;")) {
            autoSave = !autoSave;
            return SimpleQueryResult.builder().database(PathUtilities.getName(file)).table(table).build();
        }

        if (query.equalsIgnoreCase("save database;")) {
            save();
            return SimpleQueryResult.builder().database(PathUtilities.getName(file)).table(table).build();
        }

        JsonConnectionExecutor executor = new JsonConnectionExecutor(this);
        return executor.execute(query);
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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot create table " + name + " on " + table + " because it is schemed");
        }

        JsonObject child = new JsonObject();
        if (database.has(name)) {
            JsonElement element = database.get(name);
            if (!element.isJsonObject()) throw new UnsupportedOperationException("Cannot create a table " + name + " because another field with that name already exists");

            if (typesObject.has(name)) {
                String type = typesObject.get(name).getAsString();
                if (!type.equals("table")) throw new UnsupportedOperationException("Cannot create a table " + name + " because another field with that name already exists");
            }

            child = element.getAsJsonObject();
        }

        if (!child.has("types")) {
            JsonObject types = new JsonObject();
            types.addProperty("schemed", false);
            child.add("types", types);
        }
        database.add(name, child);
        JsonConnection connection = new JsonConnection(file, this, name);
        connection.database = child;

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
        JsonObject typesObject = this.database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot create tables for " + database + " on " + table + " because it is schemed");
        }

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

            if (!child.has("types")) {
                JsonObject types = new JsonObject();
                types.addProperty("schemed", false);
                child.add("types", types);
            }
            array.add(child);

            JsonConnection connection = new JsonConnection(file, this, name);
            connection.database = child;

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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot set map " + key + " on " + table + " because it is schemed");
        }

        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonObject()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");
        }

        if (value != null) {
            Gson gson = new GsonBuilder().create();
            JsonElement element = gson.toJsonTree(value);

            database.add(key, element);

            typesObject.addProperty(key, "map");
        } else {
            database.remove(key);

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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            String currentType = getType(key);
            if (!currentType.equalsIgnoreCase("null") && !currentType.equalsIgnoreCase("string")) {
                throw new UnsupportedOperationException("Cannot assign string value to non string schemed key " + key);
            }

            throw new UnsupportedOperationException("Cannot create table " + key + " on " + table + " because it is schemed");
        }

        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isString()) throw new UnsupportedOperationException("Cannot set string to non-string field!");
        }

        if (value != null) {
            database.addProperty(key, value);

            typesObject.addProperty(key, "string");
        } else {
            database.remove(key);
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

            if (value != null) {
                String currentType = getType(key);
                String typeName = getTypeName(value);

                JsonObject typesObject = database.getAsJsonObject("types");
                if (typesObject.get("schemed").getAsBoolean()) {
                    if (!currentType.equalsIgnoreCase("null") && !currentType.equalsIgnoreCase(typeName)) {
                        throw new UnsupportedOperationException("Cannot assign " + typeName + " value to non " + typeName + " schemed key " + key);
                    }

                    throw new UnsupportedOperationException("Cannot create table " + key + " on " + table + " because it is schemed");
                }

                if (!typeName.equalsIgnoreCase(currentType)) {
                    throw new UnsupportedOperationException("Cannot set number of " + key + " to non-" + typeName + " value!");
                }
            }
        }

        if (value != null) {
            database.addProperty(key, value);

            JsonObject typesObject = database.getAsJsonObject("types");
            String typeName = getTypeName(value);
            typesObject.addProperty(key, typeName);
        } else {
            database.remove(key);

            JsonObject typesObject = database.getAsJsonObject("types");
            typesObject.remove(key);
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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            String currentType = getType(key);
            if (!currentType.equalsIgnoreCase("null") && !currentType.equalsIgnoreCase("boolean")) {
                throw new UnsupportedOperationException("Cannot assign boolean value to non boolean schemed key " + key);
            }

            throw new UnsupportedOperationException("Cannot create table " + key + " on " + table + " because it is schemed");
        }

        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (!element.isJsonPrimitive()) throw new UnsupportedOperationException("Cannot redefine field " + key + " because existing type doesn't match new type");

            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isBoolean()) throw new UnsupportedOperationException("Cannot set boolean to non-boolean field!");
        }

        if (value != null) {
            database.addProperty(key, value);

            typesObject.addProperty(key, "boolean");
        } else {
            database.remove(key);
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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot set list " + key + " on " + table + " because it is schemed");
        }

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

            typesObject.addProperty(key, "stringList");
        } else {
            database.remove(key);
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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot set list " + key + " on " + table + " because it is schemed");
        }

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

            typesObject.addProperty(key, "numberList");
        } else {
            database.remove(key);
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
        JsonObject typesObject = database.getAsJsonObject("types");
        if (typesObject.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot set list " + key + " on " + table + " because it is schemed");
        }

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

            typesObject.addProperty(key, "booleanList");
        } else {
            database.remove(key);
            typesObject.remove(key);
        }
    }

    /**
     * Get a map
     *
     * @param key the map key
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(final String key) {
        if (!database.has(key)) return null;
        JsonElement element = database.get(key);
        if (element == null) return null;

        if (getType(key).equals("map")) {
            Gson gson = new GsonBuilder().create();
            return (Map<String, Object>) gson.fromJson(element, Map.class);
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
        if (typesObject.has(key) && typesObject.get(key).isJsonPrimitive()) {
            return typesObject.get(key).getAsString();
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
        return database.has(key);
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
     * Save all changes into the local database
     */
    public boolean save() {
        if (parent == null) {
            GsonBuilder builder = new GsonBuilder().serializeNulls();

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
