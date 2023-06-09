package es.karmadev.api.object;

/**
 * Runtime modifier object
 */
public interface RuntimeModifier<T> {

    /**
     * Modify the object
     *
     * @param original the original object
     * @return the modified object
     */
    T modify(final T original);
}
