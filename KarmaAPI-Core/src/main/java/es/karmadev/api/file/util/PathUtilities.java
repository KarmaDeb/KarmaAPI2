package es.karmadev.api.file.util;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.file.yaml.handler.ResourceLoader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * KarmaAPI path utilities
 */
@SuppressWarnings("unused")
public class PathUtilities {

    /**
     * Get a path size
     *
     * @param path the path
     * @return the path size
     */
    public static long getSize(final Path path) {
        if (Files.isDirectory(path)) {
            AtomicLong initialSize = new AtomicLong(0);
            try (Stream<Path> files = Files.list(path)) {
                files.forEachOrdered((file) -> {
                    if (Files.isDirectory(file)) {
                        long size = getSize(file);
                        initialSize.addAndGet(size);
                    } else {
                        try {
                            initialSize.addAndGet(Files.size(file));
                        } catch (IOException ex) {
                            ExceptionCollector.catchException(PathUtilities.class, ex);
                        }
                    }
                });

                return initialSize.get();
            } catch (IOException ex) {
                ExceptionCollector.catchException(PathUtilities.class, ex);
            }
        } else {
            try {
                return Files.size(path);
            } catch (IOException ex) {
                ExceptionCollector.catchException(PathUtilities.class, ex);
            }
        }

        return 0;
    }

    /**
     * Create a path, without any
     * result
     *
     * @return if the path was able to be created
     * @param path the path to create
     */
    public static boolean createPath(final Path path) {
        Path targetPath = path;
        if (!targetPath.isAbsolute()) {
            targetPath = targetPath.toAbsolutePath();
        }

        if (Files.exists(targetPath)) return true;

        Path parent = targetPath.getParent();
        if (createDirectory(parent)) {
            try {
                Files.createFile(targetPath);
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
     * Get the {@link Path} path string
     *
     * @param path the path
     * @return the {@link Path} path string
     */
    public static String pathString(final Path path) {
        return pathString(path, '/');
    }

    /**
     * Get the {@link Path} path string
     *
     * @param path the path
     * @param directorySpacer the path string directory separator
     * @return the {@link Path} path string
     */
    public static String pathString(final Path path, final char directorySpacer) {
        if (Character.isSpaceChar(directorySpacer))
            return path.toAbsolutePath().toString().replaceAll("%20", " ");

        return path.toAbsolutePath().toString().replaceAll("%20", " ").replace(File.separatorChar, directorySpacer);
    }

    /**
     * Get the path extension
     *
     * @param path the path
     * @return the path extension
     */
    public static String getExtension(final @NotNull Path path) {
        if (Files.isDirectory(path)) return "dir";
        String name = path.getFileName().toString();
        return getExtension(name);
    }

    /**
     * Get the path extension
     *
     * @param name the path name
     * @return the path extension
     */
    public static String getExtension(final @NotNull String name) {
        if (name.contains(".")) {
            String[] nameData = name.split("\\.");
            return nameData[nameData.length - 1];
        }

        return "";
    }

    /**
     * Get the path name
     *
     * @param path the path
     * @return the path name
     */
    public static String getName(final Path path) {
        return getName(path, false);
    }

    /**
     * Get the path name
     *
     * @param path the path
     * @param extension include extension in the file name
     * @return the path name
     */
    public static String getName(final Path path, final boolean extension) {
        if (Files.isDirectory(path)) {
            return path.getFileName().toString();
        }

        String pathExtension = getExtension(path);
        String name = path.getFileName().toString();

        return (extension ? name : name.replace("." + pathExtension, ""));
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
        if (createPath(path)) {
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
