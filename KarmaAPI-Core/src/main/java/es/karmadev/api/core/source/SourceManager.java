package es.karmadev.api.core.source;

import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.core.source.exception.UnknownProviderException;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * KarmaAPI source manager
 */
public final class SourceManager {

    private final static Set<KarmaSource> sources = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Initialize the source managers
     * @throws IOException always
     */
    private SourceManager() throws IOException {
        throw new IOException("Cannot initialize class SourceManager");
    }

    /**
     * Register the source
     *
     * @param source the source
     * @throws AlreadyRegisteredException if the source is already registered
     */
    public static void register(final KarmaSource source) throws AlreadyRegisteredException {
        if (sources.stream().noneMatch(compare(source))) {
            sources.add(source);
            return;
        }

        throw new AlreadyRegisteredException(source);
    }

    /**
     * Get a provider by its class
     *
     * @param clazz the provider class
     * @return the provider
     * @param <A> the provider type
     * @throws UnknownProviderException if no provider from that class
     * has been registered
     */
    @SuppressWarnings("unchecked")
    public static <A extends KarmaSource> A getProvider(final Class<A> clazz) throws UnknownProviderException {
        if (sources.stream().anyMatch(compare(clazz.getCanonicalName()))) {
            Optional<KarmaSource> source = sources.stream().filter(compare(clazz.getCanonicalName())).findAny();
            if (source.isPresent()) {
                return (A) source.get();
            }
        }

        throw new UnknownProviderException(clazz);
    }

    /**
     * Get a provider by its name
     *
     * @param name the provider name
     * @return the provider
     * @throws UnknownProviderException if no provider with that name is
     * registered
     */
    public static KarmaSource getProvider(final String name) throws UnknownProviderException {
        if (sources.stream().anyMatch(compare(name))) {
            return sources.stream().filter(compare(name)).findFirst().orElse(null);
        }

        throw new UnknownProviderException(name);
    }

    private static Predicate<KarmaSource> compare(final KarmaSource source) {
        String name = source.getName();
        String clazz = source.getClass().getCanonicalName();

        return registered -> {
            String registeredName = registered.getName();
            String registeredClazz = registered.getClass().getCanonicalName();

            return registeredName.equals(name) || registeredClazz.equals(clazz);
        };
    }

    private static Predicate<KarmaSource> compare(final String name) {
        return registered -> {
            String registeredName = registered.getName();
            String registeredClazz = registered.getClass().getCanonicalName();

            return registeredName.equals(name) || registeredClazz.equals(name);
        };
    }
}
