package es.karmadev.api.netty.message.table.entry;

import es.karmadev.api.netty.message.table.DataTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Arrays;

@AllArgsConstructor @Getter
public class TableEntry {

    protected final DataTypes type;
    protected final int origin;
    protected final int destination;

    public byte[] wrap() {
        byte dataType = type.getId();
        byte[] originAlloc = ByteBuffer.allocate(4).putInt(origin).array();
        byte[] destAlloc = ByteBuffer.allocate(4).putInt(destination).array();

        byte[] block = new byte[9];
        block[0] = dataType;
        System.arraycopy(originAlloc, 0, block, 1, 4);
        System.arraycopy(destAlloc, 0, block, 5, 4);

        return block;
    }
}
