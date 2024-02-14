package es.karmadev.api.file.serializer.serialized;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdException;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.file.serializer.SerializeCompressor;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.strings.StringUtils;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Serialized file dictionary
 */
@SuppressWarnings("unused")
public class SerializedDictionary implements Serializable, Iterable<SerializedFile> {

    private final SerializedFile[] files;

    /**
     * Initialize the dictionary
     *
     * @param files the files
     */
    public SerializedDictionary(final SerializedFile... files) {
        this.files = files;
    }

    /**
     * Get the files
     *
     * @return the files
     */
    public SerializedFile[] getFiles() {
        return files.clone();
    }

    /**
     * Find a file by its SHA value
     *
     * @param sha the file SHA
     * @return the file
     */
    public SerializedFile[] find(final String sha) {
        if (ObjectUtils.isNullOrEmpty(sha)) return new SerializedFile[0];

        try (Stream<SerializedFile> filtered = Arrays.stream(files).filter((serialized) -> serialized.getSha().equals(sha))) {
            return filtered.toArray(value -> new SerializedFile[0]);
        }
    }

    /**
     * Load a serialized dictionary
     *
     * @param name the dictionary name
     * @return the dictionary
     */
    public static Optional<SerializedDictionary> load(final String name) {
        APISource kore = KarmaKore.INSTANCE();
        SerializedDictionary dictionary = null;

        if (kore != null) {
            Path sizesPath = kore.workingDirectory().resolve("serializer").resolve("dictionary").resolve("data.json");

            JsonObject sizes = JsonObject.newObject("", "");
            if (Files.exists(sizesPath)) {
                sizes = JsonReader.read(PathUtilities.read(sizesPath)).asObject();
            }

            if (sizes.hasChild(name)) {
                JsonObject thisData = sizes.getChild(name).asObject();
                int length = thisData.getChild("size").asInteger();
                SerializeCompressor method = SerializeCompressor.valueOf(thisData.getChild("method").asString());

                Path destination = kore.workingDirectory().resolve("serializer").resolve("dictionary").resolve(name + ".sdc");
                if (Files.exists(destination)) {
                    byte[] decompressed = null;
                    byte[] raw = PathUtilities.readBytes(destination);

                    switch (method) {
                        case ZLIB:
                            Inflater inflater = new Inflater();
                            inflater.setInput(raw);
                            byte[] buffer = new byte[4096];
                            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(raw.length)) {
                                while (!inflater.finished()) {
                                    int count = inflater.inflate(buffer);
                                    outputStream.write(buffer, 0, count);
                                }
                                decompressed = outputStream.toByteArray();
                            } catch (IOException | DataFormatException ex) {
                                ExceptionCollector.catchException(SerializedDictionary.class, ex);
                            }
                            break;
                        case LZ4:
                            try {
                                LZ4Factory factory = LZ4Factory.fastestInstance();
                                LZ4FastDecompressor decompressor = factory.fastDecompressor();

                                decompressed = decompressor.decompress(raw, length);
                            } catch (Throwable ex) {
                                ExceptionCollector.catchException(SerializedDictionary.class, ex);
                            }
                            break;
                        case ZSTD:
                        default:
                            try {
                                decompressed = Zstd.decompress(raw, length);
                            } catch (ZstdException ex) {
                                ExceptionCollector.catchException(SerializedDictionary.class, ex);
                            }
                    }

                    if (decompressed != null) {
                        dictionary = StringUtils.loadAndCast(Base64.getEncoder().encodeToString(decompressed));
                    }
                }
            }


        }

        return Optional.ofNullable(dictionary);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<SerializedFile> iterator() {
        return Arrays.asList(files).iterator();
    }
}
