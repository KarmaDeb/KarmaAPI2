package es.karmadev.api.database.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import es.karmadev.api.database.result.QueryResult;
import es.karmadev.api.database.result.SimpleQueryResult;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.strings.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Json connection query executor
 */
class JsonConnectionExecutor {

    private final static String DEFAULT_NULL_VALUE = StringUtils.generateString();

    private final JsonConnection connection;

    public JsonConnectionExecutor(final JsonConnection connection) {
        this.connection = connection;
    }

    public QueryResult execute(final String query) {
        Pattern createQueryPattern = Pattern.compile("^SETUP TABLE '([\\w\\s]+)';$", Pattern.CASE_INSENSITIVE);

        Pattern defineSchemaPattern = Pattern.compile("^IN TABLE '([\\w\\s]+)' SCHEMA IS (\\(.*\\));$", Pattern.CASE_INSENSITIVE);
        Pattern singleDefinePattern = Pattern.compile("^IN TABLE '([\\w\\s]+)' SET KEY '(\\w*)' VALUE TO ('[^']*'|[+-]?\\d*|[+-]?\\d*[,.]\\d*|false|true|NULL);$", Pattern.CASE_INSENSITIVE);
        Pattern singleDefineWherePattern = Pattern.compile("^IN TABLE '([\\w\\s]+)' SET KEY '(\\w*)' VALUE TO ('[^']*'|[+-]?\\d*|[+-]?\\d*[,.]\\d*|false|true|NULL) WHEREVER '(\\w*)' (=|>|<|>=|<=|<>) ('[^']*'|[+-]?\\d*|[+-]?\\d*[,.]\\d*|false|true|NULL);$", Pattern.CASE_INSENSITIVE);

        Matcher createMatcher = createQueryPattern.matcher(query);
        if (createMatcher.matches()) {
            return executeCreate(createMatcher.group(1));
        }

        Matcher defineSchemaMatcher = defineSchemaPattern.matcher(query);
        if (defineSchemaMatcher.matches()) {
            return executeScheme(defineSchemaMatcher, query);
        }

        Matcher singleDefineWhereMatcher = singleDefineWherePattern.matcher(query);
        if (singleDefineWhereMatcher.matches()) {
            String table = singleDefineWhereMatcher.group(1);
            String key = singleDefineWhereMatcher.group(2);
            String value = singleDefineWhereMatcher.group(3);
            String field = singleDefineWhereMatcher.group(4);
            String comparator = singleDefineWhereMatcher.group(5);
            String requiredValue = singleDefineWhereMatcher.group(6);

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

        Matcher singleDefineMatcher = singleDefinePattern.matcher(query);
        if (singleDefineMatcher.matches()) {
            String table = singleDefineMatcher.group(1);
            String key = singleDefineMatcher.group(2);
            String value = singleDefineMatcher.group(3);
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            } else {
                if (value.equals("NULL")) {
                    value = DEFAULT_NULL_VALUE;
                }
            }

            return executeSingleDefine(table, key, value);
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
        Pattern keyTypePattern = Pattern.compile("\\s*'(\\w+)'\\s*(string|boolean|byte|short|integer|int|long|float|double)\\s*(NN|AI)?\\s*(NN|AI)?", Pattern.CASE_INSENSITIVE);
        Map<String, BiValue<String[]>> types = getTypesFromString(schema, keyTypePattern);

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

    private QueryResult executeSingleDefine(final String table, final String key, final String rawValue) {
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

        if (!rawValue.equalsIgnoreCase(DEFAULT_NULL_VALUE)) {
            String type = typesJson.get(key).getAsString();
            if (type.equalsIgnoreCase("boolean")) {
                if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false") &&
                        !rawValue.equalsIgnoreCase("1") && !rawValue.equalsIgnoreCase("0")) {
                    throw new IllegalStateException("Cannot set non boolean value " + rawValue + " for boolean field " + key + " in table " + table);
                }
            }

            if (!type.equalsIgnoreCase("string") && !rawValue.matches("^[+-]?(\\d*[,.]\\d+)|(\\d*)$")) {
                throw new IllegalStateException("Cannot set non numeric value " + rawValue + " for numeric field " + key + " in table " + table);
            }
        }

        SimpleQueryResult.QueryResultBuilder builder = SimpleQueryResult.builder().database(PathUtilities.getName(connection.file))
                .table(table);

        String type = jsonTable.getType(key);
        JsonArray records = new JsonArray();
        if (jsonTable.database.has("records")) {
            records = jsonTable.database.getAsJsonArray("records");
        }

        JsonObject recordElement = new JsonObject();
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

        for (String field : getFieldsWithModifiers(jsonTable.database, "ai")) {
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
            }

            if (!type.equalsIgnoreCase("string") && !rawValue.matches("^[+-]?(\\d*[,.]\\d+)|(\\d*)$")) {
                throw new IllegalStateException("Cannot set non numeric value " + rawValue + " for numeric field " + key + " in table " + table);
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
                        case ">":
                        case "<=":
                        case "<":
                            throw new UnsupportedOperationException("Unsupported string operator: " + comparator);
                        case "<>":
                            updateRecord = existingValue.equalsIgnoreCase(DEFAULT_NULL_VALUE) || !existingValue.equalsIgnoreCase(stringValue);
                            break;
                        case "=":
                        default:
                            updateRecord = existingValue.equalsIgnoreCase(stringValue);
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
    private static Map<String, BiValue<String[]>> getTypesFromString(final String schema, final Pattern keyTypePattern) {
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

    private static QueryResult createAllResult(final Path file, final String table, final JsonConnection jsonTable) {
        SimpleQueryResult.QueryResultBuilder builder = SimpleQueryResult.builder().database(PathUtilities.getName(file))
                .table(table);

        for (String key : jsonTable.getKeys()) {
            String type = jsonTable.getType(key);
            if (type.equalsIgnoreCase("table")) continue;
            switch (type) {
                case "string":
                    builder.appendResult(key, jsonTable.getString(key));
                    break;
                case "boolean":
                    builder.appendResult(key, jsonTable.getBoolean(key));
                    break;
                case "byte":
                    builder.appendResult(key, (Byte) jsonTable.getNumber(key));
                    break;
                case "short":
                    builder.appendResult(key, (Short) jsonTable.getNumber(key));
                    break;
                case "integer":
                    builder.appendResult(key, (Integer) jsonTable.getNumber(key));
                    break;
                case "long":
                    builder.appendResult(key, (Long) jsonTable.getNumber(key));
                    break;
                case "float":
                    builder.appendResult(key, (Float) jsonTable.getNumber(key));
                    break;
                case "double":
                    builder.appendResult(key, (Double) jsonTable.getNumber(key));
                    break;
                default:
                    break;

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
