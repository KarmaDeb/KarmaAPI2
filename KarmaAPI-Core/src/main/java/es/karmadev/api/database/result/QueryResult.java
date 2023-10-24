package es.karmadev.api.database.result;

/**
 * KarmaAPI database connection query result
 */
public interface QueryResult {

    /**
     * Goes to the next query result
     *
     * @return true if there are more results
     */
    boolean next();

    /**
     * Get the result fields
     *
     * @return the result fields
     */
    int fields();

    /**
     * Get the database we are at
     *
     * @return the database name
     */
    String databaseName();

    /**
     * Get the table we are at, for
     * non-relational database types, such
     * as mongo, this should be the collection
     * name instead
     *
     * @return the table name of the result
     */
    String tableName();

    /**
     * Get the result field names
     *
     * @return the field names
     */
    String[] fieldNames();

    /**
     * Get if the latest result fetched
     * was null
     *
     * @return if the result was null
     */
    boolean wasNull();

    /**
     * Get a byte value
     *
     * @param field the field to get the byte
     *              from
     * @return the byte value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    byte getByte(final String field) throws NoSuchFieldException;

    /**
     * Get a byte value
     *
     * @param index the field index to get the
     *              byte from
     * @return the byte value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    byte getByte(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;

    /**
     * Get a short value
     *
     * @param field the field to get the short
     *              from
     * @return the short value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    short getShort(final String field) throws NoSuchFieldException;

    /**
     * Get a short value
     *
     * @param index the field index to get the
     *              short from
     * @return the short value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    short getShort(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;

    /**
     * Get an integer value
     *
     * @param field the field to get the integer
     *              from
     * @return the integer value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    int getInteger(final String field) throws NoSuchFieldException;

    /**
     * Get an integer value
     *
     * @param index the field index to get the integer
     *              from
     * @return the integer value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    int getInteger(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;

    /**
     * Get a long value
     *
     * @param field the field to get the long
     *              from
     * @return the long value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    long getLong(final String field) throws NoSuchFieldException;

    /**
     * Get a long value
     *
     * @param index the field index to get the
     *              long from
     * @return the long value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    long getLong(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;

    /**
     * Get a double value
     *
     * @param field the field to get the double
     *              from
     * @return the double value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    double getDouble(final String field) throws NoSuchFieldException;

    /**
     * Get a double value
     *
     * @param index the field index to get the
     *              double from
     * @return the double value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    double getDouble(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;

    /**
     * Get a float value
     *
     * @param field the field name to get the
     *              float from
     * @return the float value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    float getFloat(final String field) throws NoSuchFieldException;

    /**
     * Get a float value
     *
     * @param index the field index to get the
     *              float from
     * @return the float value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    float getFloat(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;

    /**
     * Get a string value
     *
     * @param field the field to get the string
     *              from
     * @return the string value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    String getString(final String field) throws NoSuchFieldException;

    /**
     * Get a string value
     *
     * @param index the field index to get the
     *              string from
     * @return the string value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    String getString(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;

    /**
     * Get a boolean value
     *
     * @param field the field to get the boolean
     *              from
     * @return the boolean value
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    boolean getBoolean(final String field) throws NoSuchFieldException;

    /**
     * Get a boolean value
     *
     * @param index the field index to get the
     *              boolean from
     * @return the boolean value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * @throws NoSuchFieldException if the specified type does not contain the data
     */
    boolean getBoolean(final int index) throws IndexOutOfBoundsException, NoSuchFieldException;
}
