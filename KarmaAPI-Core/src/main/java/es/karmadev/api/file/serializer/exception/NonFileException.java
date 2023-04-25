package es.karmadev.api.file.serializer.exception;

import java.nio.file.Path;

/**
 * The non file exception is thrown when an
 * operation that requires a {@link java.io.File file} is
 * a directory but a file is required.
 */
public class NonFileException extends RuntimeException {

    /**
     * Initialize the exception
     *
     * @param file the file
     */
    public NonFileException(final Path file) {
        super("Cannot open " + file + " because it's a directory");
    }
}
