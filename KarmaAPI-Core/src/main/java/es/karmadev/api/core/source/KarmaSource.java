package es.karmadev.api.core.source;

import es.karmadev.api.core.DefaultRuntime;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.schedule.task.TaskScheduler;
import es.karmadev.api.schedule.task.scheduler.BalancedScheduler;
import es.karmadev.api.version.Version;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Karma source
 */
@SuppressWarnings("unused")
public abstract class KarmaSource {

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
        directory = dir;
        console = (logger != null ? logger : new UnboundedLogger());

        Runtime.getRuntime().addShutdownHook(new Thread(this::kill));
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
    public final String getName() {
        return name;
    }

    /**
     * Get the source version
     *
     * @return the source version
     */
    public final Version getVersion() {
        return version;
    }

    /**
     * Get the source description
     *
     * @return the source description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Get the source authors
     *
     * @return the source authors
     */
    public final String[] getAuthors() {
        return authors;
    }

    /**
     * Get the source update URL
     *
     * @return the source update URL
     */
    @Nullable
    public abstract String updateURL();

    /**
     * Get the source runtime
     *
     * @return the source runtime
     */
    public SourceRuntime getRuntime() {
        return runtime;
    }

    /**
     * Get the source working directory
     *
     * @return the working directory
     */
    public Path getWorkingDirectory() {
        return directory;
    }

    /**
     * Get the source console
     *
     * @return the source console
     */
    public SourceLogger getConsole() {
        return console;
    }
}
