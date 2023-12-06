package es.karmadev.api.channel.data;

import es.karmadev.api.kson.JsonInstance;
import org.jetbrains.annotations.Nullable;

/**
 * Base message. All messages must
 * be cloneable
 */
public interface BaseMessage extends Cloneable {

    /**
     * Get the message ID
     *
     * @return the message ID
     */
    long getId();

    /**
     * Get the full message
     *
     * @return the message
     */
    byte[] readAll();

    /**
     * Get the next byte array
     * from the message
     *
     * @return the byte array
     */
    @Nullable
    byte[] getBytes();

    /**
     * Get the next UTF
     * from the message
     *
     * @return the UTF
     */
    @Nullable
    String getUTF();

    /**
     * Get the next short
     * from the message
     *
     * @return the short
     */
    @Nullable
    Short getInt16();

    /**
     * Get the next integer
     * from the message
     *
     * @return the integer
     */
    @Nullable
    Integer getInt32();

    /**
     * Get the next long
     * from the message
     *
     * @return the long
     */
    @Nullable
    Long getInt64();

    /**
     * Get the next float
     * from the message
     *
     * @return the float
     */
    @Nullable
    Float getFloat32();

    /**
     * Get the next double
     * from the message
     *
     * @return the double
     */
    @Nullable
    Double getFloat64();

    /**
     * Get the next boolean from
     * the message
     *
     * @return the boolean
     */
    @Nullable
    Boolean getBoolean();

    /**
     * Get the next json data
     * from the message
     *
     * @return the json
     */
    @Nullable
    JsonInstance getJson();

    /**
     * Clone the message
     *
     * @return the cloned message
     */
    BaseMessage clone();
}
