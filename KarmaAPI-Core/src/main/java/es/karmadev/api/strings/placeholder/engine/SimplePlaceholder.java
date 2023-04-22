package es.karmadev.api.strings.placeholder.engine;

import es.karmadev.api.strings.placeholder.Placeholder;

/**
 * KarmaAPI simple placeholder
 *
 * @param <T> the placeholder type
 */
@SuppressWarnings("unused")
public class SimplePlaceholder<T> implements Placeholder<T> {

    private final String key;
    private final T value;
    private boolean isProtected = false;

    /**
     * Initialize the placeholder
     *
     * @param key the placeholder key
     * @param value the placeholder value
     */
    public SimplePlaceholder(final String key, final T value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the placeholder as a protected
     * placeholder instance
     *
     * @return the protected placeholder
     */
    public SimplePlaceholder<T> asProtected() {
        SimplePlaceholder<T> clone = new SimplePlaceholder<>(key, value);
        clone.isProtected = true;

        return clone;
    }

    /**
     * Get the placeholder key
     *
     * @return the placeholder key
     */
    @Override
    public String key() {
        return key;
    }

    /**
     * Set the placeholder value
     *
     * @return the placeholder value
     */
    @Override
    public T value() {
        return value;
    }

    /**
     * Get if this placeholder is
     * protected
     *
     * @return if the placeholder is protected
     */
    @Override
    public boolean isProtected() {
        return isProtected;
    }

    /**
     * Get the same placeholder without
     * protection
     *
     * @return the unprotected placeholder
     */
    @Override
    public Placeholder<T> unprotected() {
        return new SimplePlaceholder<>(key, value);
    }
}
