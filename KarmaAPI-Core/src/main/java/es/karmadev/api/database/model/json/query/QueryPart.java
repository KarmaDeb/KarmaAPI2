package es.karmadev.api.database.model.json.query;

import java.util.Map;

/**
 * Represents a query part
 */
public interface QueryPart {

    /**
     * Get the part content
     *
     * @return the part content
     */
    String getContent();

    /**
     * Get if the part is or
     *
     * @return if the part is an OR
     * operand
     */
    boolean isOr();

    /**
     * Get if the part is and
     *
     * @return if the part is an AND
     * operand
     */
    boolean isAnd();

    /**
     * Get if the part is a single
     *
     * @return if the part is single
     */
    boolean isSingle();

    /**
     * Test the part
     *
     * @param tests the values to test with
     * @return if the test was successful
     */
    boolean test(Map<String, GlobalTypeValue<?>> tests);
}
