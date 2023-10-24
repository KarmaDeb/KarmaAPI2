package es.karmadev.api.database.result;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Simple query result, for building
 * query results on custom database implementations
 */
@SuppressWarnings("unused") @ToString @EqualsAndHashCode
public class SimpleQueryResult implements QueryResult {

    private final String database;
    private final String table;

    private int index = -1;

    Map<Integer, String>[] indexes;
    Map<String, Byte>[] byteMap;
    Map<String, Short>[] shortMap;
    Map<String, Integer>[] intMap;
    Map<String, Long>[] longMap;
    Map<String, Float>[] floatMap;
    Map<String, Double>[] doubleMap;
    Map<String, String>[] stringMap;
    Map<String, Boolean>[] boolMap;

    private Object lastObject = null;

    public SimpleQueryResult(final String database, final String table) {
        this.database = database;
        this.table = table;
    }

    /**
     * Goes to the next query result
     *
     * @return true if there are more results
     */
    @Override
    public boolean next() {
        if (indexes.length > index + 1) {
            index += 1;
            return true;
        }

        return false;
    }

    /**
     * Get the result fields
     *
     * @return the result fields
     */
    @Override
    public int fields() {
        return indexes[Math.max(0, Math.min(indexes.length - 1, this.index))].size();
    }

    /**
     * Get the database we are at
     *
     * @return the database name
     */
    @Override
    public String databaseName() {
        return database;
    }

    /**
     * Get the table we are at, for
     * non-relational database types, such
     * as mongo, this should be the collection
     * name instead
     *
     * @return the table name of the result
     */
    @Override
    public String tableName() {
        return table;
    }

    /**
     * Get the result field names
     *
     * @return the field names
     */
    @Override
    public String[] fieldNames() {
        return indexes[Math.max(0, Math.min(indexes.length - 1, this.index))].values().toArray(new String[0]);
    }

    /**
     * Get if the latest result fetched
     * was null
     *
     * @return if the result was null
     */
    @Override
    public boolean wasNull() {
        return lastObject == null;
    }

    /**
     * Get a byte value
     *
     * @param field the field to get the byte
     *              from
     * @return the byte value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public byte getByte(final String field) throws NoSuchElementException {
        Byte val = indexOfMap(field, byteMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a byte value
     *
     * @param index the field index to get the
     *              byte from
     * @return the byte value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        Byte val = indexOfMap(index, byteMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a short value
     *
     * @param field the field to get the short
     *              from
     * @return the short value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public short getShort(final String field) throws NoSuchElementException {
        Short val = indexOfMap(field, shortMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a short value
     *
     * @param index the field index to get the
     *              short from
     * @return the short value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        Short val = indexOfMap(index, shortMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get an integer value
     *
     * @param field the field to get the integer
     *              from
     * @return the integer value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public int getInteger(final String field) throws NoSuchElementException {
        Integer val = indexOfMap(field, intMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get an integer value
     *
     * @param index the field index to get the integer
     *              from
     * @return the integer value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public int getInteger(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        Integer val = indexOfMap(index, intMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a long value
     *
     * @param field the field to get the long
     *              from
     * @return the long value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public long getLong(final String field) throws NoSuchElementException {
        Long val = indexOfMap(field, longMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a long value
     *
     * @param index the field index to get the
     *              long from
     * @return the long value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public long getLong(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        Long val = indexOfMap(index, longMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a double value
     *
     * @param field the field to get the double
     *              from
     * @return the double value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public double getDouble(final String field) throws NoSuchElementException {
        Double val = indexOfMap(field, doubleMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a double value
     *
     * @param index the field index to get the
     *              double from
     * @return the double value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public double getDouble(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        Double val = indexOfMap(index, doubleMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a float value
     *
     * @param field the field name to get the
     *              float from
     * @return the float value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public float getFloat(final String field) throws NoSuchElementException {
        Float val = indexOfMap(field, floatMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a float value
     *
     * @param index the field index to get the
     *              float from
     * @return the float value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public float getFloat(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        Float val = indexOfMap(index, floatMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (val == null) return 0;

        return val;
    }

    /**
     * Get a string value
     *
     * @param field the field to get the string
     *              from
     * @return the string value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public String getString(final String field) throws NoSuchElementException {
        return indexOfMap(field, stringMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
    }

    /**
     * Get a string value
     *
     * @param index the field index to get the
     *              string from
     * @return the string value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public String getString(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        return indexOfMap(index, stringMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
    }

    /**
     * Get a boolean value
     *
     * @param field the field to get the boolean
     *              from
     * @return the boolean value
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    @Override
    public boolean getBoolean(final String field) throws NoSuchElementException {
        Boolean bool = indexOfMap(field, boolMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (bool == null) return false;

        return bool;
    }

    /**
     * Get a boolean value
     *
     * @param index the field index to get the
     *              boolean from
     * @return the boolean value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException      if the specified type does not contain the data
     */
    @Override
    public boolean getBoolean(final int index) throws IndexOutOfBoundsException, NoSuchElementException {
        Boolean bool = indexOfMap(index, boolMap[Math.max(0, Math.min(indexes.length - 1, this.index))]);
        if (bool == null) return false;

        return bool;
    }

    /**
     * Get a value of a map based on
     * a provided field
     *
     * @param field the field
     * @param map the map to get data from
     * @return the value
     * @param <T> the value type
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    private <T> T indexOfMap(final String field, final Map<String, T> map) throws NoSuchElementException {
        if (!map.containsKey(field)) {
            throw new NoSuchElementException("No field named " + field + " in result set");
        }

        T v = map.get(field);
        lastObject = v;
        return v;
    }

    /**
     * Get a value of a map based on
     * a provided index
     *
     * @param index the index
     * @param map the map to get data from
     * @return the value
     * @param <T> the value type
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchElementException if the specified type does not contain the data
     */
    private <T> T indexOfMap(final int index, final Map<String, T> map) throws IndexOutOfBoundsException, NoSuchElementException {
        if (index < 0 || index > indexes[this.index].size()) {
            throw new IndexOutOfBoundsException("No field at index " + index);
        }

        int searchIndex = 0;
        int i = 0;
        for (Map.Entry<Integer, String> indexes : this.indexes[this.index].entrySet()) {
            if (i++ == index) {
                searchIndex = indexes.getKey();
                break;
            }
        }

        String fieldName = indexes[this.index].get(searchIndex);
        if (!map.containsKey(fieldName)) {
            throw new NoSuchElementException("No field named " + fieldName + " in result set");
        }

        T v = map.get(fieldName);
        lastObject = v;
        return v;
    }

    /**
     * Get a query result builder
     *
     * @return a new result builder
     */
    public static QueryResultBuilder builder() {
        return new QueryResultBuilder();
    }

    public static class QueryResultBuilder {

        private String table;
        private String database;
        private int index = 0;

        private int writeIndex = -1;
        private final List<Map<Integer, String>> indexes = new ArrayList<>();
        private final List<Map<String, Byte>> byteMap = new ArrayList<>();
        private final List<Map<String, Short>> shortMap = new ArrayList<>();
        private final List<Map<String, Integer>> intMap = new ArrayList<>();
        private final List<Map<String, Long>> longMap = new ArrayList<>();
        private final List<Map<String, Float>> floatMap = new ArrayList<>();
        private final List<Map<String, Double>> doubleMap = new ArrayList<>();
        private final List<Map<String, String>> stringMap = new ArrayList<>();
        private final List<Map<String, Boolean>> boolMap = new ArrayList<>();

        private QueryResultBuilder() {
            indexes.add(new HashMap<>());
            byteMap.add(new LinkedHashMap<>());
            shortMap.add(new LinkedHashMap<>());
            intMap.add(new LinkedHashMap<>());
            longMap.add(new LinkedHashMap<>());
            floatMap.add(new LinkedHashMap<>());
            doubleMap.add(new LinkedHashMap<>());
            stringMap.add(new LinkedHashMap<>());
            boolMap.add(new LinkedHashMap<>());
        }

        public QueryResultBuilder table(final String table) {
            this.table = table;
            return this;
        }

        public QueryResultBuilder database(final String database) {
            this.database = database;
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final Byte value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, Byte> byteMap = this.byteMap.get(writeIndex);

            checkDuplication(fieldName, byteMap);
            byteMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final Short value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, Short> shortMap = this.shortMap.get(writeIndex);

            checkDuplication(fieldName, shortMap);
            shortMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final Integer value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, Integer> intMap = this.intMap.get(writeIndex);

            checkDuplication(fieldName, intMap);
            intMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final Long value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, Long> longMap = this.longMap.get(writeIndex);

            checkDuplication(fieldName, longMap);
            longMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final Float value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, Float> floatMap = this.floatMap.get(writeIndex);

            checkDuplication(fieldName, floatMap);
            floatMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final Double value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, Double> doubleMap = this.doubleMap.get(writeIndex);

            checkDuplication(fieldName, doubleMap);
            doubleMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final String value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, String> stringMap = this.stringMap.get(writeIndex);

            checkDuplication(fieldName, stringMap);
            stringMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        public QueryResultBuilder appendResult(final String fieldName, final Boolean value) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            Map<Integer, String> indexes = this.indexes.get(writeIndex);
            Map<String, Boolean> boolMap = this.boolMap.get(writeIndex);

            checkDuplication(fieldName, boolMap);
            boolMap.put(fieldName, value);
            indexes.put(index++, fieldName);
            return this;
        }

        /**
         * Add a new query to the builder
         *
         * @return the new query builder
         */
        public QueryResultBuilder nextQuery() {
            if (indexes.get(Math.max(0, Math.min(writeIndex, indexes.size() - 1))).isEmpty()) {
                return this;
            }

            writeIndex++;
            indexes.add(new HashMap<>());
            byteMap.add(new LinkedHashMap<>());
            shortMap.add(new LinkedHashMap<>());
            intMap.add(new LinkedHashMap<>());
            longMap.add(new LinkedHashMap<>());
            floatMap.add(new LinkedHashMap<>());
            doubleMap.add(new LinkedHashMap<>());
            stringMap.add(new LinkedHashMap<>());
            boolMap.add(new LinkedHashMap<>());

            return this;
        }

        /**
         * Add the other query result builder
         * to the current one
         *
         * @param other the other query result builder
         * @return this builder
         */
        public QueryResultBuilder add(final QueryResultBuilder other) {
            if (writeIndex < 0) {
                writeIndex = 0;
            }
            if (!indexes.get(writeIndex).isEmpty()) {
                throw new UnsupportedOperationException("Cannot add another query result to a used query builder");
            }

            indexes.addAll(other.indexes);
            byteMap.addAll(other.byteMap);
            shortMap.addAll(other.shortMap);
            intMap.addAll(other.intMap);
            longMap.addAll(other.longMap);
            floatMap.addAll(other.floatMap);
            doubleMap.addAll(other.doubleMap);
            stringMap.addAll(other.stringMap);
            boolMap.addAll(other.boolMap);

            writeIndex += other.writeIndex;
            return this;
        }

        private void checkDuplication(final String fieldName, final Object container) throws IllegalStateException {
            if (writeIndex < 0) {
                writeIndex = 0;
            }

            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    Object value = field.get(this);
                    if (value.equals(container)) continue;

                    if (value instanceof List<?>) {
                        value = ((List<?>) value).get(writeIndex);

                        if (value instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) value;
                            if (map.containsKey(fieldName)) {
                                throw new IllegalStateException("Cannot set field " + fieldName + " because it has been already set for another value");
                            }
                        }
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }

        @SuppressWarnings("unchecked")
        public SimpleQueryResult build() {
            SimpleQueryResult rs = new SimpleQueryResult(database, table);
            rs.indexes = indexes.toArray(new Map[0]);
            rs.byteMap = byteMap.toArray(new Map[0]);
            rs.shortMap = shortMap.toArray(new Map[0]);
            rs.intMap = intMap.toArray(new Map[0]);
            rs.longMap = longMap.toArray(new Map[0]);
            rs.floatMap = floatMap.toArray(new Map[0]);
            rs.doubleMap = doubleMap.toArray(new Map[0]);
            rs.stringMap = stringMap.toArray(new Map[0]);
            rs.boolMap = boolMap.toArray(new Map[0]);

            return rs;
        }
    }
}
