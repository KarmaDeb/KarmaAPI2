package es.karmadev.api.netty.message;

import es.karmadev.api.channel.data.BaseMessage;
import es.karmadev.api.file.serializer.SerializeCompressor;
import es.karmadev.api.file.util.ByteUtils;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.netty.message.table.DataTable;
import es.karmadev.api.netty.message.table.DataTypes;
import es.karmadev.api.netty.message.table.entry.TableEntry;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;

@Getter
public class DecMessage implements BaseMessage {

    private final long id;
    private final byte[] data;

    private DataTable table;

    public DecMessage(final long id, final byte[] data) throws IOException, DataFormatException {
        this.id = id;
        ByteBuffer allocation = ByteBuffer.allocate(4);
        allocation.clear();
        allocation.put(data, 0, 4);
        allocation.flip();

        int compressedLength = allocation.getInt();
        byte[] compressed = Arrays.copyOfRange(data, 4, data.length);

        byte[] decompressed = ByteUtils.decompress(compressed, compressedLength, SerializeCompressor.ZLIB);

        allocation.clear();
        allocation.put(decompressed, 0, 4);
        allocation.flip();

        int tableLength = allocation.getInt();;
        this.data = split(decompressed, tableLength + 4, decompressed.length);

        byte[] tableData = Arrays.copyOfRange(decompressed, 4, tableLength + 4);
        table = DataTable.unwrap(tableData);
    }

    @Override
    public byte[] readAll() {
        return data.clone();
    }

    @Override
    public byte[] getBytes() {
        TableEntry entry = table.getNext(DataTypes.BYTE);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] array = Arrays.copyOfRange(data, from, to);
        ByteBuffer buf = ByteBuffer.wrap(array);
        buf.flip();

        return buf.array();
    }

    @Override
    public String getUTF() {
        TableEntry entry = table.getNext(DataTypes.UTF);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] string = Arrays.copyOfRange(data, from, to);
        ByteBuffer buf = ByteBuffer.wrap(string);
        buf.flip();

        byte[] array = buf.array();
        int nullByteIndex = array.length;
        for (int i = 0; i < array.length; i++) {
            byte b = array[i];
            if (b == 0) {
                nullByteIndex = i;
                break;
            }
        }

        return new String(Arrays.copyOf(buf.array(), nullByteIndex), StandardCharsets.UTF_8);
    }

    @Override
    public Short getInt16() {
        TableEntry entry = table.getNext(DataTypes.INT16);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] selection = Arrays.copyOfRange(data, from, to);
        byte[] minSelection = new byte[2];
        mapReverse(selection, minSelection);

        ByteBuffer allocation = ByteBuffer.allocate(2);
        allocation.clear();
        allocation.put(minSelection);
        allocation.flip();

        return allocation.getShort();
    }

    @Override
    public Integer getInt32() {
        TableEntry entry = table.getNext(DataTypes.INT32);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] selection = Arrays.copyOfRange(data, from, to);
        byte[] minSelection = new byte[4];
        mapReverse(selection, minSelection);

        ByteBuffer allocation = ByteBuffer.allocate(4);
        allocation.clear();
        allocation.put(minSelection);
        allocation.flip();

        return allocation.getInt();
    }

    @Override
    public Long getInt64() {
        TableEntry entry = table.getNext(DataTypes.INT64);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] selection = Arrays.copyOfRange(data, from, to);
        byte[] minSelection = new byte[8];
        mapReverse(selection, minSelection);

        ByteBuffer allocation = ByteBuffer.allocate(8);
        allocation.clear();
        allocation.put(minSelection);
        allocation.flip();

        return allocation.getLong();
    }

    @Override
    public Float getFloat32() {
        TableEntry entry = table.getNext(DataTypes.FLOAT32);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] selection = Arrays.copyOfRange(data, from, to);
        byte[] minSelection = new byte[4];
        mapReverse(selection, minSelection);

        ByteBuffer allocation = ByteBuffer.allocate(4);
        allocation.clear();
        allocation.put(minSelection);
        allocation.flip();

        return allocation.getFloat();
    }

    @Override
    public Double getFloat64() {
        TableEntry entry = table.getNext(DataTypes.FLOAT64);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] selection = Arrays.copyOfRange(data, from, to);
        byte[] minSelection = new byte[8];
        mapReverse(selection, minSelection);

        ByteBuffer allocation = ByteBuffer.allocate(8);
        allocation.clear();
        allocation.put(minSelection);
        allocation.flip();

        return allocation.getDouble();
    }

    @Override
    public Boolean getBoolean() {
        TableEntry entry = table.getNext(DataTypes.BOOLEAN);
        if (entry == null) return null;

        int from = entry.getOrigin();

        byte rs = data[from];
        return rs == 1;
    }

    /**
     * Get the next json data
     * from the message
     *
     * @return the json
     */
    @Override
    public @Nullable JsonInstance getJson() {
        TableEntry entry = table.getNext(DataTypes.JSON);
        if (entry == null) return null;

        int from = entry.getOrigin();
        int to = entry.getDestination();

        byte[] array = Arrays.copyOfRange(data, from, to);
        ByteBuffer buf = ByteBuffer.wrap(array);
        buf.flip();

        byte[] jsonData = buf.array();
        return JsonReader.parse(jsonData);
    }

    /**
     * Clone the message
     *
     * @return the cloned message
     */
    @Override
    public BaseMessage clone() {
        DecMessage clone = null;
        try {
            clone = (DecMessage) super.clone();
            clone.table = table.clone();
        } catch (CloneNotSupportedException ignored) {}

        return clone;
    }

    private static void mapReverse(final byte[] from, final byte[] to) {
        int rev = from.length - 1;
        if (rev < 0) return;

        for (int i = to.length - 1; i >= 0; i--) {
            if (rev < 0) {
                to[i] = (byte) 0;
                continue;
            }

            to[i] = from[rev--];
        }
    }

    private static byte[] split(final byte[] array, final int from, final int to) {
        int src = Math.max(0, from);
        int max = Math.min(array.length, to);

        byte[] newArray = new byte[max - src];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = array[i + src];
        }

        return newArray;
    }
}
