package es.karmadev.api.file.util;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.file.yaml.handler.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * KarmaAPI path utilities
 */
@SuppressWarnings("unused")
public class PathUtilities {

    /**
     * Create a path, without any
     * result
     *
     * @return if the path was able to be created
     * @param path the path to create
     */
    public static boolean createPath(final Path path) {
        if (Files.exists(path)) return true;

        Path parent = path.getParent();
        if (createDirectory(parent)) {
            try {
                Files.createFile(path);
                return true;
            } catch (IOException ex) {
                ExceptionCollector.catchException(PathUtilities.class, ex);
            }
        }

        return false;
    }

    /**
     * Build a path as a directory
     *
     * @param path the path
     * @return if the path was able to be created
     */
    public static boolean createDirectory(final Path path) {
        if (Files.exists(path)) return true;

        try {
            Files.createDirectories(path);
            return true;
        } catch (IOException ex) {
            ExceptionCollector.catchException(PathUtilities.class, ex);
        }

        return false;
    }

    /**
     * Destroy a path
     *
     * @param path the path to destroy
     * @return if the path was able to be destroyed
     */
    public static boolean destroy(final Path path) {
        if (Files.isDirectory(path)) {
            try(Stream<Path> sub = Files.list(path)) {
                for (Path subFile : sub.collect(Collectors.toList())) {
                    if (!destroy(subFile)) return false;
                }

                return Files.deleteIfExists(path);
            } catch (IOException ex) {
                ExceptionCollector.catchException(PathUtilities.class, ex);
            }
        } else {
            try {
                return Files.deleteIfExists(path);
            } catch (IOException ex) {
                ExceptionCollector.catchException(PathUtilities.class, ex);
            }
        }

        return false;
    }

    /**
     * Transform a path into an input stream
     *
     * @param path the path
     * @return the path input stream
     */
    public static InputStream toStream(final Path path) {
        InputStream stream = new ByteArrayInputStream(new byte[0]);

        try {
            stream = Files.newInputStream(path, StandardOpenOption.CREATE);
        } catch (IllegalArgumentException | UnsupportedOperationException | IOException | SecurityException ex) {
            ExceptionCollector.catchException(PathUtilities.class, ex);
        }

        return stream;
    }

    /**
     * Copy from an internal resource to a path
     *
     * @param loader the resource loader
     * @param resource the resource to copy
     * @param path the destination path
     * @return if the resource was able to be copied
     */
    public static boolean copy(final ResourceLoader loader, final String resource, final Path path) {
        try (InputStream stream = loader.loadResource(resource)) {
            if (stream != null) {
                if (!Files.exists(path)) {
                    if (!createPath(path)) return false;
                }

                Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (IOException ex) {
            ExceptionCollector.catchException(PathUtilities.class, ex);
        }

        return false;
    }

    /**
     * Read the path data
     *
     * @param path the path content
     * @return the path contents
     */
    public static byte[] readBytes(final Path path) {
        if (Files.isDirectory(path) || !Files.exists(path)) return new byte[0];
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            ExceptionCollector.catchException(PathUtilities.class, ex);
        }

        return new byte[0];
    }

    /**
     * Read the file
     *
     * @param path the file
     * @return the file content
     */
    public static List<String> readAllLines(final Path path) {
        return readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Read the file
     *
     * @param path the file
     * @param charset the charset to get data as
     * @return the file content
     */
    public static List<String> readAllLines(final Path path, final Charset charset) {
        if (Files.isDirectory(path) || !Files.exists(path)) return new ArrayList<>(0);
        try {
            return Files.readAllLines(path, charset);
        } catch (IOException ex) {
            ExceptionCollector.catchException(PathUtilities.class, ex);
        }

        return new ArrayList<>(0);
    }

    /**
     * Read the file
     *
     * @param path the file
     * @return the file content
     */
    public static String read(final Path path) {
        return read(path, StandardCharsets.UTF_8);
    }

    /**
     * Read the file
     *
     * @param path the file
     * @param charset the charset to get data as
     * @return the file content
     */
    public static String read(final Path path, final Charset charset) {
        return new String(readBytes(path), charset);
    }

    /**
     * Write to the file
     *
     * @param path the file to write to
     * @param data the file data
     * @param options the write options
     * @return if the file could be written
     */
    public static boolean write(final Path path, final byte[] data, OpenOption... options) {
        if (Files.isDirectory(path)) return false;
        if (createDirectory(path)) {
            try {
                Files.write(path, data, options);
                return true;
            } catch (IOException ex) {
                ExceptionCollector.catchException(PathUtilities.class, ex);
            }
        }

        return false;
    }

    /**
     * Write to the file
     *
     * @param path the file to write to
     * @param data the file data
     * @param options the write options
     * @return if the file could be written
     */
    public static boolean write(final Path path, final CharSequence data, final OpenOption... options) {
        return write(path, data, StandardCharsets.UTF_8, options);
    }

    /**
     * Write to the file
     *
     * @param path the file to write to
     * @param data the file data
     * @param charset the data charset
     * @param options the write options
     * @return if the file could be written
     */
    public static boolean write(final Path path, final CharSequence data, final Charset charset, final OpenOption... options) {
        return write(path, data.toString().getBytes(charset), options);
    }

    /**
     * Write to the file
     *
     * @param path the file to write to
     * @param data the file data
     * @param options the write options
     * @return if the file could be written
     */
    public static boolean write(final Path path, final Collection<String> data, final OpenOption... options) {
        return write(path, data, StandardCharsets.UTF_8, options);
    }

    /**
     * Write to the file
     *
     * @param path the file to write to
     * @param data the file data
     * @param charset the data charset
     * @param options the write options
     * @return if the file could be written
     */
    public static boolean write(final Path path, final Collection<String> data, final Charset charset, final OpenOption... options) {
        StringBuilder rawBuilder = new StringBuilder();

        int index = 0;
        for (String line : data) {
            rawBuilder.append(line).append((index == data.size() - 1 ? "" : "\n"));
        }

        return write(path, rawBuilder, charset, options);
    }

    /**
     * Write to the file
     *
     * @param path the file to write to
     * @param data the file data
     * @param options the write options
     * @return if the file could be written
     */
    public static boolean write(final Path path, final String[] data, final OpenOption... options) {
        return write(path, Arrays.asList(data), StandardCharsets.UTF_8, options);
    }

    /**
     * Write to the file
     *
     * @param path the file to write to
     * @param data the file data
     * @param charset the data charset
     * @param options the write options
     * @return if the file could be written
     */
    public static boolean write(final Path path, final String[] data, final Charset charset, final OpenOption... options) {
        return write(path, Arrays.asList(data), charset, options);
    }

    /**
     * The default resource loader used for path utilities
     */
    public static ResourceLoader DEFAULT_LOADER = (name) -> {
        ClassLoader loader = PathUtilities.class.getClassLoader();
        return loader.getResourceAsStream(name);
    };
}
