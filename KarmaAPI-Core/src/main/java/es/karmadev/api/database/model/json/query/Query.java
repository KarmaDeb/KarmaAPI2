package es.karmadev.api.database.model.json.query;

import java.util.Map;

/**
 * Represents the whole query
 */
public interface Query {

    /**
     * Get the query parts
     *
     * @return the parts
     */
    QueryPart[] getParts();

    /**
     * Get the next query part
     *
     * @return the next query part
     */
    QueryPart next();

    /**
     * Get the previous query part
     *
     * @return the previous query part
     */
    QueryPart previous();

    /**
     * Test the query
     *
     * @param tests the tests
     * @return if the test is successful
     */
    boolean test(final Map<String, GlobalTypeValue<?>> tests);
}
