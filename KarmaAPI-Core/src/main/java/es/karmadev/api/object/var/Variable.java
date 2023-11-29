package es.karmadev.api.object.var;

import es.karmadev.api.object.var.reference.NotNullReference;

import java.util.function.Supplier;

/**
 * Represents a variable.
 * A variable is an object which you cannot
 * set directly once created, instead, you
 * modify a reference to that object.
 *
 * @param <T> the variable type
 */
public interface Variable<T> {

    /**
     * Create a variable for an element
     * which can be null
     *
     * @param value the element
     * @return the element variable
     * @param <T> the variable type
     */
    static <T> Variable<T> nullable(final T value) {
        return new DefaultVariable<>(value);
    }

    /**
     * Create a variable for an element
     * which should not be null
     *
     * @param value the element
     * @return the element variable
     * @param <T> the variable type
     */
    static <T> Variable<T> notNull(final T value) {
        return new DefaultVariable<>(new NotNullReference<>(value));
    }

    /**
     * Get the variable
     *
     * @return the variable
     */
    T get();

    /**
     * Get the variable, or get
     * the default value if the
     * current variable value is null
     *
     * @param def the default value
     * @return the variable
     */
    default T get(T def) {
        return get(() -> def);
    }

    /**
     * Get the variable, or get
     * the one supplied by the supplier
     *
     * @param def the default value
     * @return the variable
     */
    T get(Supplier<T> def);

    /**
     * Get the variable, and set
     * the new value
     *
     * @param value the new value
     * @return the variable
     */
    default T getAndSet(final T value) {
        return getAndSet(() -> value);
    }

    /**
     * Get the variable, and set
     * the new value
     *
     * @param value the new value
     * @return the variable
     */
    T getAndSet(final Supplier<T> value);

    /**
     * Get the variable, or set the
     * value if the variable is null
     *
     * @param value the new value
     * @return the variable
     */
    default T getOrSet(final T value) {
        return getAndSet(() -> value);
    }

    /**
     * Get the variable, or set the
     * value if the variable is null
     *
     * @param value the new value
     * @return the variable
     */
    T getOrSet(final Supplier<T> value);

    /**
     * Get the reference
     *
     * @return the reference
     */
    Reference<T> getReference();
}
