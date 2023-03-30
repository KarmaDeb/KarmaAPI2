package es.karmadev.api.core.source;

import es.karmadev.api.logger.console.ConsoleLogger;
import es.karmadev.api.core.version.Version;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

/**
 * KarmaAPI source loader
 */
public class SourceLoader {

    /**
     * Prepare a source
     *
     * @param clazz the source class
     * @param name the source name
     * @param version the source version
     * @param description the source description
     * @param authors the source authors
     * @return the created source
     *
     * @throws NoSuchMethodException if the class doesn't contain a source constructor
     * @throws InvocationTargetException if the class doesn't allow the constructor
     * @throws InstantiationException if the constructor doesn't allow the instantiation
     * @throws IllegalAccessException if the constructor is not accessible by this module
     */
    public static KarmaSource prepareSource(final Class<? extends KarmaSource> clazz, final String name, final Version version, final String description, final String[] authors)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        return prepareSource(clazz, name, version, description, authors, null, null);
    }

    /**
     * Prepare a source
     *
     * @param clazz the source class
     * @param name the source name
     * @param version the source version
     * @param description the source description
     * @param authors the source authors
     * @param directory the source directory
     * @return the created source
     *
     * @throws NoSuchMethodException if the class doesn't contain a source constructor
     * @throws InvocationTargetException if the class doesn't allow the constructor
     * @throws InstantiationException if the constructor doesn't allow the instantiation
     * @throws IllegalAccessException if the constructor is not accessible by this module
     */
    public static KarmaSource prepareSource(final Class<? extends KarmaSource> clazz, final String name, final Version version, final String description, final String[] authors, final Path directory)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return prepareSource(clazz, name, version, description, authors, directory, null);
    }

    /**
     * Prepare a source
     *
     * @param clazz the source class
     * @param name the source name
     * @param version the source version
     * @param description the source description
     * @param authors the source authors
     * @param directory the source working directory
     * @param logger the source logger
     * @return the created source
     *
     * @throws NoSuchMethodException if the class doesn't contain a source constructor
     * @throws InvocationTargetException if the class doesn't allow the constructor
     * @throws InstantiationException if the constructor doesn't allow the instantiation
     * @throws IllegalAccessException if the constructor is not accessible by this module
     */
    public static KarmaSource prepareSource(final Class<? extends KarmaSource> clazz, final String name, final Version version, final String description, final String[] authors, final Path directory, final ConsoleLogger logger)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Constructor<? extends KarmaSource> constructor = clazz.getConstructor(String.class, Version.class, String.class, String[].class, Path.class, ConsoleLogger.class);
        return constructor.newInstance(name, version, description, authors, directory, logger);
    }
}
