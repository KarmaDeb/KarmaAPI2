package es.karmadev.api.database;

import es.karmadev.api.database.result.QueryResult;

/**
 * KarmaAPI database connection
 */
public interface DatabaseConnection {

    /**
     * Return if the connection supports
     * queries
     *
     * @return if the connection supports queries
     */
    boolean querySupported();

    /**
     * Execute a query
     *
     * @param query the query to run
     * @return the query
     */
    QueryResult execute(final String query);
}
