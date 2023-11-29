package es.karmadev.api.object.var;

/**
 * Represents a reference of
 * an object
 * @param <T> the object type
 */
public interface Reference<T> {

    /**
     * Get the reference value
     *
     * @return the value
     */
    T get();

    /**
     * Get if the reference is set
     *
     * @return if the reference is
     * set
     */
    boolean isSet();

    /**
     * Set the reference
     *
     * @param object the new value
     */
    void set(final T object);
}
