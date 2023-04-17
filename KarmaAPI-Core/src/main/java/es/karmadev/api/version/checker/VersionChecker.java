package es.karmadev.api.version.checker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import es.karmadev.api.core.KarmaAPI;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.schedule.task.completable.TaskCompletor;
import es.karmadev.api.schedule.task.completable.late.LateTask;
import es.karmadev.api.version.BuildStatus;
import es.karmadev.api.version.Version;
import es.karmadev.api.web.URLConnectionWrapper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Version checker
 */
@SuppressWarnings("unused")
public class VersionChecker {

    private static JsonSchema versionSchema;

    private final KarmaSource source;
    private final Changelog changelog = new Changelog();
    private final List<Version> versionHistory = new CopyOnWriteArrayList<>();
    private Version version;
    private boolean checking = false;
    private TaskCompletor<Void> checkTask;

    /**
     * Initialize the version checker
     *
     * @param source the source to check version with
     */
    public VersionChecker(final KarmaSource source) {
        this.source = source;

        if (versionSchema == null) {
            try (InputStream stream = VersionChecker.class.getResourceAsStream("/update.schema.json")) {
                if (stream != null) {
                    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(stream);
                    versionSchema = factory.getJsonSchema(node);
                }
            } catch (IOException | ProcessingException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Check a version online and wait for the response
     *
     * @throws IOException if there's a problem connecting
     * with the update server
     * @throws ProcessingException if the update json doesn't match
     * the schema
     */
    public void checkAndWait() throws IOException, ProcessingException {
        if (versionSchema == null) return;

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

            source.createScheduler("async").schedule(() -> {
                try {
                    checkOnline();
                } catch (IOException | ProcessingException ex) {
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
     * @throws ProcessingException if the version data is not valid
     */
    private void checkOnline() throws MalformedURLException, IOException, ProcessingException {
        String rawUrl = source.updateURL();
        if (rawUrl == null) throw new IOException("Cannot check version for a source with invalid update URL");
        URL url = new URL(rawUrl);

        try (URLConnectionWrapper connection = URLConnectionWrapper.fromURL(url)) {
            connection.setUserAgent(KarmaAPI.USER_AGENT);
            connection.setRequestProperty("Content-Type", "application/json");

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("Failed to connect to update web server at " + url + " with code: " + code);
            }

            try (InputStream updateContent = connection.getInputStream()) {
                if (updateContent != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(updateContent);

                    ProcessingReport report = versionSchema.validate(node);
                    if (!report.isSuccess()) {
                        ProcessingMessage message = new ProcessingMessage();
                        report.error(message);
                        throw message.asException();
                    }

                    ArrayNode versions = (ArrayNode) node.get("versions");
                    for (JsonNode versionNode : versions) {
                        String raw = versionNode.fieldNames().next();
                        JsonNode infoNode = versionNode.get(raw);

                        String build = infoNode.get("build").asText();
                        if (build.replaceAll("\\s", "").isEmpty()) build = null;

                        ArrayNode rawChangelog = (ArrayNode) infoNode.get("changelog");
                        List<String> changelog = new ArrayList<>();

                        Version version = Version.parse(raw, build);
                        versionHistory.add(version);
                        for (JsonNode changelogLineNode : rawChangelog) changelog.add(changelogLineNode.asText().replace("%version%", raw));
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
        return getChangelog(version != null ? version : source.getVersion());
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
     * Get the version build status
     *
     * @return the build status
     */
    public BuildStatus getBuildStatus() {
        return compareWith(version);
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

        Version current = source.getVersion();
        return version.compareTo(current) >= 0 ? BuildStatus.UPDATED : BuildStatus.OUTDATED;
    }
}
