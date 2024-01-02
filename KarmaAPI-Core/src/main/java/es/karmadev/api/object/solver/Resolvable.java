package es.karmadev.api.object.solver;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a resolvable element.
 * A resolvable object is nothing but an
 * object which resolves to another object, which
 * is considered as "the working" object in this
 * context
 */
public interface Resolvable {

    /**
     * Get if the resolvable element resolves to
     * an object of type
     *
     * @param type the type
     * @return if the object resolves to this
     * type
     */
    boolean resolvesTo(final Class<?> type);

    /**
     * resolve the element
     *
     * @param type the element type
     * @return the resolved element
     * @param <T> the resolved type
     */
    @Nullable
    <T> T resolveTo(final Class<T> type);
}
