package es.karmadev.api.kson;

import es.karmadev.api.kson.object.JsonNull;
import es.karmadev.api.kson.object.type.NativeBoolean;
import es.karmadev.api.kson.object.type.NativeNumber;
import es.karmadev.api.kson.object.type.NativeString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a json instances which
 * holds a java native element
 */
@SuppressWarnings("unused")
public interface JsonNative extends JsonInstance {

    /**
     * Get the json native instance of
     * a character sequence object. If the
     * sequence is null, an instance of {@link JsonNull null}
     * will be returned
     *
     * @param sequence the sequence
     * @return the sequence as native json element
     */
    static JsonNative forSequence(final CharSequence sequence) {
        return forSequence("", '.', sequence);
    }

    /**
     * Get the json native instance of
     * the number. If the number is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param number the number
     * @return the number as a native element
     */
    static JsonNative forNumber(final Number number) {
        return forNumber("", '.', number);
    }

    /**
     * Get the json native instance of
     * the boolean. If the boolean is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param bool the boolean
     * @return the boolean as a native element
     */
    static JsonNative forBoolean(final Boolean bool) {
        return forBoolean("", '.', bool);
    }

    /**
     * Get the json native instance of
     * a character sequence object. If the
     * sequence is null, an instance of {@link JsonNull null}
     * will be returned
     *
     * @param path the native element path
     * @param sequence the sequence
     * @return the sequence as native json element
     */
    static JsonNative forSequence(final String path, final CharSequence sequence) {
        return forSequence(path, '.', sequence);
    }

    /**
     * Get the json native instance of
     * the number. If the number is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param number the number
     * @return the number as a native element
     */
    static JsonNative forNumber(final String path, final Number number) {
        return forNumber(path, '.', number);
    }

    /**
     * Get the json native instance of
     * the boolean. If the boolean is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param bool the boolean
     * @return the boolean as a native element
     */
    static JsonNative forBoolean(final String path, final Boolean bool) {
        return forBoolean(path, '.', bool);
    }

    /**
     * Get the json native instance of
     * a character sequence object. If the
     * sequence is null, an instance of {@link JsonNull null}
     * will be returned
     *
     * @param path the native element path
     * @param pathSeparator the native element path separator
     * @param sequence the sequence
     * @return the sequence as native json element
     */
    static JsonNative forSequence(final String path, final char pathSeparator, final CharSequence sequence) {
        if (sequence == null) return JsonNull.get(path, pathSeparator);
        return new NativeString(sequence.toString());
    }

    /**
     * Get the json native instance of
     * the number. If the number is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param pathSeparator the native element path separator
     * @param number the number
     * @return the number as a native element
     */
    static JsonNative forNumber(final String path, final char pathSeparator, final Number number) {
        if (number == null) return JsonNull.get(path, pathSeparator);
        return new NativeNumber(number);
    }

    /**
     * Get the json native instance of
     * the boolean. If the boolean is null,
     * an instance of {@link JsonNull null} will
     * be returned.
     *
     * @param path the native element path
     * @param pathSeparator the native element path separator
     * @param bool the boolean
     * @return the boolean as a native element
     */
    static JsonNative forBoolean(final String path, final char pathSeparator, final Boolean bool) {
        if (bool == null) return JsonNull.get(path, pathSeparator);
        return new NativeBoolean(bool);
    }

    /**
     * Returns whether the element
     * is a string
     *
     * @return if the element is a string
     */
    boolean isString();

    /**
     * Returns whether the element
     * is a number
     *
     * @return if the element is a number
     */
    boolean isNumber();

    /**
     * Returns whether the element is a
     * boolean
     *
     * @return if the element is a boolean
     */
    boolean isBoolean();

    /**
     * Get the string value of the element
     *
     * @return the string value
     * @throws UnsupportedOperationException if the element
     * is not a string
     */
    @NotNull
    String getString() throws UnsupportedOperationException;

    /**
     * Get the number value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    @NotNull
    Number getNumber() throws UnsupportedOperationException;

    /**
     * Get the boolean value of the element
     *
     * @return the boolean element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    boolean getBoolean() throws UnsupportedOperationException;

    /**
     * Get the string value of the element. The
     * main difference between this method and
     * {@link #getString()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the string value
     */
    @Nullable
    String getAsString();

    /**
     * Get the number value of the element. The
     * main difference between this method and
     * {@link #getNumber()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the number element
     */
    @Nullable
    Number getAsNumber();

    /**
     * Get the boolean value of the element. The
     * main difference between this method and
     * {@link #getBoolean()} is that this method tries
     * to converse the current element type into the
     * requested one. If it fails, it returns null
     *
     * @return the boolean element
     */
    @Nullable
    Boolean getAsBoolean();

    /**
     * Get the byte value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    default byte getByte() throws UnsupportedOperationException {
        return getNumber().byteValue();
    }

    /**
     * Get the short value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    default short getShort() throws UnsupportedOperationException {
        return getNumber().shortValue();
    }

    /**
     * Get the int value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    default int getInteger() throws UnsupportedOperationException {
        return getNumber().intValue();
    }

    /**
     * Get the long value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    default long getLong() throws UnsupportedOperationException {
        return getNumber().longValue();
    }

    /**
     * Get the float value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    default float getFloat() throws UnsupportedOperationException {
        return getNumber().floatValue();
    }

    /**
     * Get the double value of the element
     *
     * @return the number element
     * @throws UnsupportedOperationException if the element
     * is not a number
     */
    default double getDouble() throws UnsupportedOperationException {
        return getNumber().doubleValue();
    }
}
