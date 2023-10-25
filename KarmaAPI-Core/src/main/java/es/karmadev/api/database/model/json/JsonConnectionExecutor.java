package es.karmadev.api.database.model.json;

import com.google.gson.*;
import es.karmadev.api.database.model.json.query.GlobalTypeValue;
import es.karmadev.api.database.model.json.query.QueryPart;
import es.karmadev.api.database.model.json.query.RawQuery;
import es.karmadev.api.database.model.json.query.SimpleQuery;
import es.karmadev.api.database.result.QueryResult;
import es.karmadev.api.database.result.SimpleQueryResult;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.strings.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Json connection query executor
 */
class JsonConnectionExecutor {


    private final static Pattern createQueryPattern = Pattern.compile("^SETUP TABLE '(?<table>[\\w\\s]+)';$", Pattern.CASE_INSENSITIVE);
    private final static Pattern defineSchemaPattern = Pattern.compile("^IN TABLE '(?<table>[\\w\\s]+)' SCHEMA IS (?<schema>\\(.*\\));$", Pattern.CASE_INSENSITIVE);
    private final static Pattern singleDefinePattern = Pattern.compile("^IN TABLE '(?<table>[\\w\\s]+)' SET KEY '(?<key>\\w*)' VALUE TO (?<value>'[^']*'|[+-]?\\d*[,.]?e?\\d*|false|true|NULL);$", Pattern.CASE_INSENSITIVE);
    private final static Pattern multiDefinePattern = Pattern.compile("^IN TABLE '(?<table>[\\w\\s]+)' SET KEYS \\('(?<keys>.*)'\\) VALUE TO \\((?<values>.*)\\);$", Pattern.CASE_INSENSITIVE);
    private final static Pattern singleDefineWherePattern = Pattern.compile("^IN TABLE '(?<table>[\\w\\s]+)' SET KEY '(?<key>\\w*)' VALUE TO (?<value>'[^']*'|[+-]?\\d*[,.]?e?\\d*|false|true|NULL) WHEREVER '(?<whereKey>\\w*)' (?<operator>=|~=|!>|~>|~<|>=|<=|>~|<~|<>|>|<) (?<whereValue>'[^']*'|[+-]?\\d*[,.]?e?\\d*|false|true|NULL);$", Pattern.CASE_INSENSITIVE);
    private final static Pattern singleGetPattern = Pattern.compile("^FROM TABLE '(?<table>[^']*)' SELECT (?<key>\\(.*\\)|\\*);$", Pattern.CASE_INSENSITIVE);
    private final static Pattern whereGetPattern = Pattern.compile("^FROM TABLE '(?<table>[^']*)' SELECT (?<keys>\\(.*\\)|\\*) WHERE (?<where>.+);$", Pattern.CASE_INSENSITIVE);
    private final static Pattern keyPattern = Pattern.compile("\\s*'(?<key>\\w+)'\\s*", Pattern.CASE_INSENSITIVE);
    private final static Pattern keyTypePattern = Pattern.compile("\\s*'(?<key>\\w+)'\\s*(?<type>string|boolean|byte|short|integer|int|long|float|double)\\s*(?<modifier1>NN|AI|UK)?\\s*(?<modifier2>NN|AI|UK)?\\s*(?<modifier3>NN|AI|UK)?", Pattern.CASE_INSENSITIVE);
    private final static Pattern whereAndOrPattern = Pattern.compile("(?<optionalANDiOR>AND|OR)?\\s*'(?<key>[^']*)'\\s*(?<operator>=|~=|!>|~>|~<|>=|<=|>~|<~|<>|>|<)\\s*(?<value>'[^']*'|null|true|false|[+-]?\\d*[,.]?e?\\d*)", Pattern.CASE_INSENSITIVE);

    private final static String DEFAULT_NULL_VALUE = StringUtils.generateString();

    private final JsonConnection connection;

    public JsonConnectionExecutor(final JsonConnection connection) {
        this.connection = connection;
    }

    public QueryResult execute(final String query) {
        Matcher createMatcher = createQueryPattern.matcher(query);
        if (createMatcher.matches()) {
            return executeCreate(createMatcher.group("table"));
        }

        Matcher defineSchemaMatcher = defineSchemaPattern.matcher(query);
        if (defineSchemaMatcher.matches()) {
            return executeScheme(defineSchemaMatcher, query);
        }

        Matcher singleDefineMatcher = singleDefinePattern.matcher(query);
        if (singleDefineMatcher.matches()) {
            String table = singleDefineMatcher.group("table");
            String key = singleDefineMatcher.group("key");
            String value = singleDefineMatcher.group("value");
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            } else {
                if (value.equals("NULL")) {
                    value = DEFAULT_NULL_VALUE;
                }
            }

            Map<String, String> singleMap = new ConcurrentHashMap<>();
            singleMap.put(key, value);
            return executeDefine(table, singleMap);
        }

        Matcher multiDefinematcher = multiDefinePattern.matcher(query);
        if (multiDefinematcher.matches()) {
            String table = multiDefinematcher.group("table");
            String rawKeys = multiDefinematcher.group("keys");
            String rawValues = multiDefinematcher.group("values");

            Map<String, String> multiMap = new ConcurrentHashMap<>();
            if (rawKeys.contains(",")) {
                if (!rawValues.contains(",")) {
                    throw new IllegalStateException("Unknown or invalid json database syntax: " + query);
                }

                String[] keyData = rawKeys.split(",");
                String[] valueData = rawValues.split(",");
                if (keyData.length == 0 || valueData.length == 0) {
                    throw new IllegalStateException("Unknown or invalid json database syntax: " + query);
                }
                if (keyData.length != valueData.length) {
                    throw new IllegalStateException("Unknown or invalid json database syntax: " + query);
                }

                for (int i = 0; i < keyData.length; i++) {
                    String key = keyData[i];
                    String value = valueData[i];

                    if (key.startsWith("'")) {
                        key = key.substring(1);
                    }
                    if (key.endsWith("'")) {
                        key = key.substring(0, key.length() - 1);
                    }

                    while (key.startsWith(" ")) {
                        key = key.substring(1);
                    }
                    while (key.endsWith(" ")) {
                        key = key.substring(0, key.length() - 1);
                    }
                    while (value.startsWith(" ")) {
                        value = value.substring(1);
                    }
                    while (value.endsWith(" ")) {
                        value = value.substring(0, value.length() - 1);
                    }

                    if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    } else {
                        if (value.equals("NULL")) {
                            value = DEFAULT_NULL_VALUE;
                        }
                    }

                    multiMap.put(key, value);
                }
            } else {
                multiMap.put(rawKeys, rawValues);
            }

            return executeDefine(table, multiMap);
        }

        Matcher singleDefineWhereMatcher = singleDefineWherePattern.matcher(query);
        if (singleDefineWhereMatcher.matches()) {
            String table = singleDefineWhereMatcher.group("table");
            String key = singleDefineWhereMatcher.group("key");
            String value = singleDefineWhereMatcher.group("value");
            String field = singleDefineWhereMatcher.group("whereKey");
            String comparator = singleDefineWhereMatcher.group("operator");
            String requiredValue = singleDefineWhereMatcher.group("whereValue");

            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            } else {
                if (value.equals("NULL")) {
                    value = DEFAULT_NULL_VALUE;
                }
            }

            if (requiredValue.startsWith("'") && requiredValue.endsWith("'")) {
                requiredValue = requiredValue.substring(1, requiredValue.length() - 1);
            } else {
                if (requiredValue.equals("NULL")) {
                    requiredValue = DEFAULT_NULL_VALUE;
                }
            }

            return executeSingleDefineWhere(table, key, value, field, comparator, requiredValue);
        }

        Matcher singleGetMatcher = singleGetPattern.matcher(query);
        if (singleGetMatcher.matches()) {
            String table = singleGetMatcher.group("table");
            String select = singleGetMatcher.group("key");
            if (!select.equalsIgnoreCase("*")) {
                select = select.substring(1, select.length() - 1);
                if (ObjectUtils.isNullOrEmpty(select)) {
                    throw new UnsupportedOperationException("Unknown or invalid json database syntax: " + query);
                }
            }

            return executeSingleGet(table, select);
        }

        Matcher getWhereMatcher = whereGetPattern.matcher(query);
        if (getWhereMatcher.matches()) {
            String table = getWhereMatcher.group("table");
            String keys = getWhereMatcher.group("keys");
            String where = getWhereMatcher.group("where");

            List<String> keyArray = new ArrayList<>();
            if (!keys.equalsIgnoreCase("*")) {
                keys = keys.substring(1, keys.length() - 1);
                if (ObjectUtils.isNullOrEmpty(keys)) {
                    throw new IllegalStateException("Unknown or invalid json database syntax: " + query);
                }


                if (keys.contains(",")) {
                    String[] data = keys.split(",");
                    for (String key : data) {
                        Matcher matcher = keyPattern.matcher(key);
                        if (matcher.matches()) {
                            keyArray.add(matcher.group(1));
                        } else {
                            throw new IllegalStateException("Unknown or invalid json database syntax at: " + query);
                        }
                    }
                } else {
                    Matcher matcher = keyPattern.matcher(keys);
                    if (matcher.matches()) {
                        keyArray.add(matcher.group(1));
                    } else {
                        throw new IllegalStateException("Unknown or invalid json database syntax at: " + query);
                    }
                }
            }

            return executeGetWhere(table, keyArray, where, query);
        }

        throw new UnsupportedOperationException("Unknown or invalid json database syntax: " + query);
    }

    /**
     * Execute a create operation on the json database
     *
     * @param table the table to create
     * @return the creation result
     */
    private QueryResult executeCreate(final String table) {
        boolean mustSave = !connection.hasTable(table);
        JsonConnection existingTable = connection.createTable(table);
        if (connection.autoSave && mustSave) {
            connection.save();
        }

        return createAllResult(connection.file, table, existingTable);
    }

    private QueryResult executeScheme(final Matcher defineSchemaMatcher, final String query) {
        String table = defineSchemaMatcher.group(1);
        String schema = defineSchemaMatcher.group(2);
        if (schema.length() == 2) {
            try {
                throw new UnsupportedOperationException("Invalid json database syntax, empty schema");
            } finally {
                int index = defineSchemaMatcher.toMatchResult().start(2);
                StringBuilder builder = new StringBuilder("Invalid json database syntax at: " + query);
                builder.append("\n");
                for (int i = 0; i < 33 + index; i++) builder.append(" ");
                builder.append("^");

                System.out.println(builder);
            }
        }

        schema = schema.substring(1, schema.length() - 1);
        Map<String, BiValue<String[]>> types = getTypesFromString(schema);

        if (connection.hasTable(table)) {
            JsonConnection jsonTable = connection.createTable(table);
            JsonObject tableTypes = jsonTable.database.get("types").getAsJsonObject();

            int checked = 0;
            for (String key : tableTypes.keySet()) {
                if (key.equalsIgnoreCase("schemed") || key.equalsIgnoreCase("modifiers") || key.equalsIgnoreCase("attributes")) continue;

                if (!types.containsKey(key)) {
                    throw new IllegalStateException("Cannot redefine table " + table + " schema because it has values. Either remove the table or scheme a new table");
                }

                String storedType = tableTypes.get(key).getAsString();
                if (!storedType.equalsIgnoreCase(types.get(key).getValue()[0])) {
                    throw new IllegalStateException("Cannot redefine table " + table + " schema because it has values. Either remove the table or scheme a new table");
                }

                checked++;
            }

            if (checked > 0 && checked != types.size()) {
                throw new IllegalStateException("Cannot redefine table " + table + " schema because it has values. Either remove the table or scheme a new table");
            }

            if (checked > 0) return createAllResult(connection.file, table, jsonTable);
        }

        JsonConnection jsonTable = connection.createTable(table);
        if (jsonTable.database.get("types").getAsJsonObject().size() - 1 > 0) {
            throw new IllegalStateException("Cannot redefine table " + table + " schema because it has values. Either remove the table or scheme a new table");
        }

        JsonObject typesJson = jsonTable.database.get("types").getAsJsonObject();
        typesJson.addProperty("schemed", true);

        JsonObject modifiers = new JsonObject();
        for (String key : types.keySet()) {
            if (key.equalsIgnoreCase("modifiers")) {
                throw new IllegalStateException("Cannot scheme for reserved key: modifiers");
            }
            if (key.equalsIgnoreCase("attributes")) {
                throw new IllegalStateException("Cannot scheme for reserved key: attributes");
            }

            BiValue<String[]> value = types.get(key);
            typesJson.addProperty(key, value.getValue()[0]);
            String[] mods = value.getSubValue();
            JsonArray array = new JsonArray();
            for (String mod : mods) array.add(mod);

            modifiers.add(key, array);
        }

        typesJson.add("modifiers", modifiers);
        typesJson.add("attributes", new JsonObject());
        jsonTable.database.add("types", typesJson);
        if (connection.autoSave) {
            connection.save();
        }

        return createAllResult(connection.file, table, jsonTable);
    }

    private QueryResult executeDefine(final String table, final Map<String, String> keyValues) {
        if (!connection.hasTable(table)) {
            throw new UnsupportedOperationException("Unknown table: " + table);
        }

        JsonConnection jsonTable = connection.createTable(table);
        JsonObject typesJson = jsonTable.database.get("types").getAsJsonObject();

        if (!typesJson.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot execute json query on non-schemed table");
        }

        JsonArray records = new JsonArray();
        if (jsonTable.database.has("records")) {
            records = jsonTable.database.getAsJsonArray("records");
        }

        SimpleQueryResult.QueryResultBuilder builder = SimpleQueryResult.builder().database(PathUtilities.getName(connection.file))
                .table(table);

        JsonObject recordElement = new JsonObject();
        for (String key : keyValues.keySet()) {
            String rawValue = keyValues.get(key);

            if (!typesJson.has(key)) {
                throw new IllegalStateException("Unknown field " + key + " in table " + table);
            }

            if (!rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                String type = typesJson.get(key).getAsString();
                if (type.equalsIgnoreCase("boolean")) {
                    if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false") &&
                            !rawValue.equalsIgnoreCase("1") && !rawValue.equalsIgnoreCase("0")) {
                        throw new IllegalStateException("Cannot set non boolean value " + rawValue + " for boolean field " + key + " in table " + table);
                    }
                } else if (!type.equalsIgnoreCase("string") && !rawValue.matches("^[+-]?(\\d*[,.]\\d+)|(\\d*)$")) {
                    throw new IllegalStateException("Cannot set non numeric value " + rawValue + " for numeric field " + key + " in table " + table);
                }
            }

            String type = jsonTable.getType(key);
            if (hasModifier(jsonTable.database, key, "uk")) {
                for (JsonElement record : records) {
                    if (!record.isJsonObject()) continue;
                    JsonObject object = record.getAsJsonObject();
                    if (object.has(key)) {
                        JsonElement primitive = object.get(key);
                        if (primitive == null) primitive = JsonNull.INSTANCE;

                        switch (type) {
                            case "string":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (primitive.getAsString().equals(rawValue)) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                            case "boolean":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (String.valueOf(primitive.getAsBoolean()).equals(rawValue.replace("1", "true").replace("0", "false"))) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                            case "byte":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (String.valueOf(primitive.getAsByte()).equals(rawValue)) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                            case "short":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (String.valueOf(primitive.getAsShort()).equals(rawValue)) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                            case "integer":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (String.valueOf(primitive.getAsInt()).equals(rawValue)) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                            case "long":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (String.valueOf(primitive.getAsLong()).equals(rawValue)) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                            case "float":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (String.valueOf(primitive.getAsFloat()).equals(rawValue.replace(",", "."))) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                            case "double":
                                if (primitive.isJsonNull()) {
                                    if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                        throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                    }
                                }

                                if (String.valueOf(primitive.getAsByte()).equals(rawValue.replace(",", "."))) {
                                    throw new IllegalStateException("Cannot set value for " + key + " on table " + table + " because it's marked as unique key but the table already has a record with that value");
                                }
                                break;
                        }
                    }
                }
            }

            if (!rawValue.equals(DEFAULT_NULL_VALUE)) {
                switch (type) {
                    case "string":
                        recordElement.addProperty(key, rawValue);
                        builder.appendResult(key, rawValue);

                        break;
                    case "boolean":
                        if (rawValue.equalsIgnoreCase("1") || rawValue.equalsIgnoreCase("true")) {
                            recordElement.addProperty(key, true);
                            builder.appendResult(key, true);

                            break;
                        }

                        recordElement.addProperty(key, false);
                        builder.appendResult(key, false);

                        break;
                    case "byte":
                        byte b = Byte.parseByte(rawValue);
                        recordElement.addProperty(key, b);
                        builder.appendResult(key, b);

                        break;
                    case "short":
                        short s = Short.parseShort(rawValue);
                        recordElement.addProperty(key, s);
                        builder.appendResult(key, s);

                        break;
                    case "integer":
                        int i = Integer.parseInt(rawValue);
                        recordElement.addProperty(key, i);
                        builder.appendResult(key, i);

                        break;
                    case "long":
                        long l = Long.parseLong(rawValue);
                        recordElement.addProperty(key, l);
                        builder.appendResult(key, l);

                        break;
                    case "float":
                        float fl = Float.parseFloat(rawValue.replace(",", "."));
                        recordElement.addProperty(key, fl);
                        builder.appendResult(key, fl);

                        break;
                    case "double":
                        double db = Double.parseDouble(rawValue);
                        recordElement.addProperty(key, db);
                        builder.appendResult(key, db);

                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported field " + key + " type: " + type);
                }
            } else {
                if (hasModifier(jsonTable.database, key, "nn")) {
                    throw new UnsupportedOperationException("Cannot set null value for non null key " + key + " on table " + table);
                }

                if (type.equalsIgnoreCase("boolean")) {
                    recordElement.addProperty(key, false);
                } else {
                    recordElement.addProperty(key, (String) null);
                }
            }
        }

        for (String field : getFieldsWithModifiers(jsonTable.database, "ai")) {
            if (keyValues.containsKey(field)) continue;

            String fieldType = jsonTable.getType(field);
            if (fieldType.equalsIgnoreCase("integer")) {
                int val = getOrCreateIntAttribute(jsonTable.database, field, (value, attributes) -> {
                    attributes.addProperty(field, value + 1);
                });
                recordElement.addProperty(field, val);
                builder.appendResult(field, val);
            }
        }

        records.add(recordElement);

        jsonTable.database.add("records", records);
        if (connection.autoSave) {
            connection.save();
        }

        return builder.build();
    }

    private QueryResult executeSingleDefineWhere(final String table, final String key,
                                                 final String rawValue, final String field,
                                                 final String comparator, final String existingValue) {
        if (!connection.hasTable(table)) {
            throw new UnsupportedOperationException("Unknown table: " + table);
        }

        JsonConnection jsonTable = connection.createTable(table);
        JsonObject typesJson = jsonTable.database.get("types").getAsJsonObject();

        if (!typesJson.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot execute json query on non-schemed table");
        }

        if (!typesJson.has(key)) {
            throw new IllegalStateException("Unknown field " + key + " in table " + table);
        }
        if (!typesJson.has(field)) {
            throw new IllegalStateException("Unknown field " + field + " in table " + table);
        }

        if (!rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
            String type = typesJson.get(key).getAsString();
            if (type.equalsIgnoreCase("boolean")) {
                if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false") &&
                        !rawValue.equalsIgnoreCase("1") && !rawValue.equalsIgnoreCase("0")) {
                    throw new IllegalStateException("Cannot set non boolean value " + rawValue + " for boolean field " + key + " in table " + table);
                }
            } else {
                if (!type.equalsIgnoreCase("string") && !rawValue.matches("^[+-]?(\\d*[,.]\\d+)|(\\d*)$")) {
                    throw new IllegalStateException("Cannot set non numeric value " + rawValue + " for numeric field " + key + " in table " + table);
                }
            }
        }

        SimpleQueryResult.QueryResultBuilder builder = SimpleQueryResult.builder().database(PathUtilities.getName(connection.file))
                .table(table);

        JsonArray records = new JsonArray();
        if (jsonTable.database.has("records")) {
            records = jsonTable.database.getAsJsonArray("records");
        }

        JsonArray modifiedRecords = new JsonArray();
        for (JsonElement recordElement : records) {
            JsonObject record = recordElement.getAsJsonObject();
            String fieldType = jsonTable.getType(field);

            if (fieldType.equalsIgnoreCase("null")) {
                throw new IllegalStateException("Unknown field " + field + " in table " + table);
            }

            JsonElement elementValue = record.get(field);
            if (elementValue.isJsonNull() && existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (String) null);
                    builder.nextQuery().appendResult(key, (String) null);
                    modifiedRecords.add(record);
                    continue;
                }

                record.addProperty(key, rawValue);
                builder.nextQuery().appendResult(key, rawValue);
                modifiedRecords.add(record);
                continue;
            }

            boolean updateRecord = false;
            switch (fieldType) {
                case "string":
                    String stringValue = elementValue.getAsString();

                    switch (comparator) {
                        case ">=":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.startsWith(stringValue);
                            break;
                        case ">~":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.toLowerCase().startsWith(stringValue.toLowerCase());
                            break;
                        case ">":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.contains(stringValue);
                            break;
                        case "~>":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.toLowerCase().contains(stringValue.toLowerCase());
                            break;
                        case "<=":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.endsWith(stringValue);
                            break;
                        case "<~":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.toLowerCase().endsWith(stringValue.toLowerCase());
                            break;
                        case "<":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || !existingValue.contains(stringValue);
                            break;
                        case "~<":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || !existingValue.toLowerCase().contains(stringValue.toLowerCase());
                            break;
                        case "<>":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || !existingValue.equals(stringValue);
                            break;
                        case "!>":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || !existingValue.equalsIgnoreCase(stringValue);
                            break;
                        case "~=":
                            updateRecord = existingValue.equalsIgnoreCase(stringValue);
                            break;
                        case "=":
                        default:
                            updateRecord = existingValue.equals(stringValue);
                            break;
                    }
                    break;
                case "boolean":
                    boolean boolValue = elementValue.getAsBoolean();
                    boolean boolMatch = existingValue.replace("0", "false").replace("1", "true").equalsIgnoreCase(String.valueOf(boolValue));

                    switch (comparator) {
                        case ">=":
                        case ">":
                        case "<=":
                        case "<":
                            throw new UnsupportedOperationException("Unsupported boolean operator: " + comparator);
                        case "<>":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || !boolMatch;
                            break;
                        case "=":
                        default:
                            updateRecord = boolMatch;
                            break;
                    }
                    break;
                case "byte":
                    byte byteValue = elementValue.getAsByte();

                    switch (comparator) {
                        case ">=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                byte bVal = (byte) Math.max(Byte.MIN_VALUE, Math.min(lVal, Byte.MAX_VALUE));
                                updateRecord = bVal >= byteValue;
                            }
                            break;
                        case "<=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                byte bVal = (byte) Math.max(Byte.MIN_VALUE, Math.min(lVal, Byte.MAX_VALUE));
                                updateRecord = bVal <= byteValue;
                            }
                            break;
                        case ">":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                byte bVal = (byte) Math.max(Byte.MIN_VALUE, Math.min(lVal, Byte.MAX_VALUE));
                                updateRecord = bVal > byteValue;
                            }
                            break;
                        case "<":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                byte bVal = (byte) Math.max(Byte.MIN_VALUE, Math.min(lVal, Byte.MAX_VALUE));
                                updateRecord = bVal < byteValue;
                            }
                            break;
                        case "<>":
                            if (existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.matches("^\\d*$")) {
                                long lVal = byteValue - 1;
                                if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                    lVal = Long.parseLong(existingValue);
                                }

                                byte bVal = (byte) Math.max(Byte.MIN_VALUE, Math.min(lVal, Byte.MAX_VALUE));
                                updateRecord = bVal != byteValue;
                            }
                            break;
                        case "=":
                        default:
                            if (existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                byte bVal = (byte) Math.max(Byte.MIN_VALUE, Math.min(lVal, Byte.MAX_VALUE));
                                updateRecord = bVal == byteValue;
                            }
                            break;
                    }
                    break;
                case "short":
                    short shortValue = elementValue.getAsShort();

                    switch (comparator) {
                        case ">=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                short bVal = (short) Math.max(Short.MIN_VALUE, Math.min(lVal, Short.MAX_VALUE));
                                updateRecord = bVal >= shortValue;
                            }
                            break;
                        case "<=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                short bVal = (short) Math.max(Short.MIN_VALUE, Math.min(lVal, Short.MAX_VALUE));
                                updateRecord = bVal <= shortValue;
                            }
                            break;
                        case ">":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                short bVal = (short) Math.max(Short.MIN_VALUE, Math.min(lVal, Short.MAX_VALUE));
                                updateRecord = bVal > shortValue;
                            }
                            break;
                        case "<":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                short bVal = (short) Math.max(Short.MIN_VALUE, Math.min(lVal, Short.MAX_VALUE));
                                updateRecord = bVal < shortValue;
                            }
                            break;
                        case "<>":
                            if (existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.matches("^\\d*$")) {
                                long lVal = Math.max(Short.MIN_VALUE + 1, Math.min(Short.MAX_VALUE - 1, shortValue - 1));
                                if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                    lVal = Long.parseLong(existingValue);
                                }

                                short bVal = (short) Math.max(Short.MIN_VALUE, Math.min(lVal, Short.MAX_VALUE));
                                updateRecord = bVal != shortValue;
                            }
                            break;
                        case "=":
                        default:
                            if (existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                short bVal = (short) Math.max(Short.MIN_VALUE, Math.min(lVal, Short.MAX_VALUE));
                                updateRecord = bVal == shortValue;
                            }
                            break;
                    }
                    break;
                case "integer":
                    int intValue = elementValue.getAsInt();

                    switch (comparator) {
                        case ">=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                int bVal = (int) Math.max(Integer.MIN_VALUE, Math.min(lVal, Integer.MAX_VALUE));
                                updateRecord = bVal >= intValue;
                            }
                            break;
                        case "<=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                int bVal = (int) Math.max(Integer.MIN_VALUE, Math.min(lVal, Integer.MAX_VALUE));
                                updateRecord = bVal <= intValue;
                            }
                            break;
                        case ">":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                int bVal = (int) Math.max(Integer.MIN_VALUE, Math.min(lVal, Integer.MAX_VALUE));
                                updateRecord = bVal > intValue;
                            }
                            break;
                        case "<":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                int bVal = (int) Math.max(Integer.MIN_VALUE, Math.min(lVal, Integer.MAX_VALUE));
                                updateRecord = bVal < intValue;
                            }
                            break;
                        case "<>":
                            if (existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.matches("^\\d*$")) {
                                long lVal = Math.max(Integer.MIN_VALUE + 1, Math.min(Integer.MAX_VALUE - 1, intValue - 1));
                                if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                    lVal = Long.parseLong(existingValue);
                                }

                                int bVal = (int) Math.max(Integer.MIN_VALUE, Math.min(lVal, Integer.MAX_VALUE));
                                updateRecord = bVal != intValue;
                            }
                            break;
                        case "=":
                        default:
                            if (existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                int bVal = (int) Math.max(Integer.MIN_VALUE, Math.min(lVal, Integer.MAX_VALUE));
                                updateRecord = bVal == intValue;
                            }
                            break;
                    }
                    break;
                case "long":
                    long longValue = elementValue.getAsLong();

                    switch (comparator) {
                        case ">=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                updateRecord = lVal >= longValue;
                            }
                            break;
                        case "<=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                updateRecord = lVal <= longValue;
                            }
                            break;
                        case ">":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                updateRecord = lVal > longValue;
                            }
                            break;
                        case "<":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                updateRecord = lVal < longValue;
                            }
                            break;
                        case "<>":
                            if (existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.matches("^\\d*$")) {
                                long lVal = Math.max(Long.MIN_VALUE + 1, Math.min(Long.MAX_VALUE - 1, longValue - 1));
                                if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                    lVal = Long.parseLong(existingValue);
                                }

                                updateRecord = lVal != longValue;
                            }
                            break;
                        case "=":
                        default:
                            if (existingValue.matches("^\\d*$")) {
                                long lVal = Long.parseLong(existingValue);
                                updateRecord = lVal == longValue;
                            }
                            break;
                    }
                    break;
                case "float":
                    float floatValue = elementValue.getAsFloat();

                    switch (comparator) {
                        case ">=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                float fVal = (float) Math.max(Float.MIN_VALUE, Math.min(Float.MIN_VALUE, lVal));
                                updateRecord = fVal >= floatValue;
                            }
                            break;
                        case "<=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                float fVal = (float) Math.max(Float.MIN_VALUE, Math.min(Float.MIN_VALUE, lVal));
                                updateRecord = fVal <= floatValue;
                            }
                            break;
                        case ">":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                float fVal = (float) Math.max(Float.MIN_VALUE, Math.min(Float.MIN_VALUE, lVal));
                                updateRecord = fVal > floatValue;
                            }
                            break;
                        case "<":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                float fVal = (float) Math.max(Float.MIN_VALUE, Math.min(Float.MIN_VALUE, lVal));
                                updateRecord = fVal < floatValue;
                            }
                            break;
                        case "<>":
                            if (existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Math.max(Double.MIN_VALUE + 1, Math.min(Double.MAX_VALUE - 1, floatValue - 1));
                                if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                    lVal = Double.parseDouble(existingValue.replace(",", "."));
                                }

                                float fVal = (float) Math.max(Float.MIN_VALUE, Math.min(Float.MIN_VALUE, lVal));
                                updateRecord = fVal != floatValue;
                            }
                            break;
                        case "=":
                        default:
                            if (existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                float fVal = (float) Math.max(Float.MIN_VALUE, Math.min(Float.MIN_VALUE, lVal));
                                updateRecord = fVal == floatValue;
                            }
                            break;
                    }
                    break;
                case "double":
                    double doubleValue = elementValue.getAsDouble();

                    switch (comparator) {
                        case ">=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                updateRecord = lVal >= doubleValue;
                            }
                            break;
                        case "<=":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                updateRecord = lVal <= doubleValue;
                            }
                            break;
                        case ">":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                updateRecord = lVal > doubleValue;
                            }
                            break;
                        case "<":
                            if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) && existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                updateRecord = lVal < doubleValue;
                            }
                            break;
                        case "<>":
                            if (existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Math.max(Double.MIN_VALUE + 1, Math.min(Double.MAX_VALUE - 1, doubleValue - 1));
                                if (!existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                                    lVal = Double.parseDouble(existingValue.replace(",", "."));
                                }

                                updateRecord = lVal != doubleValue;
                            }
                            break;
                        case "=":
                        default:
                            if (existingValue.matches("^(\\d*[,.]?\\d*|\\d*)$")) {
                                double lVal = Double.parseDouble(existingValue.replace(",", "."));
                                updateRecord = lVal == doubleValue;
                            }
                            break;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported field " + field + " type: " + fieldType);
            }

            if (updateRecord) {
                updateValue(jsonTable, key, table, rawValue, record, builder);
            }
            modifiedRecords.add(record);
        }

        jsonTable.database.add("records", modifiedRecords);
        if (jsonTable.autoSave) {
            jsonTable.save();
        }
        return builder.build();
    }

    private QueryResult executeSingleGet(final String table, final String search) {
        if (!connection.hasTable(table)) {
            throw new UnsupportedOperationException("Unknown table: " + table);
        }

        JsonConnection jsonTable = connection.createTable(table);
        JsonObject typesJson = jsonTable.database.get("types").getAsJsonObject();

        if (!typesJson.has("schemed") || !typesJson.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot execute json query on non-schemed table");
        }

        if (search.equalsIgnoreCase("*")) {
            return createAllResult(jsonTable.file, table, jsonTable);
        }

        List<String> keys = new ArrayList<>();
        if (search.contains(",")) {
            String[] data = search.split(",");
            for (String str : data) {
                Matcher matcher = keyPattern.matcher(str);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    keys.add(key);
                } else {
                    throw new IllegalStateException("Unknown or invalid json query syntax at: " + str);
                }
            }
        } else {
            Matcher matcher = keyPattern.matcher(search);
            if (matcher.matches()) {
                String key = matcher.group(1);
                keys.add(key);
            } else {
                throw new IllegalStateException("Unknown or invalid json query syntax at: " + search);
            }
        }

        if (keys.isEmpty()) {
            return createAllResult(jsonTable.file, table, jsonTable);
        }

        SimpleQueryResult.QueryResultBuilder builder = SimpleQueryResult.builder().database(PathUtilities.getName(jsonTable.file))
                .table(table);

        JsonArray records = new JsonArray();
        if (jsonTable.database.has("records")) {
            records = jsonTable.database.get("records").getAsJsonArray();
        }

        for (JsonElement element : records) {
            JsonObject record = element.getAsJsonObject();
            builder.nextQuery();

            for (String key : keys) {
                String keyType = jsonTable.getType(key);
                if (keyType.equalsIgnoreCase("table")) continue;

                JsonElement recordElement = record.get(key);
                if (recordElement == null) {
                    recordElement = JsonNull.INSTANCE;
                }

                switch (keyType) {
                    case "string":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (String) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsString());
                        }
                        break;
                    case "boolean":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Boolean) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsBoolean());
                        }
                        break;
                    case "byte":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Byte) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsByte());
                        }
                        break;
                    case "short":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Short) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsShort());
                        }
                        break;
                    case "integer":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Integer) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsInt());
                        }
                        break;
                    case "long":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Long) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsLong());
                        }
                        break;
                    case "float":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Float) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsFloat());
                        }
                        break;
                    case "double":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Double) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsDouble());
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return builder.build();
    }

    private QueryResult executeGetWhere(final String table, final List<String> keys, final String search, final String query) {
        if (!connection.hasTable(table)) {
            throw new UnsupportedOperationException("Unknown table: " + table);
        }

        JsonConnection jsonTable = connection.createTable(table);
        JsonObject typesJson = jsonTable.database.get("types").getAsJsonObject();

        if (!typesJson.get("schemed").getAsBoolean()) {
            throw new UnsupportedOperationException("Cannot execute json query on non-schemed table");
        }

        Matcher multiWhere = whereAndOrPattern.matcher(search);
        boolean first = true;

        List<QueryPart> parts = new ArrayList<>();
        while (multiWhere.find()) {
            int start = multiWhere.start();
            int end = multiWhere.end();

            String sub = search.substring(start, end);
            while (sub.startsWith(" ")) {
                sub = sub.substring(1);
            }

            if (!sub.toLowerCase().startsWith("and") && !sub.toLowerCase().startsWith("or")) {
                if (first) {
                    first = false;

                    parts.add(new RawQuery(sub, false, false, true));
                    continue;
                }

                throw new IllegalStateException("Unexpected json database syntax at: " + query + ". Expected AND or OR");
            }

            if (sub.toLowerCase().startsWith("and")) {
                parts.add(new RawQuery(sub, true, false, false));
                continue;
            }

            parts.add(new RawQuery(sub, false, true, false));
        }
        SimpleQuery sq = new SimpleQuery(parts);

        SimpleQueryResult.QueryResultBuilder builder = SimpleQueryResult.builder().database(PathUtilities.getName(connection.file))
                .table(table);


        JsonArray records = new JsonArray();
        if (jsonTable.database.has("records")) {
            records = jsonTable.database.getAsJsonArray("records");
        }

        for (JsonElement recordElement : records) {
            Map<String, GlobalTypeValue<?>> valueMap = new HashMap<>();

            for (String field : keys) {
                JsonObject record = recordElement.getAsJsonObject();
                String fieldType = jsonTable.getType(field);

                if (fieldType.equalsIgnoreCase("null")) {
                    throw new IllegalStateException("Unknown field " + field + " in table " + table);
                }

                JsonElement elementValue = record.get(field);
                switch (fieldType) {
                    case "string":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, String.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsString(), String.class));
                        break;
                    case "boolean":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, Boolean.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsBoolean(), Boolean.class));
                        break;
                    case "byte":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, Byte.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsByte(), Byte.class));
                        break;
                    case "short":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, Short.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsShort(), Short.class));
                        break;
                    case "integer":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, Integer.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsInt(), Integer.class));
                        break;
                    case "long":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, Long.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsLong(), Long.class));
                        break;
                    case "float":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, Float.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsFloat(), Float.class));
                        break;
                    case "double":
                        if (elementValue == null || elementValue.isJsonNull()) {
                            valueMap.put(field, GlobalTypeValue.of(null, Double.class));
                            continue;
                        }

                        valueMap.put(field, GlobalTypeValue.of(elementValue.getAsDouble(), Double.class));
                        break;
                }
            }

            if (sq.test(valueMap)) {
                builder.nextQuery();

                for (String field : valueMap.keySet()) {
                    GlobalTypeValue<?> rs = valueMap.get(field);
                    if (rs.getType().equals(String.class)) {
                        builder.appendResult(field, (String) rs.getValue());
                    } else if (rs.getType().equals(Boolean.class)) {
                        builder.appendResult(field, (Boolean) rs.getValue());
                    } else if (rs.getType().equals(Byte.class)) {
                        builder.appendResult(field, (Byte) rs.getValue());
                    } else if (rs.getType().equals(Short.class)) {
                        builder.appendResult(field, (Short) rs.getValue());
                    } else if (rs.getType().equals(Integer.class)) {
                        builder.appendResult(field, (Integer) rs.getValue());
                    } else if (rs.getType().equals(Long.class)) {
                        builder.appendResult(field, (Long) rs.getValue());
                    } else if (rs.getType().equals(Float.class)) {
                        builder.appendResult(field, (Float) rs.getValue());
                    } else {
                        builder.appendResult(field, (Boolean) rs.getValue());
                    }
                }
            }
        }

        return builder.build();
    }

    private void updateValue(final JsonConnection jsonTable, final String key, final String table, final String rawValue, final JsonObject record, final SimpleQueryResult.QueryResultBuilder builder) {
        switch (jsonTable.getType(key)) {
            case "string":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (String) null);
                    builder.nextQuery().appendResult(key, (String) null);
                } else {
                    record.addProperty(key, rawValue);
                    builder.nextQuery().appendResult(key, rawValue);
                }
                break;
            case "boolean":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (String) null);
                    builder.nextQuery().appendResult(key, (String) null);
                } else {
                    record.addProperty(key, Boolean.parseBoolean(rawValue.replace("1", "true")));
                    builder.nextQuery().appendResult(key, Boolean.parseBoolean(rawValue.replace("1", "true")));
                }
                break;
            case "byte":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (Byte) null);
                    builder.nextQuery().appendResult(key, (Byte) null);
                } else {
                    long slVal = Long.parseLong(rawValue);
                    byte sbVal = (byte) Math.max(Byte.MIN_VALUE, Math.min(slVal, Byte.MAX_VALUE));

                    record.addProperty(key, sbVal);
                    builder.nextQuery().appendResult(key, sbVal);
                }
                break;
            case "short":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (Short) null);
                    builder.nextQuery().appendResult(key, (Short) null);
                } else {
                    long slVal = Long.parseLong(rawValue);
                    short sbVal = (short) Math.max(Short.MIN_VALUE, Math.min(slVal, Short.MAX_VALUE));

                    record.addProperty(key, sbVal);
                    builder.nextQuery().appendResult(key, sbVal);
                }
                break;
            case "integer":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (Integer) null);
                    builder.nextQuery().appendResult(key, (Integer) null);
                } else {
                    long slVal = Long.parseLong(rawValue);
                    int sbVal = (int) Math.max(Integer.MIN_VALUE, Math.min(slVal, Integer.MAX_VALUE));

                    record.addProperty(key, sbVal);
                    builder.nextQuery().appendResult(key, sbVal);
                }
                break;
            case "long":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (Long) null);
                    builder.nextQuery().appendResult(key, (Long) null);
                } else {
                    long slVal = Long.parseLong(rawValue);

                    record.addProperty(key, slVal);
                    builder.nextQuery().appendResult(key, slVal);
                }
                break;
            case "float":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (Float) null);
                    builder.nextQuery().appendResult(key, (Float) null);
                } else {
                    double slVal = Double.parseDouble(rawValue.replace(",", "."));
                    float sfVal = (float) Math.max(Float.MIN_VALUE, Math.min(Float.MIN_VALUE, slVal));

                    record.addProperty(key, sfVal);
                    builder.nextQuery().appendResult(key, sfVal);
                }
                break;
            case "double":
                if (rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
                    if (hasModifier(jsonTable.database, key, "nn")) {
                        throw new IllegalStateException("Cannot set null value for non null key " + key + " on table " + table);
                    }

                    record.addProperty(key, (Double) null);
                    builder.nextQuery().appendResult(key, (Double) null);
                } else {
                    double slVal = Double.parseDouble(rawValue.replace(",", "."));

                    record.addProperty(key, slVal);
                    builder.nextQuery().appendResult(key, slVal);
                }
                break;
        }

    }

    @NotNull
    private Map<String, BiValue<String[]>> getTypesFromString(final String schema) {
        Map<String, BiValue<String[]>> types = new HashMap<>();
        if (schema.contains(",")) {
            String[] data = schema.split(",");

            for (String str : data) {
                List<String> mods = new ArrayList<>();
                Matcher matcher = keyTypePattern.matcher(str);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String type = matcher.group(2);

                    if (type.equalsIgnoreCase("int")) {
                        type = "integer";
                    }

                    if (types.containsKey(key)) {
                        throw new IllegalStateException("Invalid json database syntax, duplicate key: " + key);
                    }

                    for (int i = 3; i <= matcher.groupCount(); i++) {
                        String value = matcher.group(i);
                        if (value != null) mods.add(matcher.group(i));
                    }

                    types.put(key, BiValue.of(new String[]{type}, mods.toArray(new String[0])));
                } else {
                    throw new IllegalStateException("Invalid json database syntax, invalid schema data: " + str);
                }
            }
        } else {
            List<String> mods = new ArrayList<>();
            Matcher matcher = keyTypePattern.matcher(schema);
            if (matcher.matches()) {
                String key = matcher.group(1);
                String type = matcher.group(2);

                if (type.equalsIgnoreCase("int")) {
                    type = "integer";
                }

                for (int i = 3; i <= matcher.groupCount(); i++) {
                    String value = matcher.group(i);
                    if (value != null) mods.add(matcher.group(i));
                }

                types.put(key, BiValue.of(new String[]{type}, mods.toArray(new String[0])));
            } else {
                throw new IllegalStateException("Invalid json database syntax, invalid schema data: " + schema);
            }
        }
        return types;
    }

    private QueryResult createAllResult(final Path file, final String table, final JsonConnection jsonTable) {
        SimpleQueryResult.QueryResultBuilder builder = SimpleQueryResult.builder().database(PathUtilities.getName(file))
                .table(table);

        JsonArray records = new JsonArray();
        if (jsonTable.database.has("records")) {
            records = jsonTable.database.get("records").getAsJsonArray();
        }

        for (JsonElement element : records) {
            builder.nextQuery();
            JsonObject record = element.getAsJsonObject();

            for (String key : jsonTable.getKeys()) {
                String type = jsonTable.getType(key);
                if (type.equalsIgnoreCase("table") || key.equals("schemed")  || !jsonTable.database.get("types").getAsJsonObject().get(key).isJsonPrimitive()) continue;

                JsonElement recordElement = record.get(key);
                if (recordElement == null) {
                    recordElement = JsonNull.INSTANCE;
                }

                switch (type) {
                    case "string":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (String) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsString());
                        }
                        break;
                    case "boolean":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Boolean) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsBoolean());
                        }
                        break;
                    case "byte":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Byte) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsByte());
                        }
                        break;
                    case "short":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Short) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsShort());
                        }
                        break;
                    case "integer":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Integer) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsInt());
                        }
                        break;
                    case "long":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Long) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsLong());
                        }
                        break;
                    case "float":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Float) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsFloat());
                        }
                        break;
                    case "double":
                        if (recordElement.isJsonNull()) {
                            builder.appendResult(key, (Double) null);
                        } else {
                            builder.appendResult(key, recordElement.getAsDouble());
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return builder.build();
    }

    private boolean hasModifier(final JsonObject object, final String key, final String modifier) {
        JsonObject typesObject = object.getAsJsonObject("types");
        if (!typesObject.has("modifiers") && !typesObject.get("modifiers").isJsonObject()) return false;

        JsonObject modifiers = typesObject.get("modifiers").getAsJsonObject();
        if (!modifiers.has(key)) return false;

        JsonArray keyModifiers = modifiers.get(key).getAsJsonArray();
        return keyModifiers.contains(new JsonPrimitive(modifier));
    }

    private String[] getFieldsWithModifiers(final JsonObject object, final String modifier) {
        JsonObject typesObject = object.getAsJsonObject("types");
        if (!typesObject.has("modifiers") && !typesObject.get("modifiers").isJsonObject()) return new String[]{};

        JsonObject modifiers = typesObject.get("modifiers").getAsJsonObject();
        List<String> keys = new ArrayList<>();

        for (String key : modifiers.keySet()) {
            JsonArray keyModifiers = modifiers.get(key).getAsJsonArray();
            if (keyModifiers.contains(new JsonPrimitive(modifier))) {
                keys.add(key);
            }
        }

        return keys.toArray(new String[0]);
    }

    private int getOrCreateIntAttribute(final JsonObject object, final String key, final BiConsumer<Integer, JsonObject> onGet) {
        JsonObject typesObject = object.getAsJsonObject("types");
        if (!typesObject.has("attributes") && !typesObject.get("attributes").isJsonObject()) {
            JsonObject attributes = new JsonObject();
            attributes.addProperty(key, 0);

            typesObject.add("attributes", attributes);
            onGet.accept(0, attributes);
            return 0;
        }

        JsonObject attributes = typesObject.get("attributes").getAsJsonObject();
        if (!attributes.has(key)) {
            attributes.addProperty(key, 0);

            typesObject.add("attributes", attributes);
            onGet.accept(0, attributes);
            return 0;
        }

        JsonPrimitive value = attributes.get(key).getAsJsonPrimitive();
        int v = value.getAsInt();
        onGet.accept(v, attributes);

        return v;
    }
}