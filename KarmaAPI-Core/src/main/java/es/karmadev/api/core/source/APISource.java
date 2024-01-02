package es.karmadev.api.core.source;

import es.karmadev.api.core.CoreModule;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.file.util.NamedStream;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.task.TaskScheduler;
import es.karmadev.api.strings.StringFilter;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;
import es.karmadev.api.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * KarmaAPI source
 */
public interface APISource {

    /**
     * Get the source identifier
     *
     * @return the source identifier
     */
    @NotNull String identifier();

    /**
     * Get the source name
     *
     * @return the source name
     */
    @NotNull String sourceName();

    /**
     * Get the source version
     *
     * @return the source version
     */
    @NotNull Version sourceVersion();

    /**
     * Get the source description
     *
     * @return the source description
     */
    @NotNull String sourceDescription();

    /**
     * Get the source authors
     *
     * @return the source authors
     */
    @NotNull String[] sourceAuthors();

    /**
     * Get the source update URI
     *
     * @return the source update URI
     */
    @Nullable URI sourceUpdateURI();

    /**
     * Get the source runtime
     *
     * @return the source runtime
     */
    @NotNull SourceRuntime runtime();

    /**
     * Get the source placeholder engine.
     * This will never return null, as if the engine does
     * not exist, a new one will be created
     *
     * @param name the placeholder engine name
     * @return the source placeholder engine
     */
    @NotNull PlaceholderEngine placeholderEngine(final String name);

    /**
     * Get the source task scheduler.
     * This will never return null, as if the scheduler does
     * not exist, a new one will be created
     *
     * @param name the scheduler name
     * @return the task scheduler
     */
    @NotNull TaskScheduler scheduler(final String name);

    /**
     * Get the source working directory
     *
     * @return the working directory
     */
    @NotNull Path workingDirectory();

    /**
     * Navigate to the specified file
     *
     * @param fileName the file name
     * @param route the file route starting from {@link APISource#workingDirectory()}
     * @return the file
     */
    @NotNull Path navigate(final String fileName, final String... route);

    /**
     * Find a resource inside the source file
     *
     * @param resourceName the resource name
     * @return the resource
     */
    default @Nullable NamedStream findResource(final String resourceName) {
        JarFile jarHandle = null;
        NamedStream stream = null;
        try {
            Class<? extends APISource> clazz = getClass();
            ProtectionDomain domain = clazz.getProtectionDomain();
            if (domain == null) return null;

            CodeSource source = domain.getCodeSource();
            if (source == null) return null;

            URL location = source.getLocation();
            if (location == null) return null;

            String filePath = location.getFile().replaceAll("%20", " ");
            File file = new File(filePath);

            jarHandle = new JarFile(file);

            JarEntry entry = jarHandle.getJarEntry(resourceName);
            if (entry == null || entry.isDirectory()) return null;

            InputStream streamHandle = jarHandle.getInputStream(entry);
            stream = NamedStream.newStream(entry.getName(), StreamUtils.clone(streamHandle, true));
        } catch (IOException ex) {
            ExceptionCollector.catchException(KarmaSource.class, ex);
        } finally {
            if (jarHandle != null) {
                try {
                    jarHandle.close();
                } catch (IOException ignored) {}
            }
        }

        return stream;
    }

    /**
     * Get all the resources inside the source file folder
     *
     * @param resourceName the resources directory name
     * @param filter the resource name filter
     * @return the resources
     */
    default @NotNull NamedStream[] findResources(final String resourceName, final @Nullable StringFilter filter) {
        JarFile jarHandle = null;
        List<NamedStream> handles = new ArrayList<>();
        try {
            Class<? extends APISource> clazz = getClass();
            ProtectionDomain domain = clazz.getProtectionDomain();
            if (domain == null) return null;

            CodeSource source = domain.getCodeSource();
            if (source == null) return null;

            URL location = source.getLocation();
            if (location == null) return null;

            String filePath = location.getFile().replaceAll("%20", " ");
            File file = new File(filePath);

            jarHandle = new JarFile(file);
            Enumeration<JarEntry> entries = jarHandle.entries();

            do {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                String name = entry.getName();
                if (filter == null || filter.accept(name)) {
                    try (InputStream stream = jarHandle.getInputStream(entry)) {
                        handles.add(NamedStream.newStream(name, stream));
                    }
                }
            } while (entries.hasMoreElements());
        } catch (IOException ex) {
            ExceptionCollector.catchException(KarmaSource.class, ex);
        } finally {
            if (jarHandle != null) {
                try {
                    jarHandle.close();
                } catch (IOException ignored) {}
            }
        }

        return handles.toArray(new NamedStream[0]);
    }

    /**
     * Get all the resources inside the source file
     *
     * @return all the source resources
     */
    default NamedStream[] findResources() {
        return findResources("", sequence -> {
            if (sequence == null) return false;
            String str = sequence.toString();
            return !str.endsWith(".class") && !str.endsWith(".java"); //Allow everything except class and java files
        });
    }

    /**
     * Export a resource into the specified
     * path
     *
     * @param resourceName the resource name
     * @param target the file to export to
     * @return if the file was able to be export
     */
    default boolean export(final String resourceName, final Path target) {
        try (NamedStream single = findResource(resourceName)) {
            if (single != null) {
                return copyStream(single, target);
            }
        } catch (IOException ex) {
            ExceptionCollector.catchException(KarmaSource.class, ex);
        }

        NamedStream[] streams = findResources(resourceName, (seq) -> resourceName.startsWith(seq.toString()) ||
                resourceName.endsWith(seq.toString()));
        if (streams.length == 0) return false; //We export nothing

        int success = 0;
        for (NamedStream stream : streams) {
            try {
                if (copyStream(stream, target)) {
                    success++;
                }
            } finally {
                try {
                    stream.close();
                } catch (IOException ignored) {}
            }
        }

        return success == streams.length;
    }

    /**
     * Get the source logger.
     * If the source is not ready, an {@link es.karmadev.api.logger.log.UnboundedLogger unbounded logger} will
     * be return, which can only print to console or log if is lately bind to a source
     *
     * @return the logger
     */
    SourceLogger logger();

    /**
     * Get a module by name
     *
     * @param name the module name
     * @return the module
     */
    @Nullable CoreModule getModule(final String name);

    /**
     * Register a module
     *
     * @param module the module to register
     * @return if the module was able to be registered
     */
    boolean registerModule(final CoreModule module);

    /**
     * Load an identifier
     *
     * @param name the identifier name
     */
    void loadIdentifier(final String name);

    /**
     * Load an identifier
     */
    default void loadIdentifier() {
        loadIdentifier("DEFAULT");
    }

    /**
     * Generate and save an identifier
     *
     * @param name the identifier name
     */
    void saveIdentifier(final String name);

    /**
     * Generate and save an identifier
     */
    default void saveIdentifier() {
        saveIdentifier("DEFAULT");
    }

    /**
     * Copy a stream to the specified resource
     * directory
     *
     * @param stream the stream to export
     * @param targetFile the resource directory
     * @return if copy was success
     */
    default boolean copyStream(final NamedStream stream, final Path targetFile) {
        if (Files.isDirectory(targetFile)) {
            String name = stream.getName();
            Path target = targetFile;
            if (name.contains("/")) {
                String[] data = name.split("/");
                for (String dir : data) {
                    //Are we the start route?
                    if (!ObjectUtils.isNullOrEmpty(dir)) {
                        target = targetFile.resolve(dir);
                    }
                }
            } else {
                target = targetFile.resolve(name);
            }

            String raw = StreamUtils.streamToString(stream);
            return PathUtilities.write(target, raw);
        } else {
            return PathUtilities.write(targetFile, StreamUtils.streamToString(stream));
        }
    }
}
