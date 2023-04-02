package es.karmadev.api.file.util;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.file.yaml.handler.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * KarmaAPI path utilities
 */
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
        if (buildPath(parent)) {
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
    public static boolean buildPath(final Path path) {
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

    public static ResourceLoader DEFAULT_LOADER = (name) -> {
        ClassLoader loader = PathUtilities.class.getClassLoader();
        return loader.getResourceAsStream(name);
    };
}
