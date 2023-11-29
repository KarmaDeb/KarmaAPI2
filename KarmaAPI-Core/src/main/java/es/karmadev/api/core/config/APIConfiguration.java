package es.karmadev.api.core.config;

import com.google.gson.*;
import es.karmadev.api.core.KarmaAPI;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.logger.log.console.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * KarmaAPI configuration
 */
@SuppressWarnings("unused")
public final class APIConfiguration {

    private static JsonObject settings;

    /**
     * Initialize the API configuration
     *
     * @throws RuntimeException if something happens while initializing the
     * configuration
     */
    public APIConfiguration() throws RuntimeException {
        try {
            KarmaAPI.setup();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

        if (settings == null) {
            Path workingDirectory = Paths.get("./KarmaAPI");
            Path config = workingDirectory.resolve("settings.json");
            boolean write = !Files.exists(config);

            if (PathUtilities.createPath(config)) {
                if (write) {
                    if (!PathUtilities.copy(PathUtilities.DEFAULT_LOADER, "karmaSettings.json", config)) {
                        PathUtilities.destroy(config);
                        return;
                    }
                }

                Gson gson = new GsonBuilder().create();
                try (InputStream stream = PathUtilities.toStream(config)) {
                    String raw = StreamUtils.streamToString(stream);
                    JsonElement element = gson.fromJson(raw, JsonElement.class);

                    if (!element.isJsonObject()) return;

                    settings = element.getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * Get the logger prefix
     *
     * @param level the log level
     * @return the prefix
     */
    public String getPrefix(final LogLevel level) {
        if (settings == null || !settings.has("logger") || !settings.get("logger").isJsonObject()) {
            return (level != null ? level.getPrefix() : "&7[{0}]: &f{1}");
        }

        JsonObject loggerSettings = settings.get("logger").getAsJsonObject();
        if (!loggerSettings.has("console") || !loggerSettings.get("console").isJsonObject()) {
            return (level != null ? level.getPrefix() : "&7[{0}]: &f{1}");
        }

        JsonObject consoleSettings = loggerSettings.get("console").getAsJsonObject();
        if (!consoleSettings.has("prefix") || !consoleSettings.get("prefix").isJsonArray()) {
            return (level != null ? level.getPrefix() : "&7[{0}]: &f{1}");
        }

        JsonArray prefixArray = consoleSettings.get("prefix").getAsJsonArray();
        for (JsonElement prefixElement : prefixArray) {
            if (!prefixElement.isJsonObject()) {
                continue;
            }

            JsonObject prefixObject = prefixElement.getAsJsonObject();
            if (!prefixObject.has("type") || !prefixObject.has("display")) {
                continue;
            }

            JsonElement typeElement = prefixObject.get("type");
            JsonElement displayElement = prefixObject.get("display");
            if (!typeElement.isJsonPrimitive() || !displayElement.isJsonPrimitive()) {
                continue;
            }

            JsonPrimitive typePrimitive = typeElement.getAsJsonPrimitive();
            JsonPrimitive displayPrimitive = displayElement.getAsJsonPrimitive();
            if (!typePrimitive.isString() || !displayPrimitive.isString()) {
                continue;
            }

            if (level == null) {
                if (typePrimitive.getAsString().equalsIgnoreCase("none"))  return displayPrimitive.getAsString();
            } else {
                if (level.name().equalsIgnoreCase(typePrimitive.getAsString())) {
                    return displayPrimitive.getAsString();
                }
            }
        }

        return (level != null ? level.getPrefix() : "&7[{0}]: &f{1}");
    }

    /**
     * Get if the specified log level
     * is enabled
     *
     * @param level the log level
     * @return if the level is enabled
     */
    public boolean isLevelEnabled(final LogLevel level) {
        if (settings == null || !settings.has("logger") || !settings.get("logger").isJsonObject()) {
            return true;
        }

        JsonObject loggerSettings = settings.get("logger").getAsJsonObject();
        if (!loggerSettings.has("console") || !loggerSettings.get("console").isJsonObject()) {
            return true;
        }

        JsonObject consoleSettings = loggerSettings.get("console").getAsJsonObject();
        if (!consoleSettings.has("print") || !consoleSettings.get("print").isJsonArray()) {
            return true;
        }

        JsonArray printArray = consoleSettings.get("print").getAsJsonArray();
        return printArray.contains(new JsonPrimitive(level.name()));
    }

    /**
     * Get if the logger works asynchronously
     *
     * @return if the logger works in another
     * thread
     */
    public boolean asyncConsoleLogger() {
        if (settings == null || !settings.has("logger") || !settings.get("logger").isJsonObject()) {
            return true;
        }

        JsonObject loggerSettings = settings.get("logger").getAsJsonObject();
        if (!loggerSettings.has("console") || !loggerSettings.get("console").isJsonPrimitive()) {
            return true;
        }

        JsonObject consoleSettings = loggerSettings.get("console").getAsJsonObject();
        if (!consoleSettings.has("async") || !consoleSettings.get("async").isJsonPrimitive()) {
            return true;
        }

        JsonPrimitive primitive = consoleSettings.get("async").getAsJsonPrimitive();
        return (!primitive.isBoolean() || primitive.getAsBoolean());
    }

    /**
     * Get if the logger works asynchronously
     *
     * @return if the logger works in another
     * thread
     */
    public boolean asyncFileLogger() {
        if (settings == null || !settings.has("logger") || !settings.get("logger").isJsonObject()) {
            return true;
        }

        JsonObject loggerSettings = settings.get("logger").getAsJsonObject();
        if (!loggerSettings.has("file") || !loggerSettings.get("file").isJsonPrimitive()) {
            return true;
        }

        JsonObject fileSettings = loggerSettings.get("file").getAsJsonObject();
        if (!fileSettings.has("async") || !fileSettings.get("async").isJsonPrimitive()) {
            return true;
        }

        JsonPrimitive primitive = fileSettings.get("async").getAsJsonPrimitive();
        return (!primitive.isBoolean() || primitive.getAsBoolean());
    }

    /**
     * Get if the configuration requests the URL
     * response codes to be 200
     *
     * @return if the requests URL response codes
     * must be 200
     */
    public boolean strictURLCodes() {
        if (settings == null || !settings.has("url") || !settings.get("url").isJsonObject()) {
            return false;
        }

        JsonObject urlSettings = settings.get("url").getAsJsonObject();
        if (!urlSettings.has("strict") || !urlSettings.get("strict").isJsonPrimitive()) {
            return false;
        }

        JsonPrimitive primitive = urlSettings.get("strict").getAsJsonPrimitive();
        return (primitive.isBoolean() && primitive.getAsBoolean());
    }

    /**
     * Get the configuration URL requests timeout
     *
     * @return the requests timeout
     */
    public int requestTimeout() {
        if (settings == null || !settings.has("url") || !settings.get("url").isJsonObject()) {
            return 5000;
        }

        JsonObject urlSettings = settings.get("url").getAsJsonObject();
        if (!urlSettings.has("timeout") || !urlSettings.get("timeout").isJsonPrimitive()) {
            return 5000;
        }

        JsonPrimitive primitive = urlSettings.get("timeout").getAsJsonPrimitive();
        return (primitive.isNumber() ? primitive.getAsInt() : 5000);
    }

    /**
     * Get if the configuration allows the use
     * of experimental features
     *
     * @return if the configuration allows
     * experimental features
     */
    public boolean enableExperimental() {
        if (settings == null || !settings.has("experimental") || !settings.get("experimental").isJsonPrimitive()) {
            return false;
        }

        JsonPrimitive primitive = settings.get("experimental").getAsJsonPrimitive();
        return primitive.isBoolean() && primitive.getAsBoolean();
    }
}
