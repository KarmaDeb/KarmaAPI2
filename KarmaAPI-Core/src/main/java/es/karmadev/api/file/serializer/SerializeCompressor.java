package es.karmadev.api.file.serializer;

/**
 * Valid serialized compressors
 */
public enum SerializeCompressor {
    /**
     * Zlib, old but available always
     */
    ZLIB,
    /**
     * Modern, faster and higher compression ratio
     */
    ZSTD,
    /**
     * Modern and faster, but doesn't have a very big compression ratio
     */
    LZ4
}
