package es.karmadev.api.file.util;

import com.github.luben.zstd.Zstd;
import es.karmadev.api.file.serializer.SerializeCompressor;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Byte utilities
 */
public class ByteUtils {

    /**
     * Compress the data
     *
     * @param data the data to compress
     * @param compressor the compressor
     * @return the compressed data
     * @throws IOException if the data fails to compress
     */
    public static byte[] compress(final byte[] data, final SerializeCompressor compressor) throws IOException {
        switch (compressor) {
            case ZLIB:
                Deflater deflater = new Deflater();
                deflater.setLevel(Deflater.BEST_COMPRESSION);
                deflater.setInput(data);
                deflater.finish();
                byte[] buffer = new byte[4096];
                try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
                    while (!deflater.finished()) {
                        int count = deflater.deflate(buffer);
                        outputStream.write(buffer, 0, count);
                    }

                    return outputStream.toByteArray();
                }
            case LZ4:
                LZ4Factory factory = LZ4Factory.fastestInstance();
                LZ4Compressor lz4Compressor = factory.fastCompressor();

                return lz4Compressor.compress(data);
            case ZSTD:
                return Zstd.compress(data, Zstd.maxCompressionLevel());
        }

        return data;
    }

    /**
     * Decompress the data
     *
     * @param data the data to decompress
     * @param length the original length, required for
     *               {@link SerializeCompressor#LZ4 lz4} and {@link SerializeCompressor#ZSTD}.
     *               Can be any value for {@link SerializeCompressor#ZLIB}
     * @param compressor the compressor
     * @return the decompressed data
     * @throws IOException if the data fails to decompress
     * @throws DataFormatException if the data is not compressed
     */
    public static byte[] decompress(final byte[] data, final int length, final SerializeCompressor compressor) throws IOException, DataFormatException {
        switch (compressor) {
            case ZLIB:
                Inflater inflater = new Inflater();
                inflater.setInput(data);
                byte[] buffer = new byte[4096];
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
                    while (!inflater.finished()) {
                        int count = inflater.inflate(buffer);
                        outputStream.write(buffer, 0, count);
                    }

                    return outputStream.toByteArray();
                }
            case LZ4:
                LZ4Factory factory = LZ4Factory.fastestInstance();
                LZ4FastDecompressor decompressor = factory.fastDecompressor();

                return decompressor.decompress(data, length);
            case ZSTD:
            default:
                return Zstd.decompress(data, length);
        }
    }
}
