package es.karmadev.api.core;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Default source runtime
 */
public final class DefaultRuntime implements SourceRuntime {

    private final Path file;
    private final Collection<Class<?>> routes = new ArrayList<>();

    /**
     * Initialize the default runtime
     *
     * @param source the source owning this runtime
     * @throws IllegalStateException if the runtime fails to initialize
     */
    public DefaultRuntime(final APISource source) throws IllegalStateException {
        URL location = getURL(source);

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

    @NotNull
    private static URL getURL(final APISource source) {
        Class<?> clazz = source.getClass();
        ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain == null) throw new IllegalStateException("Cannot initialize source runtime because the protected domain is null");

        CodeSource code = domain.getCodeSource();
        if (code == null) throw new IllegalStateException("Cannot initialize source runtime because the code source is null");

        URL location = code.getLocation();
        if (location == null) throw new IllegalStateException("Cannot initialize source runtime because the code location is null");
        return location;
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

    /**
     * Get the file of the class
     *
     * @param clazz the class
     * @return the class file
     * @throws IllegalStateException if the file fails to be obtained
     */
    @Override
    public Path getFileFrom(final Class<?> clazz) throws IllegalStateException {
        ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain == null) throw new IllegalStateException("Cannot initialize source runtime because the protected domain is null");

        CodeSource code = domain.getCodeSource();
        if (code == null) throw new IllegalStateException("Cannot initialize source runtime because the code source is null");

        URL location = code.getLocation();
        if (location == null) throw new IllegalStateException("Cannot initialize source runtime because the code location is null");

        try {
            URI uri = location.toURI();
            return Paths.get(uri);
        } catch (URISyntaxException ex) {
            ExceptionCollector.catchException(SourceRuntime.class, ex);
        }

        return null;
    }

    /**
     * Get the source runtime classes
     *
     * @return the runtime classes
     */
    @Override
    public Collection<Class<?>> getClasses() {
        if (!routes.isEmpty()) return Collections.unmodifiableCollection(routes);

        if (file != null) {
            try(ZipFile zip = new ZipFile(file.toFile())) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                do {
                    ZipEntry entry = entries.nextElement();
                    if (entry == null || entry.isDirectory()) continue; //Just in case

                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        try {
                            String clazzPath = name.substring(0, name.length() - 6)
                                    .replaceAll("/", ".")
                                    .replaceAll("\\\\", ".");
                            Class<?> clazz = Class.forName(clazzPath);

                            routes.add(clazz);
                        } catch (ClassNotFoundException ignored) {}
                    }
                } while (entries.hasMoreElements());

            } catch (IOException ex) {
                routes.clear();
            }
        }

        return Collections.unmodifiableCollection(routes);
    }

    /**
     * Return if the caller can execute the specified method
     *
     * @param caller the caller
     * @param method the method to run
     * @return if the caller can execute the specified
     * method
     */
    @Override
    public boolean canExecute(final File caller, final Method method) {
        return true;
    }
}
