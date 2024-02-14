package es.karmadev.api.file.serializer;

import com.github.luben.zstd.Zstd;
import es.karmadev.api.MemoryUnit;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.file.serializer.serialized.SerializedDictionary;
import es.karmadev.api.file.serializer.serialized.SerializedFile;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.schedule.task.completable.BiTaskCompletor;
import es.karmadev.api.schedule.task.completable.late.BiLateTask;
import es.karmadev.api.strings.StringUtils;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.Deflater;

/**
 * File serializer
 */
@SuppressWarnings("unused")
public class FileSerializer {

    private final Path file;

    /**
     * Initialize the file serializer
     *
     * @param file the file
     */
    public FileSerializer(final File file) {
        this.file = file.toPath();
    }

    /**
     * Initialize the file serializer
     *
     * @param file the file
     */
    public FileSerializer(final Path file) {
        this.file = file;
    }

    /**
     * Get if the serializer will
     * serialize a directory
     *
     * @return if the serializer serializes
     * directories
     */
    public boolean isDirectory() {
        return Files.isDirectory(file);
    }

    /**
     * Serialize the directory or file
     *
     * @param name the dictionary name
     * @return the number of serialized files
     */
    public BiTaskCompletor<Path, Integer> serialize(final String name) {
        return serialize(name, SerializeCompressor.ZSTD);
    }

    /**
     * Serialize the directory or file
     *
     * @param name the dictionary name
     * @param compressor the compressor to use
     * @return the number of serialized files
     */
    public BiTaskCompletor<Path, Integer> serialize(final String name, final SerializeCompressor compressor) {
        BiTaskCompletor<Path, Integer> task = new BiLateTask<>();
        APISource kore = KarmaKore.INSTANCE();

        if (kore != null) {
            Path destination = kore.workingDirectory().resolve("serializer").resolve("dictionary").resolve(name + ".sdc");
            PathUtilities.createPath(destination);

            kore.scheduler("async").schedule(() -> {
                try {
                    if (Files.isDirectory(file)) {
                        kore.logger().send(LogLevel.DEBUG, "Scanning directory. This might take a while");

                        long dirLength = PathUtilities.getSize(file);

                        MemoryUnit highest = MemoryUnit.highestAvailable(dirLength, MemoryUnit.BYTES);
                        long conversed = MemoryUnit.BYTES.to(dirLength, highest);

                        kore.logger().send(LogLevel.DEBUG, "Preparing to serialize {0}{1} of data", conversed, highest.getName());

                        Collection<SerializedFile> serializedFiles = serializeDirectory(file, "/");
                        SerializedDictionary dictionary = new SerializedDictionary(serializedFiles.toArray(new SerializedFile[0]));

                        String serializedBase64 = StringUtils.serialize(dictionary);
                        byte[] raw = Base64.getDecoder().decode(serializedBase64);

                        MemoryUnit hg = MemoryUnit.highestAvailable(raw.length, MemoryUnit.BYTES);
                        kore.logger().send(LogLevel.DEBUG, "Serialized data: {0}{1}", MemoryUnit.BYTES.to(raw.length, hg), hg.getName());

                        byte[] compressed;
                        switch (compressor) {
                            case ZLIB:
                                kore.logger().send(LogLevel.DEBUG, "Using ZLIB compression");

                                Deflater deflater = new Deflater();
                                deflater.setLevel(Deflater.BEST_COMPRESSION);
                                deflater.setInput(raw);
                                deflater.finish();
                                byte[] buffer = new byte[4096];
                                try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream(raw.length)) {
                                    long zlibStart = System.currentTimeMillis();
                                    while (!deflater.finished()) {
                                        int count = deflater.deflate(buffer);
                                        outputStream.write(buffer, 0, count);
                                    }
                                    long zlibEnd = System.currentTimeMillis();
                                    compressed = outputStream.toByteArray();

                                    MemoryUnit compressedUnit = MemoryUnit.highestAvailable(compressed.length, MemoryUnit.BYTES);
                                    long compressedConversed = MemoryUnit.BYTES.to(compressed.length, highest);

                                    kore.logger().send(LogLevel.DEBUG, "ZLIB took {0} ms to compress {1} {2} into {3} {4}",
                                            (zlibEnd - zlibStart),
                                            conversed,
                                            highest.getName(),
                                            compressedConversed,
                                            compressedUnit.getName());
                                }
                                break;
                            case LZ4:
                                kore.logger().send(LogLevel.DEBUG, "Using LZ4 compression");
                                LZ4Factory factory = LZ4Factory.fastestInstance();
                                LZ4Compressor lz4Compressor = factory.fastCompressor();

                                long lz4start = System.currentTimeMillis();
                                compressed = lz4Compressor.compress(raw);
                                long lz4end = System.currentTimeMillis();

                                MemoryUnit compressedUnit = MemoryUnit.highestAvailable(compressed.length, MemoryUnit.BYTES);
                                long compressedConversed = MemoryUnit.BYTES.to(compressed.length, highest);

                                kore.logger().send("LZ4 took {0} ms to compress {1} {2} into {3} {4}",
                                        (lz4end - lz4start),
                                        conversed,
                                        highest.getName(),
                                        compressedConversed,
                                        compressedUnit.getName());
                                break;
                            case ZSTD:
                            default:
                                kore.logger().send(LogLevel.DEBUG, "Using ZSTD compression");

                                long zdtStart = System.currentTimeMillis();
                                compressed = Zstd.compress(raw, Zstd.maxCompressionLevel());
                                long zdtEnd = System.currentTimeMillis();

                                MemoryUnit compressedZSTDUnit = MemoryUnit.highestAvailable(compressed.length, MemoryUnit.BYTES);
                                long compressedZSTDConversed = MemoryUnit.BYTES.to(compressed.length, highest);

                                kore.logger().send("ZSTD took {0} ms to compress {1} {2} into {3} {4}",
                                        (zdtEnd - zdtStart),
                                        conversed,
                                        highest.getName(),
                                        compressedZSTDConversed,
                                        compressedZSTDUnit.getName());
                                break;
                        }

                        JsonObject sizes = JsonObject.newObject("", "");
                        JsonObject thisData = JsonObject.newObject("", name);
                        Path sizesPath = kore.workingDirectory().resolve("serializer").resolve("dictionary").resolve("data.json");

                        if (Files.exists(sizesPath)) {
                            sizes = JsonReader.read(PathUtilities.read(sizesPath)).asObject();
                        }

                        thisData.put("size", raw.length);
                        thisData.put("method", compressor.name());
                        sizes.put(name, thisData);
                        String json = sizes.toString();
                        PathUtilities.write(sizesPath, json);

                        PathUtilities.write(destination, compressed);
                        task.complete(destination, serializedFiles.size());
                    } else {
                        SerializedFile serialized = new SerializedFile("/", file);
                        SerializedDictionary dictionary = new SerializedDictionary(serialized);

                        String serializedBase64 = StringUtils.serialize(dictionary);
                        byte[] raw = Base64.getDecoder().decode(serializedBase64);

                        PathUtilities.write(destination, raw);
                        task.complete(destination, 1);
                    }
                } catch (NoSuchAlgorithmException | IOException ex) {
                    task.complete(destination, 0, ex);
                }
            });
        } else {
            task.complete(null, 0);
        }

        return task;
    }

    /**
     * Serialize a directory
     *
     * @param directory the directory to serialize
     * @param path the path to serialize
     * @return the serialized files
     */
    private Collection<SerializedFile> serializeDirectory(final Path directory, final String path) {
        List<SerializedFile> serializedFiles = new ArrayList<>();
        APISource kore = KarmaKore.INSTANCE();
        assert kore != null;

        try (Stream<Path> files = Files.list(directory)) {
            files.forEachOrdered((file) -> {
                if (Files.isDirectory(file)) {
                    serializedFiles.addAll(serializeDirectory(file, path + file.getFileName().toString() + "/"));
                } else {
                    try {
                        kore.logger().send(LogLevel.DEBUG, "Serializing file {0}", path + file.getFileName().toString());

                        SerializedFile serialized = new SerializedFile(path, file);
                        serializedFiles.add(serialized);

                        kore.logger().send(LogLevel.DEBUG, "Serialized file {0}", path + file.getFileName().toString());
                    } catch (NoSuchAlgorithmException | IOException ex) {
                        //ex.printStackTrace();
                        ExceptionCollector.catchException(FileSerializer.class, ex);
                    }
                }
            });
        } catch (IOException ex) {
            //ex.printStackTrace();
            ExceptionCollector.catchException(FileSerializer.class, ex);
        }

        return serializedFiles;
    }
}
