package es.karmadev.api.strings.placeholder;

/**
 * KarmaAPI placeholder
 *
 * @param <T> the placeholder type
 */
@SuppressWarnings("unused")
public interface Placeholder<T> {

    /**
     * Get the placeholder key
     *
     * @return the placeholder key
     */
    String key();

    /**
     * Set the placeholder value
     *
     * @return the placeholder value
     */
    T value();

    /**
     * Get if this placeholder is
     * protected
     *
     * @return if the placeholder is protected
     */
    boolean isProtected();

    /**
     * Get the same placeholder without
     * protection
     *
     * @return the unprotected placeholder
     */
    Placeholder<T> unprotected();
}
