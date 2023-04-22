package es.karmadev.api.strings.placeholder;

import java.util.Collection;
import java.util.Optional;

/**
 * KarmaAPI placeholder engine
 */
@SuppressWarnings("unused")
public interface PlaceholderEngine {

    /**
     * Set the placeholder identifier
     *
     * @param identifier the placeholder new identifier
     */
    void setIdentifier(final char identifier);

    /**
     * Protect the placeholder engine
     * against modifications. This won't
     * prevent new placeholders from being
     * added
     */
    void protect();

    /**
     * Register a new placeholder
     *
     * @param placeholder the placeholder to register
     * @throws SecurityException if the placeholder is already registered
     * and this engine is protected
     */
    void register(final Placeholder<?> placeholder) throws SecurityException;

    /**
     * Register a new placeholder
     *
     * @param key the placeholder key
     * @param value the placeholder value
     * @return the created placeholder
     * @param <T> the placeholder type
     * @throws SecurityException if the placeholder is already registered
     * and this engine is protected
     */
    @SuppressWarnings("UnusedReturnValue")
    <T> Placeholder<T> register(final String key, final T value) throws SecurityException;

    /**
     * Unregister a placeholder
     *
     * @param key the placeholder key
     * @return if the placeholder could be unregistered
     */
    boolean unregister(final String key);

    /**
     * Unregister a placeholder
     *
     * @param placeholder the placeholder to remove
     * @return if the placeholder could be unregistered
     */
    boolean unregister(final Placeholder<?> placeholder);

    /**
     * Get a placeholder
     *
     * @param key the placeholder key
     * @return the placeholder
     */
    Optional<Placeholder<?>> get(final String key);

    /**
     * Parse a message
     *
     * @param message the message to parse
     * @return the parsed message
     */
    String parse(final String message);

    /**
     * Parse a message
     *
     * @param message the message to parse
     * @return the parsed message
     */
    Collection<String> parse(final Collection<String> message);

    /**
     * Parse a message
     *
     * @param message the message to parse
     * @return the parsed message
     */
    String[] parse(final String[] message);

    /**
     * Get all the placeholders
     *
     * @return the placeholders
     */
    Collection<Placeholder<?>> getPlaceholders();

    /**
     * Get if the engine is protected
     *
     * @return if the engine is protected
     */
    boolean isProtected();
}
