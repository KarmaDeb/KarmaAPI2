package es.karmadev.api.database.exception;

import es.karmadev.api.database.DatabaseEngine;

/**
 * KarmaAPI exception
 *
 * This exception is thrown when a database
 * engine is tried to be installed using a
 * name of a protected engine
 */
public class ProtectedEngineException extends Exception {

    /**
     * Initialize the exception
     *
     * @param registered the registered engine
     * @param engine the engine that is trying to be installed
     */
    public ProtectedEngineException(final DatabaseEngine registered, final DatabaseEngine engine) {
        super("Cannot overwrite engine " + engine.getName() + " (" + registered + ") with " + engine + " because it's protected");
    }
}
