package es.karmadev.api.file.serializer;

import com.github.luben.zstd.Zstd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import es.karmadev.api.MemoryUnit;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.file.serializer.serialized.SerializedDictionary;
import es.karmadev.api.file.serializer.serialized.SerializedFile;
import es.karmadev.api.file.util.PathUtilities;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
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
        KarmaKore kore = KarmaKore.INSTANCE();

        if (kore != null) {
            Path destination = kore.getWorkingDirectory().resolve("serializer").resolve("dictionary").resolve(name + ".sdc");
            PathUtilities.createPath(destination);

            kore.createScheduler("async").schedule(() -> {
                try {
                    if (Files.isDirectory(file)) {
                        long dirLength = PathUtilities.getSize(file);

                        MemoryUnit highest = MemoryUnit.highestAvailable(dirLength, MemoryUnit.BYTES);
                        long conversed = MemoryUnit.BYTES.to(dirLength, highest);

                        Collection<SerializedFile> serializedFiles = serializeDirectory(file, "/");
                        SerializedDictionary dictionary = new SerializedDictionary(serializedFiles.toArray(new SerializedFile[0]));

                        String serializedBase64 = StringUtils.serialize(dictionary);
                        byte[] raw = Base64.getDecoder().decode(serializedBase64);
                        byte[] compressed;
                        switch (compressor) {
                            case ZLIB:
                                kore.getConsole().send("Using ZLIB compression", LogLevel.DEBUG);

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

                                    kore.getConsole().send("ZLIB took {0} ms to compress {1} {2} into {3} {4}",
                                            (zlibEnd - zlibStart),
                                            conversed,
                                            highest.getName(),
                                            compressedConversed,
                                            compressedUnit.getName());
                                }
                                break;
                            case LZ4:
                                kore.getConsole().send("Using LZ4 compression", LogLevel.DEBUG);
                                LZ4Factory factory = LZ4Factory.fastestInstance();
                                LZ4Compressor lz4Compressor = factory.fastCompressor();

                                long lz4start = System.currentTimeMillis();
                                compressed = lz4Compressor.compress(raw);
                                long lz4end = System.currentTimeMillis();

                                MemoryUnit compressedUnit = MemoryUnit.highestAvailable(compressed.length, MemoryUnit.BYTES);
                                long compressedConversed = MemoryUnit.BYTES.to(compressed.length, highest);

                                kore.getConsole().send("LZ4 took {0} ms to compress {1} {2} into {3} {4}",
                                        (lz4end - lz4start),
                                        conversed,
                                        highest.getName(),
                                        compressedConversed,
                                        compressedUnit.getName());
                                break;
                            case ZSTD:
                            default:
                                kore.getConsole().send("Using ZSTD compression", LogLevel.DEBUG);

                                long zdtStart = System.currentTimeMillis();
                                compressed = Zstd.compress(raw, Zstd.maxCompressionLevel());
                                long zdtEnd = System.currentTimeMillis();

                                MemoryUnit compressedZSTDUnit = MemoryUnit.highestAvailable(compressed.length, MemoryUnit.BYTES);
                                long compressedZSTDConversed = MemoryUnit.BYTES.to(compressed.length, highest);

                                kore.getConsole().send("ZSTD took {0} ms to compress {1} {2} into {3} {4}",
                                        (zdtEnd - zdtStart),
                                        conversed,
                                        highest.getName(),
                                        compressedZSTDConversed,
                                        compressedZSTDUnit.getName());
                                break;
                        }

                        JsonObject sizes = new JsonObject();
                        JsonObject thisData = new JsonObject();
                        Path sizesPath = kore.getWorkingDirectory().resolve("serializer").resolve("dictionary").resolve("data.json");
                        Gson gson = new GsonBuilder().create();

                        if (Files.exists(sizesPath)) {
                            sizes = gson.fromJson(PathUtilities.read(sizesPath), JsonObject.class);
                        }

                        thisData.addProperty("size", raw.length);
                        thisData.addProperty("method", compressor.name());
                        sizes.add(name, thisData);
                        String json = gson.toJson(sizes);
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
        try (Stream<Path> files = Files.list(directory)) {
            files.forEachOrdered((file) -> {
                if (Files.isDirectory(file)) {
                    serializedFiles.addAll(serializeDirectory(file, path + file.getFileName().toString() + "/"));
                } else {
                    try {
                        SerializedFile serialized = new SerializedFile(path, file);
                        serializedFiles.add(serialized);
                    } catch (NoSuchAlgorithmException | IOException ex) {
                        ExceptionCollector.catchException(FileSerializer.class, ex);
                    }
                }
            });
        } catch (IOException ex) {
            ExceptionCollector.catchException(FileSerializer.class, ex);
        }

        return serializedFiles;
    }
}
