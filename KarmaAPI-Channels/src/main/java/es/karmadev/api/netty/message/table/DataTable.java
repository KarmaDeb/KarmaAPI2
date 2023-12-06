package es.karmadev.api.netty.message.table;

import es.karmadev.api.netty.message.table.entry.TableEntry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * Represents a data table. A data table is
 * nothing but a table containing information
 * about an array of bytes which contains information.
 * The visual representation of the table would be this:
 * [
 *  [type=DataTypes.UTF, rangeArrayStart=0, rangeArrayEnd=14],
 *  [type=DataTypes.BYTE, rangeArrayStart=15, rangeArrayEnd=29]
 * ]
 */
public class DataTable implements Cloneable {

    private List<TableEntry> entries = new ArrayList<>();
    private EnumMap<DataTypes, Integer> specificCursor = new EnumMap<>(DataTypes.class);

    private int length = 0;

    /**
     * Create a new data table
     */
    public DataTable() {}

    /**
     * Create and fill a new data table
     *
     * @param defaultEntries the entries
     */
    private DataTable(final List<TableEntry> defaultEntries) {
        this.entries.addAll(defaultEntries);
    }

    /**
     * Add an entry to the table
     *
     * @param type the data type
     * @param position the start position
     * @param end the end position
     */
    public void addEntry(final DataTypes type, final int position, final int end) {
        TableEntry range = new TableEntry(type, position, end);
        entries.add(range);
        length += 9;
    }

    /**
     * Get all the entries from the table
     *
     * @param types the data types
     * @return the data type entries
     */
    public List<TableEntry> getEntries(final DataTypes... types) {
        List<TableEntry> matches = new ArrayList<>();
        for (DataTypes type : types) {
            for (TableEntry entry : entries) {
                if (entry.getType().equals(type)) {
                    matches.add(entry);
                }
            }
        }

        return matches;
    }

    /**
     * Get the next entry
     *
     * @param type the data type
     * @return the next type entry
     */
    public TableEntry getNext(final DataTypes type) {
        int cursor = this.specificCursor.computeIfAbsent(type, (c) -> 0);
        List<TableEntry> matches = getEntries(type);

        if (matches.size() <= cursor) {
            return null;
        }

        TableEntry entry = matches.get(cursor++);
        this.specificCursor.put(type, cursor);

        return entry;
    }

    /**
     * Wrap the table into a byte
     * array
     *
     * @return the wrapped table
     */
    public byte[] wrap() {
        byte[] result = new byte[length];

        int writeIndex = 0;
        for (TableEntry entry : entries) {
            byte[] wrap = entry.wrap();
            for (byte b : wrap) {
                result[writeIndex++] = b;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("DataTable@").append(hashCode()).append("\n[");

        int index = 0;
        for (TableEntry entry : entries) {
            builder.append("\t{")
                    .append("\n\t\tdataType=")
                    .append(entry.getType().name())
                    .append("\n\t\tfromRange=")
                    .append(entry.getOrigin())
                    .append("\n\t\ttoRange=")
                    .append(entry.getDestination())
                    .append("\n\t\t[@\n\t\t\t")
                    .append(Arrays.toString(entry.wrap()))
                    .append("\n\t\t]\n\t}");

            if (++index != entries.size()) {
                builder.append(",");
            }
            builder.append("\n");
        }

        return builder.append("]").toString();
    }

    /**
     * Unwrap a table
     *
     * @param table the table
     * @return the unwrapped table
     */
    public static DataTable unwrap(final byte[] table) {
        if (table.length % 9 != 0) return new DataTable();

        List<TableEntry> entries = new ArrayList<>();
        for (int i = 0; i < table.length; i += 9) {
            byte dataType = table[i];
            ByteBuffer originAllocator = ByteBuffer.allocate(4);
            ByteBuffer destAllocator = ByteBuffer.allocate(4);

            resolveInt(table, i + 1, originAllocator);
            resolveInt(table, i + 5, destAllocator);

            TableEntry entry = new TableEntry(
                    DataTypes.byId(dataType),
                    originAllocator.getInt(),
                    destAllocator.getInt()
            );
            entries.add(entry);
        }

        return new DataTable(entries);
    }

    private static void resolveInt(final byte[] array, final int from, final ByteBuffer target) {
        target.clear();
        target.put(array, from, 4);
        target.flip();
    }

    @Override
    public DataTable clone() {
        DataTable table = null;
        try {
            table = (DataTable) super.clone();
            table.length = length;
            table.entries = new ArrayList<>(table.entries);
            table.specificCursor = new EnumMap<>(table.specificCursor);
        } catch (CloneNotSupportedException ignored) {}

        return table;
    }
}
