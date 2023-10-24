package es.karmadev.api.core.source;

import es.karmadev.api.core.DefaultRuntime;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.file.util.NamedStream;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.task.TaskScheduler;
import es.karmadev.api.schedule.task.scheduler.BalancedScheduler;
import es.karmadev.api.strings.StringFilter;
import es.karmadev.api.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Karma source
 */
@SuppressWarnings("unused")
public abstract class KarmaSource implements APISource {

    protected final String name;
    protected final Version version;
    protected final String description;
    protected final String[] authors;
    protected final SourceRuntime runtime = new DefaultRuntime(this);

    protected Path directory;
    protected SourceLogger console;

    private final Map<String, TaskScheduler> sourceSchedulers = new ConcurrentHashMap<>();

    /**
     * Initialize the source
     *
     * @param name the source name
     * @param version the source version
     * @param description the source description
     * @param authors the source authors
     */
    public KarmaSource(final String name, final Version version, final String description, final String... authors) {
        this(name, version, description, authors, null, null);
    }

    /**
     * Initialize the source
     *
     * @param name the source name
     * @param version the source version
     * @param description the source description
     * @param authors the source authors
     * @param dir the source working directory
     * @param logger the source logger
     */
    public KarmaSource(final String name, final Version version, final String description, final String[] authors, final Path dir, final SourceLogger logger) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.authors = authors;
        directory = (dir != null ? dir : Paths.get("./" + name));
        console = (logger != null ? logger : LogManager.getLogger(this));

        //Runtime.getRuntime().addShutdownHook(new Thread(this::kill));
        sourceSchedulers.put("async", new BalancedScheduler(100, this, 10, 1));
    }

    /**
     * Start the source
     */
    public abstract void start();

    /**
     * Kill the source
     */
    public abstract void kill();

    /**
     * Get the source task scheduler.
     * This will never return null, as if the scheduler does
     * not exist, a new one will be created
     *
     * @param name the scheduler name
     * @return the task scheduler
     */
    @Override
    public final @NotNull TaskScheduler scheduler(final String name) {
        return createScheduler(name);
    }

    /**
     * Create a new scheduler
     *
     * @param name the scheduler name
     * @return the created scheduler
     */
    public TaskScheduler createScheduler(final String name) {
        return createScheduler(name, 100, 1, 100);
    }

    /**
     * Create a new scheduler
     *
     * @param name the scheduler name
     * @param capacity the scheduler capacity
     * @return the created scheduler
     */
    public TaskScheduler createScheduler(final String name, final int capacity) {
        return createScheduler(name, capacity, 10, 1);
    }

    /**
     * Create a scheduler
     *
     * @param name the scheduler name
     * @param capacity the scheduler capacity
     * @param simultaneous the scheduler simultaneous tasks
     * @return the created scheduler
     */
    public TaskScheduler createScheduler(final String name, final int capacity, final int simultaneous) {
        return createScheduler(name, capacity, simultaneous, 1);
    }

    /**
     * Create a scheduler for this source
     *
     * @param name the scheduler name
     * @param capacity the scheduler capacity
     * @param simultaneous the scheduler simultaneous tasks
     * @param perClass the scheduler simultaneous tasks per class
     * @return the created scheduler
     */
    public TaskScheduler createScheduler(final String name, final int capacity, final int simultaneous, final int perClass) {
        return sourceSchedulers.computeIfAbsent(name.toLowerCase(), (s) -> new BalancedScheduler(capacity, this, simultaneous, perClass));
    }

    /**
     * Get the source name
     *
     * @return the source name
     */
    @Override
    public final @NotNull String sourceName() {
        return name;
    }

    /**
     * Get the source version
     *
     * @return the source version
     */
    @Override
    public final @NotNull Version sourceVersion() {
        return version;
    }

    /**
     * Get the source description
     *
     * @return the source description
     */
    @Override
    public final @NotNull String sourceDescription() {
        return description;
    }

    /**
     * Get the source authors
     *
     * @return the source authors
     */
    @Override
    public final @NotNull String[] sourceAuthors() {
        return authors;
    }

    /**
     * Get the source runtime
     *
     * @return the source runtime
     */
    @Override
    public final @NotNull SourceRuntime runtime() {
        return runtime;
    }

    /**
     * Get the source working directory
     *
     * @return the working directory
     */
    @Override
    public @NotNull Path workingDirectory() {
        return directory;
    }

    /**
     * Navigate to the specified file
     *
     * @param fileName the file name
     * @param route    the file route starting from {@link APISource#workingDirectory()}
     * @return the file
     */
    @Override
    public @NotNull Path navigate(final String fileName, final String... route) {
        if (route.length == 0) return workingDirectory().resolve(fileName);
        Path start = workingDirectory();
        for (String dir : route) start = start.resolve(dir);

        return start.resolve(fileName);
    }

    /**
     * Find a resource inside the source file
     *
     * @param resourceName the resource name
     * @return the resource
     */
    @Override
    public @Nullable NamedStream findResource(final String resourceName) {
        JarFile jarHandle = null;
        NamedStream stream = null;
        try {
            Class<? extends KarmaSource> clazz = getClass();
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
     * @param filter       the resource name filter
     * @return the resources
     */
    @Override
    public @NotNull NamedStream[] findResources(final String resourceName, @Nullable final StringFilter filter) {
        JarFile jarHandle = null;
        List<NamedStream> handles = new ArrayList<>();
        try {
            Class<? extends KarmaSource> clazz = getClass();
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
     * Export a resource into the specified
     * path
     *
     * @param resourceName the resource name
     * @param target       the file to export to
     * @return if the file was able to be export
     */
    @Override
    public boolean export(final String resourceName, final Path target) {
        try (NamedStream single = findResource(resourceName)) {
            if (single != null) {
                return tryExport(single, target);
            }
        } catch (IOException ex) {
            ExceptionCollector.catchException(KarmaSource.class, ex);
        }

        NamedStream[] streams = findResources(resourceName, null);
        if (streams.length == 0) return false; //We export nothing

        int success = 0;
        for (NamedStream stream : streams) {
            try {
                if (tryExport(stream, target)) {
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
     * Get the source console
     *
     * @return the source console
     */
    @Override
    public @NotNull SourceLogger logger() {
        return console;
    }

    private boolean tryExport(final NamedStream stream, final Path directory) {
        String name = stream.getName();
        Path targetFile = directory;
        if (name.contains("/")) {
            String[] data = name.split("/");
            for (String dir : data) {
                //Are we the start route?
                if (!ObjectUtils.isNullOrEmpty(dir)) {
                    targetFile = targetFile.resolve(dir);
                }
            }
        } else {
            targetFile = targetFile.resolve(name);
        }

        String raw = StreamUtils.streamToString(stream);
        return PathUtilities.write(targetFile, raw);
    }
}
