package es.karmadev.api.core.source;

import es.karmadev.api.core.CoreModule;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.file.util.NamedStream;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.schedule.task.TaskScheduler;
import es.karmadev.api.strings.StringFilter;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;
import es.karmadev.api.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;

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
    @Nullable NamedStream findResource(final String resourceName);

    /**
     * Get all the resources inside the source file folder
     *
     * @param resourceName the resources directory name
     * @param filter the resource name filter
     * @return the resources
     */
    @NotNull NamedStream[] findResources(final String resourceName, final @Nullable StringFilter filter);

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
    boolean export(final String resourceName, final Path target);

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
}
