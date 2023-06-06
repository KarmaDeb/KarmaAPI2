package es.karmadev.api.core.source.exception;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;

import java.util.Arrays;

/**
 * AlreadyRegisteredException is thrown when a
 * source that has been already registered or
 * another source with the same name is already
 * registered
 */
public class AlreadyRegisteredException extends Exception {

    /**
     * Initialize the exception
     *
     * @param source the source that has been
     *               tried to register
     */
    public AlreadyRegisteredException(final APISource source) {
        super("Cannot register source " + source.name() + " [" + Arrays.asList(source.authors()) + "] because it is or another source with the same name is already registered");
    }
}
