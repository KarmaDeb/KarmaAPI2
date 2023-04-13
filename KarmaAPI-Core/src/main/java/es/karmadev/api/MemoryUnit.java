package es.karmadev.api;

/**
 * Most common used memory units
 */
@SuppressWarnings("unused")
public enum MemoryUnit {
    /**
     * Bits unit
     */
    BITS,
    /**
     * Bytes unit
     */
    BYTES,
    /**
     * KiloBytes unit
     */
    KILOBYTES,
    /**
     * MegaBytes unit
     */
    MEGABYTES,
    /**
     * GigaByte units
     */
    GIGABYTES;

    private final static int BYTES_PER_KB = 1024;
    private final static int BITS_PER_BYTE = 8;

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
                        return value / BITS_PER_BYTE;
                    case KILOBYTES:
                        return (value / BITS_PER_BYTE) / BYTES_PER_KB;
                    case MEGABYTES:
                        return ((value / BITS_PER_BYTE) / BYTES_PER_KB) / BYTES_PER_KB;
                    case GIGABYTES:
                    default:
                        return (((value / BITS_PER_BYTE) / BYTES_PER_KB) / BYTES_PER_KB) / BYTES_PER_KB;
                }
            case BYTES:
                switch (to) {
                    case BITS:
                        return value * BITS_PER_BYTE;
                    case KILOBYTES:
                        return value / BYTES_PER_KB;
                    case MEGABYTES:
                        return (value / BYTES_PER_KB) / BYTES_PER_KB;
                    case GIGABYTES:
                    default:
                        return ((value / BYTES_PER_KB) / BYTES_PER_KB) / BYTES_PER_KB;
                }
            case KILOBYTES:
                switch (to) {
                    case BITS:
                        return (value * BYTES_PER_KB) * BITS_PER_BYTE;
                    case BYTES:
                        return value * BYTES_PER_KB;
                    case MEGABYTES:
                        return value / BYTES_PER_KB;
                    case GIGABYTES:
                    default:
                        return (value / BYTES_PER_KB) / BYTES_PER_KB;
                }
            case MEGABYTES:
                switch (to) {
                    case BITS:
                        return ((value * BYTES_PER_KB) * BYTES_PER_KB) * BITS_PER_BYTE;
                    case BYTES:
                        return (value * BYTES_PER_KB) * BYTES_PER_KB;
                    case KILOBYTES:
                        return value * BYTES_PER_KB;
                    case GIGABYTES:
                    default:
                        return value / BYTES_PER_KB;
                }
            case GIGABYTES:
            default:
                switch (to) {
                    case BITS:
                        return (((value * BYTES_PER_KB) * BYTES_PER_KB) * BYTES_PER_KB) * BITS_PER_BYTE;
                    case BYTES:
                        return ((value * BYTES_PER_KB) * BYTES_PER_KB) * BYTES_PER_KB;
                    case KILOBYTES:
                        return (value * BYTES_PER_KB) * BYTES_PER_KB;
                    case MEGABYTES:
                    default:
                        return value * BYTES_PER_KB;
                }
        }
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
}
