package es.karmadev.api.version.checker;

import com.google.gson.*;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.schedule.task.completable.TaskCompletor;
import es.karmadev.api.schedule.task.completable.late.LateTask;
import es.karmadev.api.version.BuildStatus;
import es.karmadev.api.version.Version;
import es.karmadev.api.web.url.URLUtilities;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Version checker
 */
@SuppressWarnings("unused")
public class VersionChecker {

    private static Schema versionSchema;

    private final APISource source;
    private final Changelog changelog = new Changelog();
    private final List<Version> versionHistory = new CopyOnWriteArrayList<>();
    private Version version;
    private final Map<Version, URL[]> updateURL = new ConcurrentHashMap<>();
    private final Map<Version, Instant> releaseDates = new ConcurrentHashMap<>();
    private boolean checking = false;
    private TaskCompletor<Void> checkTask;

    /**
     * Initialize the version checker
     *
     * @param source the source to check version with
     */
    public VersionChecker(final APISource source) {
        this.source = source;

        if (versionSchema == null) {
            try (InputStream stream = VersionChecker.class.getResourceAsStream("/update.schema.json")) {
                if (stream != null) {
                    String rawStream = StreamUtils.streamToString(stream, false);

                    JSONObject rawSchema = new JSONObject(rawStream);
                    versionSchema = SchemaLoader.load(rawSchema);
                }
            } catch (IOException ex) {
                source.logger().log(ex, "Failed to read update schema");
            }
        }
    }

    /**
     * Set the version checker schema
     *
     * @param schema the version schema
     * @return the version checker
     */
    public VersionChecker withSchema(final InputStream schema) {
        if (schema == null) {
            versionSchema = null;
            return this;
        }

        JSONObject rawSchema = new JSONObject(schema);
        versionSchema = SchemaLoader.load(rawSchema);

        return this;
    }

    /**
     * Check a version online and wait for the response
     *
     * @throws IOException if there's a problem connecting
     * with the update server
     */
    public void checkAndWait() throws IOException {
        if (!checking) {
            checking = true;
            checkOnline();
            checking = false;
        }
    }

    /**
     * Check a version
     *
     * @return when the check has been completed
     */
    public TaskCompletor<Void> check() {
        if (!checking) {
            checking = true;
            checkTask = new LateTask<>();

            source.scheduler("async").schedule(() -> {
                try {
                    checkOnline();
                } catch (IOException | ValidationException ex) {
                    checkTask.completeFirst(null, ex);
                } finally {
                    checkTask.completeFirst(null);
                    checking = false;
                }
            });
        }

        return checkTask;
    }

    /**
     * Check an online version
     *
     * @throws MalformedURLException if the update URL is not valid
     * @throws IOException if the connection fails to open
     * @throws ValidationException if the update URL does not point to a valid json version data
     */
    private void checkOnline() throws MalformedURLException, IOException, ValidationException {
        URI uri = source.sourceUpdateURI();
        if (uri == null) return;

        String rawUrl = uri.toString();
        URL url = new URL(rawUrl);

        /*try (URLConnectionWrapper connection = URLConnectionWrapper.fromURL(url)) {
            connection.setUserAgent(KarmaAPI.USER_AGENT.get());
            connection.setRequestProperty("Content-Type", "application/json");

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("Failed to connect to update web server at " + url + " with code: " + code);
            }


        }*/
        try (InputStream updateContent = url.openStream()) {
            if (updateContent != null) {
                String rawResponse = StreamUtils.streamToString(updateContent, false);
                JSONObject node = new JSONObject(rawResponse);
                if (versionSchema != null) {
                    /*
                    Should we throw exception instead if the schema
                    validator is null?
                    */
                    versionSchema.validate(node);
                } else {
                    source.logger().send(LogLevel.WARNING, "Schema validator is not valid, version checker might not work as expected");
                }

                Gson gson = new GsonBuilder().setLenient().create();
                JsonObject json = gson.fromJson(rawResponse, JsonObject.class);

                JsonArray versions = json.getAsJsonArray("versions");
                for (JsonElement element : versions) {
                    JsonObject versionNode = element.getAsJsonObject();
                    String raw = versionNode.keySet().toArray(new String[0])[0];
                    JsonObject infoNode = versionNode.getAsJsonObject(raw);

                    String build = infoNode.get("build").getAsString();
                    if (build.replaceAll("\\s", "").isEmpty()) build = null;

                    Version version = Version.parse(raw, build);

                    List<URL> urls = new ArrayList<>();
                    if (infoNode.has("update")) {
                        JsonArray rawUpdate = infoNode.getAsJsonArray("update");
                        for (JsonElement updateUrlLineNode : rawUpdate) {
                            String rawURL = updateUrlLineNode.getAsString()
                                    .replace("%version%", raw)
                                    .replace("%build%", String.valueOf(build));
                            URL realURL = URLUtilities.fromString(rawURL);
                            if (realURL != null)
                                urls.add(realURL);
                        }
                    }

                    updateURL.put(version, urls.toArray(new URL[0]));

                    JsonArray rawChangelog = infoNode.getAsJsonArray("changelog");
                    List<String> changelog = new ArrayList<>();

                    String when = infoNode.get("date").getAsString();

                    if (!versionHistory.contains(version)) {
                        versionHistory.add(version);
                    }

                    Instant instant = Instant.parse(when);
                    ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
                        /*
                        Whe don't care when the version was released from publisher
                        time zone, we want to know when it was released in our timezone
                         */

                    releaseDates.put(version, zdt.toInstant());
                    for (JsonElement changelogLineNode : rawChangelog)
                        changelog.add(changelogLineNode.getAsString()
                                .replace("%version%", raw)
                                .replace("%build%", String.valueOf(build)));

                    this.changelog.define(version, changelog.toArray(new String[0]));
                }

                if (!versionHistory.isEmpty()) {
                    versionHistory.sort(Version.comparator());
                    Collections.reverse(versionHistory);

                    version = versionHistory.get(0);
                }
            }
        }
    }

    /**
     * Get the check result version
     *
     * @return the check result version
     */
    @Nullable
    public Version getVersion() {
        return version;
    }

    /**
     * Get the version history
     *
     * @return the version history
     */
    public Version[] getVersionHistory() {
        return versionHistory.toArray(new Version[0]);
    }

    /**
     * Get the changelog for the
     * specified version
     *
     * @return the version changelog
     */
    public String[] getChangelog() {
        return getChangelog(version != null ? version : source.sourceVersion());
    }

    /**
     * Get the changelog for the specified version
     *
     * @param version the version
     * @return the version changelog
     */
    public String[] getChangelog(final Version version) {
        if (version == null) return new String[0];
        return changelog.getFor(version);
    }

    /**
     * Get the latest version release date
     *
     * @return the latest version release
     * date
     */
    public Instant getReleaseDate() {
        if (version == null) return Instant.MIN;
        return releaseDates.get(version);
    }

    /**
     * Get the release date for the specified
     * version
     *
     * @param version the version to get for
     * @return the version release date
     */
    public Instant getReleaseDate(final Version version) {
        return releaseDates.getOrDefault(version, Instant.now());
    }

    /**
     * Get the update URLs for
     * the specified version
     *
     * @return the update URLs
     */
    public URL[] getUpdateURLs() {
        return getUpdateURLs(version);
    }

    /**
     * Get the update URLs for
     * the specified version
     *
     * @param version the version
     * @return the update URLs
     */
    public URL[] getUpdateURLs(final Version version) {
        if (version == null) return new URL[0];
        return updateURL.getOrDefault(version, new URL[0]);
    }

    /**
     * Get the version build status
     *
     * @return the build status
     */
    public BuildStatus getStatus() {
        return compareWith(version);
    }

    /**
     * Get the number of updates behind
     * the last one
     *
     * @return the amount of updates behind
     * the last build
     */
    public int getBehind() {
        int behind = 0;

        Version current = source.sourceVersion();
        boolean count = false;
        for (Version version : versionHistory) {
            if (count) {
                behind++;
                continue;
            }

            if (current.compareTo(version) == 0) {
                count = true;
            }
        }

        return behind;
    }

    /**
     * Compare the version with the current
     * version
     *
     * @param version the version to check with
     * @return the build status
     */
    public BuildStatus compareWith(final Version version) {
        if (version == null) return BuildStatus.OUTDATED;

        Version current = source.sourceVersion();
        return current.compareTo(version) >= 0 ? BuildStatus.UPDATED : BuildStatus.OUTDATED;
    }
}
