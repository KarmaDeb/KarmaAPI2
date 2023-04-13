package es.karmadev.api.version.checker;

import es.karmadev.api.version.Version;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Version changelog
 */
public class Changelog {

    private final Map<Version, String[]> changelogs = new ConcurrentHashMap<>();

    /**
     * Create a new changelog
     */
    public Changelog() {}

    /**
     * Define the changelog
     *
     * @param version the changelog version
     * @param changelog the changelog
     */
    public void define(final Version version, final String... changelog) {
        changelogs.put(version, changelog);
    }

    /**
     * Get the changelog for the specified version
     *
     * @param version the version to
     *                get changelog for
     * @return the version changelog
     */
    public String[] getFor(final Version version) {
        return changelogs.getOrDefault(version, new String[]{"&cNo changelog for " + version});
    }
}
