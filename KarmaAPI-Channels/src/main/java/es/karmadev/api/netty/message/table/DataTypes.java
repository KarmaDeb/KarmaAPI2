package es.karmadev.api.netty.message.table;

import lombok.Getter;

@Getter
public enum DataTypes {
    BYTE((byte) 0),
    UTF((byte) 1),
    INT16((byte) 2),
    INT32((byte) 3),
    INT64((byte) 4),
    FLOAT32((byte) 5),
    FLOAT64((byte) 6),
    BOOLEAN((byte) 7),
    JSON((byte) 8);

    private final byte id;

    DataTypes(final byte id) {
        this.id = id;
    }

    public static DataTypes byId(final byte id) {
        if (id < 0 || id > 6) return null;
        for (DataTypes type : DataTypes.values()) {
            if (type.id == id) return type;
        }

        return null;
    }
}
