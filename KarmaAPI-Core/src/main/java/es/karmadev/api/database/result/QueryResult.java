package es.karmadev.api.database.result;

import java.lang.reflect.Type;

/**
 * KarmaAPI database connection query result
 */
public interface QueryResult {

    /**
     * Get the result fields
     *
     * @return the result fields
     */
    int fields();

    /**
     * Get the result field names
     *
     * @return the field names
     */
    String[] fieldNames();

    /**
     * Get the result type
     *
     * @return the result type
     */
    Type result();

    /**
     * Get a long value
     *
     * @param field the field to get the long
     *              from
     * @return the long value
     */
    long getLong(final String field);

    /**
     * Get a long value
     *
     * @param index the field index to get the
     *              long from
     * @return the long value
     */
    long getLong(final int index);

    /**
     * Get a short value
     *
     * @param field the field to get the short
     *              from
     * @return the short value
     */
    short getShort(final String field);

    /**
     * Get a short value
     *
     * @param index the field index to get the
     *              short from
     * @return the short value
     */
    short getShort(final int index);

    /**
     * Get an integer value
     *
     * @param field the field to get the integer
     *              from
     * @return the integer value
     */
    int getInteger(final String field);

    /**
     * Get an integer value
     *
     * @param index the field index to get the integer
     *              from
     * @return the integer value
     */
    int getInteger(final int index);

    /**
     * Get a double value
     *
     * @param field the field to get the double
     *              from
     * @return the double value
     */
    double getDouble(final String field);

    /**
     * Get a double value
     *
     * @param index the field index to get the
     *              double from
     * @return the double value
     */
    double getDouble(final int index);

    /**
     * Get a float value
     *
     * @param field the field name to get the
     *              float from
     * @return the float value
     */
    float getFloat(final String field);

    /**
     * Get a float value
     *
     * @param index the field index to get the
     *              float from
     * @return the float value
     */
    float getFloat(final int index);

    /**
     * Get a byte value
     *
     * @param field the field to get the byte
     *              from
     * @return the byte value
     */
    byte getByte(final String field);

    /**
     * Get a byte value
     *
     * @param index the field index to get the
     *              byte from
     * @return the byte value
     */
    byte getByte(final int index);

    /**
     * Get a string value
     *
     * @param field the field to get the string
     *              from
     * @return the string value
     */
    String getString(final String field);

    /**
     * Get a string value
     *
     * @param index the field index to get the
     *              string from
     * @return the string value
     */
    String getString(final int index);

    /**
     * Get a boolean value
     *
     * @param field the field to get the boolean
     *              from
     * @return the boolean value
     */
    boolean getBoolean(final String field);

    /**
     * Get a boolean value
     *
     * @param index the field index to get the
     *              boolean from
     * @return the boolean value
     */
    boolean getBoolean(final int index);
}
