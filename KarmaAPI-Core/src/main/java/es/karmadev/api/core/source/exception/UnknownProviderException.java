package es.karmadev.api.core.source.exception;

import es.karmadev.api.core.source.KarmaSource;

/**
 * This exception is thrown when a call to
 * {@link es.karmadev.api.core.source.SourceManager#getProvider(Class)} or {@link es.karmadev.api.core.source.SourceManager#getProvider(String)} is made
 * with a source name or class that is not registered as
 * provider
 */
public class UnknownProviderException extends Exception {

    /**
     * Initialize the exception
     *
     * @param name the source name
     */
    public UnknownProviderException(final String name) {
        super("Cannot retrieve karma source by name " + name + " because it's not registered as provider");
    }

    /**
     * Initialize the exception
     *
     * @param clazz the source class
     */
    public UnknownProviderException(final Class<? extends KarmaSource> clazz) {
        super("Cannot retrieve karma source by class " + clazz.getCanonicalName() + " because it's not registered as provider");
    }
}
