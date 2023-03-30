package es.karmadev.api.core;

import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.runtime.SourceRuntime;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Default source runtime
 */
public final class DefaultRuntime implements SourceRuntime {

    private final Path file;

    /**
     * Initialize the default runtime
     *
     * @param source the source owning this runtime
     * @throws IllegalStateException if the runtime fails to initialize
     */
    public DefaultRuntime(final KarmaSource source) throws IllegalStateException {
        Class<?> clazz = source.getClass();
        ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain == null) throw new IllegalStateException("Cannot initialize source runtime because the protected domain is null");

        CodeSource code = domain.getCodeSource();
        if (code == null) throw new IllegalStateException("Cannot initialize source runtime because the code source is null");

        URL location = code.getLocation();
        if (location == null) throw new IllegalStateException("Cannot initialize source runtime because the code location is null");

        try {
            URI uri = location.toURI();
            try {
                file = Paths.get(uri);
            } catch (IllegalArgumentException | FileSystemNotFoundException | SecurityException ex) {
                throw new IllegalStateException("Cannot initialize source runtime because the system denied it", ex);
            }
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Cannot initialize source runtime because the code location is not a valid URI", ex);
        }
    }

    /**
     * Get the source file
     *
     * @return the source file
     */
    @Override
    public Path getFile() {
        return file;
    }
}
