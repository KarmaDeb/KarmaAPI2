package es.karmadev.api.database.model.json.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Simple query
 */
public class SimpleQuery implements Query {

    private final List<QueryPart> parts = new ArrayList<>();
    private int index;

    /**
     * Initialize the simple query
     *
     * @param parts the query parts
     */
    public SimpleQuery(final Collection<QueryPart> parts) {
        this.parts.addAll(parts);
    }


    /**
     * Get the query parts
     *
     * @return the parts
     */
    @Override
    public QueryPart[] getParts() {
        return parts.toArray(new QueryPart[0]).clone();
    }

    /**
     * Get the next query part
     *
     * @return the next query part
     */
    @Override
    public QueryPart next() {
        if (parts.size() > index + 1) {
            index += 1;
            return parts.get(index);
        }

        return null;
    }

    /**
     * Get the previous query part
     *
     * @return the previous query part
     */
    @Override
    public QueryPart previous() {
        if (index - 1 >= 0) {
            index -= 1;
            return parts.get(index);
        }

        return null;
    }

    /**
     * Test the query
     *
     * @param tests the values to test with
     * @return if the test is successful
     */
    @Override
    public boolean test(final Map<String, GlobalTypeValue<?>> tests) {
        QueryPart previous = null;

        boolean success = false;
        boolean jump = false;

        for (int i = 0; i < parts.size(); i++) {
            if (success) break;

            QueryPart query = parts.get(i);
            if (query.isAnd()) {
                if (jump) {
                    continue;
                }

                if (previous != null) {
                    if (!previous.test(tests)) {
                        previous = query;
                        continue;
                    }
                }

                previous = query;
                if (i + 1 < parts.size()) {
                    QueryPart next = parts.get(i + 1);
                    i++;

                    if (!next.isAnd()) {
                        i--; //Go back
                        success = query.test(tests);
                    }

                    while (next.isAnd()) {
                        if (!next.test(tests)) {
                            success = false;
                            jump = true;
                            previous = null;
                            break;
                        } else {
                            if (i + 1 < parts.size()) {
                                next = parts.get(i + 1);
                                i++;
                            }

                            success = true;
                        }

                        previous = next;
                    }

                    if (!success) {
                        i--; //Go back
                    }
                } else {
                    success = query.test(tests);
                }

                continue;
            }

            jump = false;
            if (query.isOr()) {
                previous = query;
                if (query.test(tests)) {
                    if (i + 1 < parts.size()) {
                        QueryPart next = parts.get(i + 1);
                        i++;

                        while (next.isAnd()) {
                            if (!next.test(tests)) {
                                success = false;
                                jump = true;
                                previous = null;
                                break;
                            } else {
                                success = true;
                            }

                            previous = next;
                        }
                    } else {
                        success = true;
                    }
                }

                continue;
            }

            previous = query;
        }

        if (!success && previous != null && parts.size() == 1)
            return previous.test(tests);

        return success;
    }
}
