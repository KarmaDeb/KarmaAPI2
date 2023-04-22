package es.karmadev.api.strings.placeholder.engine;

import es.karmadev.api.strings.placeholder.Placeholder;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * KarmaAPI simple placeholder engine
 */
public class SimpleEngine implements PlaceholderEngine {

    private final Set<Placeholder<?>> placeholders = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private char identifier = '%';
    public char negateCharacter = '\\';
    private boolean isProtected = false;

    /**
     * Initialize the simple placeholder engine
     *
     * @param initialPlaceholders the initial placeholders
     */
    public SimpleEngine(final Placeholder<?>... initialPlaceholders) {
        List<Placeholder<?>> nonNull = new ArrayList<>();
        for (Placeholder<?> placeholder : initialPlaceholders) {
            if (placeholder != null) {
                nonNull.add(placeholder);
            }
        }

        placeholders.addAll(nonNull);
    }

    /**
     * Set the placeholder identifier
     *
     * @param identifier the placeholder new identifier
     */
    @Override
    public void setIdentifier(final char identifier) {
        this.identifier = identifier;
    }

    /**
     * Protect the placeholder engine
     * against modifications. This won't
     * prevent new placeholders from being
     * added
     */
    @Override
    public void protect() {
        isProtected = true;
    }

    /**
     * Register a new placeholder
     *
     * @param placeholder the placeholder to register
     * @throws SecurityException if the placeholder is already registered
     *                           and this engine is protected
     */
    @Override
    public void register(final Placeholder<?> placeholder) throws SecurityException {
        Stream<Placeholder<?>> matchingPlaceholders = placeholders.stream().filter((existing) -> existing.key().equals(placeholder.key()));
        if (matchingPlaceholders.findAny().isPresent() && isProtected) {
            throw new SecurityException("Cannot update existing placeholder at engine " + this + " because it is protected");
        }

        placeholders.removeIf((existing) -> {
            if (existing.key().equals(placeholder.key())) {
                if (existing.isProtected()) {
                    throw new SecurityException("Cannot update existing placeholder (" + existing.key() + ") at engine " + this + " because placeholder is protected");
                }

                return true;
            }

            return false;
        });

        placeholders.add(placeholder);
    }

    /**
     * Register a new placeholder
     *
     * @param key   the placeholder key
     * @param value the placeholder value
     * @return the created placeholder
     * @throws SecurityException if the placeholder is already registered
     *                           and this engine is protected
     */
    @Override
    public <T> Placeholder<T> register(final String key, final T value) throws SecurityException {
        Stream<Placeholder<?>> matchingPlaceholders = placeholders.stream().filter((existing) -> existing.key().equals(key));
        if (matchingPlaceholders.findAny().isPresent() && isProtected) {
            throw new SecurityException("Cannot update existing placeholder at engine " + this + " because it is protected");
        }

        placeholders.removeIf((existing) -> {
            if (existing.key().equals(key)) {
                if (existing.isProtected()) {
                    throw new SecurityException("Cannot update existing placeholder (" + existing.key() + ") at engine " + this + " because placeholder is protected");
                }

                return true;
            }

            return false;
        });
        SimplePlaceholder<T> placeholder = new SimplePlaceholder<>(key, value);
        placeholders.add(placeholder);

        return placeholder;
    }

    /**
     * Unregister a placeholder
     *
     * @param key the placeholder key
     * @return if the placeholder could be unregistered
     */
    @Override
    public boolean unregister(final String key) {
        if (isProtected) return false;

        return placeholders.removeIf((existing) -> {
            if (existing.key().equals(key)) {
                return !existing.isProtected();
            }

            return false;
        });
    }

    /**
     * Unregister a placeholder
     *
     * @param placeholder the placeholder to remove
     * @return if the placeholder could be unregistered
     */
    @Override
    public boolean unregister(final Placeholder<?> placeholder) {
        if (isProtected) return false;

        return placeholders.removeIf((existing) -> {
            if (existing.key().equals(placeholder.key())) {
                return !existing.isProtected();
            }

            return false;
        });
    }

    /**
     * Get a placeholder
     *
     * @param key the placeholder key
     * @return the placeholder
     */
    @Override
    public Optional<Placeholder<?>> get(final String key) {
        return placeholders.stream().filter((existing) -> existing.key().equals(key)).findAny();
    }

    /**
     * Parse a message
     *
     * @param message    the message to parse
     * @return the parsed message
     */
    @Override
    public String parse(final String message) {
        Pattern pattern = Pattern.compile("(" + identifier + ".[^" + identifier + "]*" + identifier + ")|(\\{.[^}]*})");
        Map<String, String> replaces = new HashMap<>();

        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            int pre = Math.max(matcher.start() - 1, 0);
            int start = matcher.start();
            int end = matcher.end();

            if (pre != start && message.charAt(pre) == negateCharacter) {
                replaces.put(message.substring(pre, end), message.substring(start, end));
                continue;
            }

            String name = message.substring(start + 1, end - 1);
            String key = message.substring(start, end);

            Optional<Placeholder<?>> placeholder = get(name);
            placeholder.ifPresent(value -> replaces.put(key, String.valueOf(value.value())));
        }

        String modifiedMessage = message;
        for (String key : replaces.keySet()) {
            modifiedMessage = modifiedMessage.replace(key, replaces.get(key));
        }

        return modifiedMessage;
    }

    /**
     * Parse a message
     *
     * @param messages    the message to parse
     * @return the parsed message
     */
    @Override
    public Collection<String> parse(final Collection<String> messages) {
        List<String> parsed = new ArrayList<>();
        for (String message : messages) parsed.add(parse(message));

        return parsed;
    }

    /**
     * Parse a message
     *
     * @param messages    the message to parse
     * @return the parsed message
     */
    @Override
    public String[] parse(final String[] messages) {
        String[] parsed = new String[messages.length];
        for (int i = 0; i < parsed.length; i++) {
            String message = messages[i];
            parsed[i] = parse(message);
        }

        return parsed;
    }

    /**
     * Get all the placeholders
     *
     * @return the placeholders
     */
    @Override
    public Collection<Placeholder<?>> getPlaceholders() {
        return new ArrayList<>(placeholders);
    }

    /**
     * Get if the engine is protected
     *
     * @return if the engine is protected
     */
    @Override
    public boolean isProtected() {
        return isProtected;
    }
}
