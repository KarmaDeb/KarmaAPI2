package es.karmadev.api.database;

/**
 * KarmaAPI database engine
 */
public interface DatabaseEngine {

    /**
     * Get if the engine is protected
     *
     * @return if the engine is protected
     */
    default boolean isProtected() { return false; }

    /**
     * Get the engine name
     *
     * @return the engine name
     */
    String getName();

    /**
     * Grab a connection from the engine
     * connection pool (if any)
     *
     * @param name the connection name
     * @return a database connection
     */
    DatabaseConnection grabConnection(final String name);
}
