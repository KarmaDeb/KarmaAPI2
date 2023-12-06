package es.karmadev.api.netty.message;

import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.file.serializer.SerializeCompressor;
import es.karmadev.api.file.util.ByteUtils;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.netty.message.nat.Messages;
import es.karmadev.api.netty.message.table.DataTable;
import es.karmadev.api.netty.message.table.DataTypes;
import es.karmadev.api.netty.message.table.entry.TableEntry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents a message builder
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class MessageBuilder {

    private final ByteBuf buf = Unpooled.buffer(0);
    private final DataTable table = new DataTable();

    private int currentIndex = 0;

    /**
     * Write a set of data
     *
     * @param data the data to write
     * @return the message builder
     */

    public MessageBuilder write(final byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        compressAndWrite(DataTypes.BYTE, buffer.array());
        return this;
    }

    /**
     * Write a text
     *
     * @param utf the text to write
     * @return the message builder
     */
    public MessageBuilder writeUTF(final String utf) {
        byte[] utfData = utf.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(utfData);

        compressAndWrite(DataTypes.UTF, buffer.array());
        return this;
    }

    /**
     * Write a short number
     *
     * @param number the number to write
     * @return the message builder
     */
    public MessageBuilder writeInt16(final short number) {
        byte[] numberData = ByteBuffer.allocate(2).putShort(number).array();

        compressAndWrite(DataTypes.INT16, numberData);
        return this;
    }

    /**
     * Write an int number
     *
     * @param number the number to write
     * @return the message builder
     */
    public MessageBuilder writeInt32(final int number) {
        byte[] numberData = ByteBuffer.allocate(4).putInt(number).array();

        compressAndWrite(DataTypes.INT32, numberData);
        return this;
    }

    /**
     * Write a long number
     *
     * @param number the number to write
     * @return the message builder
     */
    public MessageBuilder writeInt64(final long number) {
        byte[] numberData = ByteBuffer.allocate(8).putLong(number).array();

        compressAndWrite(DataTypes.INT64, numberData);
        return this;
    }

    /**
     * Write a float number
     *
     * @param number the number to write
     * @return the message builder
     */
    public MessageBuilder writeFloat32(final float number) {
        byte[] numberData = ByteBuffer.allocate(4).putFloat(number).array();

        compressAndWrite(DataTypes.FLOAT32, numberData);
        return this;
    }

    /**
     * Write a double number
     *
     * @param number the number to write
     * @return the message builder
     */
    public MessageBuilder writeFloat64(final double number) {
        byte[] numberData = ByteBuffer.allocate(8).putDouble(number).array();

        compressAndWrite(DataTypes.FLOAT64, numberData);
        return this;
    }

    /**
     * Write a boolean
     *
     * @param b the boolean
     * @return the message builder
     */
    public MessageBuilder writeBoolean(final boolean b) {
        byte value = (byte) (b ? 1 : 0);

        buf.writeBoolean(b);
        table.addEntry(DataTypes.BOOLEAN, currentIndex, currentIndex + 1);
        currentIndex++;

        return this;
    }

    /**
     * Write a json
     *
     * @param json the json
     * @return the message builder
     */
    public MessageBuilder writeJson(final JsonInstance json) {
        String raw = json.toString(false);
        byte[] jsonBytes = raw.getBytes(StandardCharsets.UTF_8);

        compressAndWrite(DataTypes.JSON, jsonBytes);
        return this;
    }

    /**
     * Write the compressed data
     * version of the data. Compressed data
     * is nothing but the array without null
     * bytes (zeroes).
     * @param data the data to write
     */
    private void compressAndWrite(final DataTypes type, final byte[] data) {
        if (data.length == 0) return;
        int lastValidIndex = data.length;
        for (int i = 0; i < data.length; i++) {
            byte bt = data[i];
            if (bt != 0) {
                lastValidIndex = i;
                break;
            }
        }

        byte[] clean = Arrays.copyOfRange(data, lastValidIndex, data.length);
        if (clean.length == 0) return;

        int targetIndex = currentIndex + clean.length;

        buf.writeBytes(clean);
        table.addEntry(type, currentIndex, targetIndex);
        currentIndex += clean.length;
    }

    /**
     * Build the message
     *
     * @param message the message
     * @return the message data
     * @throws IOException if there's a problem while
     * compressing data
     */
    public BaseMessage build(final Messages message) throws IOException {
        return build(message.getId());
    }

    /**
     * Build the message
     *
     * @param messageId the message id
     * @return the message data
     * @throws IOException if there's a problem while
     * compressing data
     */
    public BaseMessage build(final long messageId) throws IOException {
        byte[] table = this.table.wrap();
        byte[] tableLength = ByteBuffer.allocate(4).putInt(table.length).array();

        byte[] result = Arrays.copyOf(buf.array(), buf.readableBytes());

        byte[] baseResult = new byte[table.length + 4 + result.length];
        System.arraycopy(tableLength, 0, baseResult, 0, tableLength.length);
        System.arraycopy(table, 0, baseResult, 4, table.length);
        System.arraycopy(result, 0, baseResult, table.length + 4, result.length);

        int length = baseResult.length;
        byte[] rsLength = ByteBuffer.allocate(4).putInt(length).array();
        byte[] compressed = ByteUtils.compress(baseResult, SerializeCompressor.ZLIB);

        byte[] finalResult = new byte[compressed.length + 4];
        System.arraycopy(rsLength, 0, finalResult, 0, rsLength.length);
        System.arraycopy(compressed, 0, finalResult, 4, compressed.length);

        return new OutMessage(messageId, finalResult, result, this.table);
    }

    /**
     * Insert the other message before the
     * message. This will merge the messages
     * in the order provided by this method.
     * Please note a merge will be only be successful
     * if both the message and the other message
     * are of the same type ({@link BaseMessage#getId()})
     *
     * @param other the messages which data will
     *              be the first available
     * @param message the message which data
     *                will be last available
     * @return the modified message builder
     * @throws IOException if the message fails to build
     */
    public static BaseMessage insertBefore(final BaseMessage message, final @NonNull BaseMessage... other) throws IOException {
        BaseMessage clone2 = message.clone();
        if (clone2 == null) {
            MessageBuilder mb = new MessageBuilder();
            long id = -1;
            boolean f = true;
            for (BaseMessage bm : other) {
                if (!f) {
                    if (id != bm.getId()) continue;
                } else {
                    id = bm.getId();
                    f = false;
                }

                write(bm.clone(), mb);
            }

            return mb.build(id);
        }

        MessageBuilder instance = new MessageBuilder();
        for (BaseMessage bm : other) {
            BaseMessage clone = bm .clone();
            if (clone.getId() != message.getId()) continue;

            write(clone, instance);
        }

        write(clone2, instance);
        return instance.build(message.getId());
    }

    /**
     * Modify an existent message
     *
     * @param message the message to modify
     * @return the modified message
     */
    public static MessageBuilder modify(final BaseMessage message) {
        BaseMessage clone = message.clone();
        if (clone == null) return new MessageBuilder();

        MessageBuilder builder = new MessageBuilder();
        write(clone, builder);

        return builder;
    }

    /**
     * Write a message into the specified message builder
     *
     * @param message the message to read from
     * @param target the target message builder
     */
    private static void write(final BaseMessage message, final MessageBuilder target) {
        byte[] data;
        String utf;
        Short int16;
        Integer int32;
        Long int64;
        Float float32;
        Double float64;
        Boolean bool;
        JsonInstance json;

        //Clone byte arrays
        while ((data = message.getBytes()) != null) target.write(data);

        //Clone strings
        while ((utf = message.getUTF()) != null) target.writeUTF(utf);

        //Clone shorts
        while ((int16 = message.getInt16()) != null) target.writeInt16(int16);

        //Clone integers
        while ((int32 = message.getInt32()) != null) target.writeInt32(int32);

        //Clone longs
        while ((int64 = message.getInt64()) != null) target.writeInt64(int64);

        //Clone floats
        while ((float32 = message.getFloat32()) != null) target.writeFloat32(float32);

        //Clone doubles
        while ((float64 = message.getFloat64()) != null) target.writeFloat64(float64);

        //Clone booleans
        while ((bool = message.getBoolean()) != null) target.writeBoolean(bool);

        //Clone JSONs
        while ((json = message.getJson()) != null) target.writeJson(json);
    }
}
