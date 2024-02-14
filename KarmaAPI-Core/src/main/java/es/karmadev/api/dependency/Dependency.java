package es.karmadev.api.dependency;

import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.web.url.URLUtilities;
import es.karmadev.api.web.url.domain.WebDomain;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;

/**
 * Represents a dependency
 */
public final class Dependency {

    private final JsonObject object;

    /**
     * Load a dependency
     *
     * @param object the dependency data
     */
    public Dependency(final JsonObject object) {
        this.object = object;
    }

    /**
     * Get the dependency ID
     *
     * @return the dependency ID
     */
    public String getId() {
        return object.getChild("id").asString();
    }

    /**
     * Get the dependency name. The name is not
     * used for nothing but as a "pretty" representation
     * for the dependency
     *
     * @return the dependency name
     */
    public String getName() {
        return object.getChild("name").asString();
    }

    /**
     * Get if the dependency has value
     * is the same as the provided hash
     *
     * @param hash the hash
     * @return if the hash matches
     */
    public boolean hashMatches(final String hash) {
        String current = object.getChild("hash").asString();
        if (current == null) return ObjectUtils.isNullOrEmpty(hash);

        return current.equals(hash);
    }

    /**
     * Get if the dependency is required
     * by the current platform.
     *
     * @return if the dependency is required because
     * of the platform
     */
    public boolean platformSupported() {
        String required = object.getChild("platform").asString();
        if (required == null || required.equals("*")) return true;
        /*
        * wildcard, means always required, regardless of platform
        */

        try {
            Class.forName(required);
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
        return false;
    }

    /**
     * Get a valid download URL for
     * the dependency
     *
     * @return a download URL
     */
    @Nullable
    public URL getDownloadURL() {
        JsonInstance instance = object.getChild("download");
        if (!instance.isArrayType()) return null;

        JsonArray array = instance.asArray();
        for (JsonInstance element : array) {
            String value = element.asString();
            if (value == null) continue;

            URL url = URLUtilities.fromString(value);
            if (url == null) continue;

            if (domainExists(url)) return url;
        }

        return null;
    }

    /**
     * Get the dependency relocations
     *
     * @return the relocations
     */
    public Map<String, String> getRelocations() {
        Map<String, String> relocations = new HashMap<>();

        JsonInstance instance = object.getChild("relocations");
        if (!instance.isArrayType()) return relocations;

        JsonArray array = instance.asArray();
        if (array.isEmpty()) return relocations;

        for (JsonInstance element : array) {
            if (!element.isObjectType()) continue;
            JsonObject obj = element.asObject();

            String from = obj.getChild("from").asString();
            String to = obj.getChild("to").asString();

            relocations.put(from, to);
        }

        return relocations;
    }

    /**
     * Get the dependency priority when
     * loading it. This doesn't affect
     * downloading, as all dependencies are
     * first loaded, the all loaded
     *
     * @return the dependency load priority
     */
    public int getPriority() {
        return object.getChild("priority").asInteger();
    }

    /**
     * Validate if a URL domain exists. Sometimes,
     * the web server will simply deny a HEAD request
     * to a route which is not domain.com, so
     * this method extracts that part from the full URL
     * and makes the request
     *
     * @param url the url
     * @return if the domain responds with an expected
     * request
     */
    private boolean domainExists(final URL url) {
        WebDomain domain = URLUtilities.getDomain(url);
        if (domain == null) return false;

        String reqURL = domain.build();
        URL domainURL = URLUtilities.fromString(reqURL);

        if (domainURL == null) return false;
        return URLUtilities.exists(domainURL);
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
