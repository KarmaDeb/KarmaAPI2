package es.karmadev.api.core.source;

import es.karmadev.api.core.DefaultRuntime;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.console.ConsoleLogger;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.core.version.Version;

import java.nio.file.Path;

/**
 * Karma source
 */
public abstract class KarmaSource {

    protected final String name;
    protected final Version version;
    protected final String description;
    protected final String[] authors;
    protected final SourceRuntime runtime = new DefaultRuntime(this);

    protected Path directory;
    protected ConsoleLogger console;

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
    public KarmaSource(final String name, final Version version, final String description, final String[] authors, final Path dir, final ConsoleLogger logger) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.authors = authors;
        /*directory = (dir != null ? dir : runtime.getFile().getParent().resolve(name));
        console = (logger != null ? logger : LogManager.getLogger(this));*/
        directory = dir;
        console = logger;
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
    public ConsoleLogger getConsole() {
        return console;
    }
}
