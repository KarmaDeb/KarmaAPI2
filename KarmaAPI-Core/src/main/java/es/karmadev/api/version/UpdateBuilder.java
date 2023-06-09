package es.karmadev.api.version;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.*;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.version.checker.VersionChecker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Update builder, to create update files
 */
@SuppressWarnings("unused")
public class UpdateBuilder {

    private final KarmaSource source;
    private final List<String> changelog = new CopyOnWriteArrayList<>();
    private final List<URL> updateURLs = new CopyOnWriteArrayList<>();

    /**
     * Initialize the update builder
     *
     * @param source the source to build a version for
     */
    public UpdateBuilder(final KarmaSource source) {
        this.source = source;
    }

    /**
     * Set the update changelog
     *
     * @param changelog the changelog
     */
    public void setChangelog(final String[] changelog) {
        List<String> nonNull = new ArrayList<>();
        for (String line : changelog) {
            if (line != null) nonNull.add(line);
        }

        this.changelog.clear();
        this.changelog.addAll(nonNull);
    }

    /**
     * Set the update changelog
     *
     * @param changelog the changelog
     */
    public void setChangelog(final Collection<String> changelog) {
        List<String> nonNull = new ArrayList<>();
        for (String line : changelog) {
            if (line != null) nonNull.add(line);
        }

        this.changelog.clear();
        this.changelog.addAll(nonNull);
    }

    /**
     * Add the specified lines to the
     * changelog
     *
     * @param lines the lines to add
     */
    public void addChangelog(final String... lines) {
        List<String> nonNull = new ArrayList<>();
        for (String line : lines) {
            if (line != null) nonNull.add(line);
        }

        this.changelog.addAll(nonNull);
    }

    /**
     * Remove the specified line from the
     * changelog
     *
     * @param line the lines to remove
     */
    public void removeChangelog(final String line) {
        this.changelog.remove(line);
    }

    /**
     * Remove the specified line from the
     * changelog
     *
     * @param index the line index to remove
     */
    public void removeChangelog(final int index) {
        this.changelog.remove(index);
    }

    /**
     * Set the update urls
     *
     * @param urls the urls
     */
    public void setUpdateURLs(final URL[] urls) {
        List<URL> nonNull = new ArrayList<>();
        for (URL url : urls) {
            if (url != null) nonNull.add(url);
        }

        this.updateURLs.clear();
        this.updateURLs.addAll(nonNull);
    }

    /**
     * Set the update urls
     *
     * @param urls the urls
     */
    public void setUpdateURLs(final Collection<URL> urls) {
        List<URL> nonNull = new ArrayList<>();
        for (URL url : urls) {
            if (url != null) nonNull.add(url);
        }

        this.updateURLs.clear();
        this.updateURLs.addAll(nonNull);
    }

    /**
     * Add the specified update urls to the
     * update url list
     *
     * @param urls the urls to add
     */
    public void addUpdateURLs(final URL... urls) {
        List<URL> nonNull = new ArrayList<>();
        for (URL url : urls) {
            if (url != null) nonNull.add(url);
        }

        this.updateURLs.addAll(nonNull);
    }

    /**
     * remove the specified update url from the
     * update url list
     *
     * @param url the url to remove
     */
    public void removeUpdateURL(final URL url) {
        this.updateURLs.remove(url);
    }

    /**
     * remove the specified update url from the
     * update url list
     *
     * @param index the url index to remove
     */
    public void removeUpdateURL(final int index) {
        this.updateURLs.remove(index);
    }

    /**
     * Build the update file
     *
     * @return if the update file could be written
     */
    public boolean build() {
        return build(source.workingDirectory().resolve("updates").resolve(source.name() + ".json"));
    }

    /**
     * Build the update file
     *
     * @param destination the file to read from and write
     *                    to
     * @return if the update file could be written
     */
    public boolean build(final Path destination) {
        try (InputStream stream = VersionChecker.class.getResourceAsStream("/update.schema.json")) {
            if (stream != null) {
                JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(stream);
                JsonSchema schema = factory.getJsonSchema(node);

                String raw = PathUtilities.read(destination);
                JsonNode existing;
                if (!raw.isEmpty()) {
                    existing = mapper.readTree(raw);
                    if (!schema.validate(existing).isSuccess()) {
                        return false;
                    }
                }

                Gson gson = new GsonBuilder().create();
                JsonObject json = null;
                boolean found = false;

                try {
                    json = gson.fromJson(raw, JsonObject.class);
                } catch (JsonSyntaxException ignored) {}
                if (json == null) json = new JsonObject();

                Version sourceVersion = source.version();

                JsonObject versionObject = new JsonObject();
                JsonArray versions = new JsonArray();
                if (json.has("versions")) {
                    versions = json.getAsJsonArray("versions");
                    JsonObject parentObject = null;
                    for (JsonElement element : versions) {
                        parentObject = element.getAsJsonObject();
                        String rawVersion = parentObject.keySet().toArray(new String[0])[0];

                        JsonObject info = parentObject.get(rawVersion).getAsJsonObject();

                        String build = info.get("build").getAsString();

                        Version instance = Version.parse(rawVersion, build);
                        if (sourceVersion.equals(instance)) {
                            versionObject = info;
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        versions.remove(parentObject);
                    }
                }

                versionObject.addProperty("build", String.valueOf(sourceVersion.getBuild()));
                if (updateURLs.isEmpty()) {
                    versionObject.remove("update");
                } else {
                    JsonArray updateElement = new JsonArray();
                    updateURLs.forEach((url) -> updateElement.add(url.toString()));

                    versionObject.add("update", updateElement);
                }

                JsonArray changelogElement = new JsonArray();
                changelog.forEach((line) -> changelogElement.add(line.replace('&', '§')));

                versionObject.add("changelog", changelogElement);
                JsonObject parentObj = new JsonObject();
                parentObj.add(sourceVersion.getMayor() + "." + sourceVersion.getMinor() + "." + sourceVersion.getPatch(), versionObject);

                JsonArray newVersions = new JsonArray();
                newVersions.add(parentObj);
                for (JsonElement child : versions) newVersions.add(child);

                json.add("versions", newVersions);

                String jsonString;
                try {
                    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
                    jsonString = prettyGson.toJson(json);
                } catch (Throwable ex) {
                    Gson simpleGson = new GsonBuilder().create();
                    jsonString = simpleGson.toJson(json);
                }

                PathUtilities.createPath(destination);
                return PathUtilities.write(destination, jsonString);
            }
        } catch (IOException | ProcessingException ex) {
            ExceptionCollector.catchException(UpdateBuilder.class, ex);
        }

        return false;
    }
}
