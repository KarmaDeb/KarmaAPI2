package es.karmadev.api;

import lombok.Getter;

/**
 * Most common used memory units
 */
@Getter
@SuppressWarnings("unused")
public enum MemoryUnit {
    /**
     * Bits unit
     */
    BITS("b"),
    /**
     * Bytes unit
     */
    BYTES("B"),
    /**
     * KiloBytes unit
     */
    KILOBYTES("KB"),
    /**
     * MegaBytes unit
     */
    MEGABYTES("MB"),
    /**
     * GigaByte units
     */
    GIGABYTES("GB");

    private final String name;

    private final static int bytesPerKB = 1024;
    private final static int bitsPerByte = 8;

    /**
     * Initialize the memory unit
     *
     * @param name the unit name
     */
    MemoryUnit(final String name) {
        this.name = name;
    }

    /**
     * Converts from a memory unit to the
     * other memory unit
     *
     * @param from the memory unit to convert from
     * @param to the memory unit to convert to
     * @param value the memory unit value
     * @return the converted memory unit
     */
    private long fromTo(final MemoryUnit from, final MemoryUnit to, final long value) {
        if (from.equals(to)) return value;
        if (value < 0) return 0;

        switch (from) {
            case BITS:
                switch (to) {
                    case BYTES:
                        return value / bitsPerByte;
                    case KILOBYTES:
                        return (value / bitsPerByte) / bytesPerKB;
                    case MEGABYTES:
                        return ((value / bitsPerByte) / bytesPerKB) / bytesPerKB;
                    case GIGABYTES:
                    default:
                        return (((value / bitsPerByte) / bytesPerKB) / bytesPerKB) / bytesPerKB;
                }
            case BYTES:
                switch (to) {
                    case BITS:
                        return value * bitsPerByte;
                    case KILOBYTES:
                        return value / bytesPerKB;
                    case MEGABYTES:
                        return (value / bytesPerKB) / bytesPerKB;
                    case GIGABYTES:
                    default:
                        return ((value / bytesPerKB) / bytesPerKB) / bytesPerKB;
                }
            case KILOBYTES:
                switch (to) {
                    case BITS:
                        return (value * bytesPerKB) * bitsPerByte;
                    case BYTES:
                        return value * bytesPerKB;
                    case MEGABYTES:
                        return value / bytesPerKB;
                    case GIGABYTES:
                    default:
                        return (value / bytesPerKB) / bytesPerKB;
                }
            case MEGABYTES:
                switch (to) {
                    case BITS:
                        return ((value * bytesPerKB) * bytesPerKB) * bitsPerByte;
                    case BYTES:
                        return (value * bytesPerKB) * bytesPerKB;
                    case KILOBYTES:
                        return value * bytesPerKB;
                    case GIGABYTES:
                    default:
                        return value / bytesPerKB;
                }
            case GIGABYTES:
            default:
                switch (to) {
                    case BITS:
                        return (((value * bytesPerKB) * bytesPerKB) * bytesPerKB) * bitsPerByte;
                    case BYTES:
                        return ((value * bytesPerKB) * bytesPerKB) * bytesPerKB;
                    case KILOBYTES:
                        return (value * bytesPerKB) * bytesPerKB;
                    case MEGABYTES:
                    default:
                        return value * bytesPerKB;
                }
        }
    }

    /**
     * Get the highest memory unit available for
     * the specified value
     *
     * @param value the value
     * @return the value highest memory unit
     */
    public static MemoryUnit highestAvailable(final long value, final MemoryUnit start) {
        if (start == MemoryUnit.GIGABYTES) return GIGABYTES;

        long bytes;
        long kilobytes;
        long megabytes;
        long gigabytes;

        switch (start) {
            case BITS:
                bytes = start.toBytes(value);
                kilobytes = start.toKiloBytes(value);
                megabytes = start.toMegaBytes(value);
                gigabytes = start.toGigaBytes(value);

                if (kilobytes > 0) return KILOBYTES;
                if (megabytes > 0) return MEGABYTES;
                if (gigabytes > 0) return GIGABYTES;
                if (bytes > 0) return BYTES;

                return BITS;
            case BYTES:
                kilobytes = start.toKiloBytes(value);
                megabytes = start.toMegaBytes(value);
                gigabytes = start.toGigaBytes(value);

                if (megabytes > 0) return MEGABYTES;
                if (gigabytes > 0) return GIGABYTES;
                if (kilobytes > 0) return KILOBYTES;

                return BYTES;
            case KILOBYTES:
                megabytes = start.toMegaBytes(value);
                gigabytes = start.toGigaBytes(value);

                if (gigabytes > 0) return GIGABYTES;
                if (megabytes > 0) return MEGABYTES;
                return KILOBYTES;
            case MEGABYTES:
                gigabytes = start.toGigaBytes(value);
                return (gigabytes > 0 ? GIGABYTES : MEGABYTES);
        }

        return BITS;
    }

    /**
     * Converts from the current memory
     * unit to bits
     *
     * @param value the unit value
     * @return the converted memory unit
     */
    public long toBits(final long value) {
        return fromTo(this, MemoryUnit.BITS, value);
    }

    /**
     * Converts from the current memory
     * unit to bytes
     *
     * @param value the unit value
     * @return the converted memory unit
     */
    public long toBytes(final long value) {
        return fromTo(this, MemoryUnit.BYTES, value);
    }

    /**
     * Converts from the current memory
     * unit to kilobytes
     *
     * @param value the unit value
     * @return the converted memory unit
     */
    public long toKiloBytes(final long value) {
        return fromTo(this, MemoryUnit.KILOBYTES, value);
    }

    /**
     * Converts from the current memory
     * unit to megabytes
     *
     * @param value the unit value
     * @return the converted memory unit
     */
    public long toMegaBytes(final long value) {
        return fromTo(this, MemoryUnit.MEGABYTES, value);
    }

    /**
     * Converts from the current memory
     * unit to megabytes
     *
     * @param value the unit value
     * @return the converted memory unit
     */
    public long toGigaBytes(final long value) {
        return fromTo(this, MemoryUnit.GIGABYTES, value);
    }

    /**
     * Converts from the current memory
     * unit to the specified one
     *
     * @param value the unit value
     * @param target the unit to convert to
     * @return the converted memory unit
     */
    public long to(final long value, final MemoryUnit target) {
        return fromTo(this, target, value);
    }
}
