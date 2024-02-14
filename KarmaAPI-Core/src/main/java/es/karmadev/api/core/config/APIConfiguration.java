package es.karmadev.api.core.config;

import es.karmadev.api.core.KarmaAPI;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.kson.object.type.NativeString;
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

                try (InputStream stream = PathUtilities.toStream(config)) {
                    JsonInstance element = JsonReader.read(stream);
                    if (element == null || !element.isObjectType()) return;

                    settings = element.asObject();
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
        if (settings == null || !settings.hasChild("logger") || !settings.getChild("logger").isObjectType()) {
            return (level != null ? level.getPrefix() : "&7[{0}]: &f{1}");
        }

        JsonObject loggerSettings = settings.getChild("logger").asObject();
        if (!loggerSettings.hasChild("console") || !loggerSettings.getChild("console").isObjectType()) {
            return (level != null ? level.getPrefix() : "&7[{0}]: &f{1}");
        }

        JsonObject consoleSettings = loggerSettings.getChild("console").asObject();
        if (!consoleSettings.hasChild("prefix") || !consoleSettings.getChild("prefix").isArrayType()) {
            return (level != null ? level.getPrefix() : "&7[{0}]: &f{1}");
        }

        JsonArray prefixArray = consoleSettings.getChild("prefix").asArray();
        for (JsonInstance prefixElement : prefixArray) {
            if (!prefixElement.isObjectType()) {
                continue;
            }

            JsonObject prefixObject = prefixElement.asObject();
            if (!prefixObject.hasChild("type") || !prefixObject.hasChild("display")) {
                continue;
            }

            JsonInstance typeElement = prefixObject.getChild("type");
            JsonInstance displayElement = prefixObject.getChild("display");
            if (!typeElement.isNativeType() || !displayElement.isNativeType()) {
                continue;
            }

            JsonNative typePrimitive = typeElement.asNative();
            JsonNative displayPrimitive = displayElement.asNative();
            if (!typePrimitive.isString() || !displayPrimitive.isString()) {
                continue;
            }

            if (level == null) {
                if (typePrimitive.asString().equalsIgnoreCase("none"))  return displayPrimitive.getAsString();
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
        if (settings == null || !settings.hasChild("logger") || !settings.getChild("logger").isObjectType()) {
            return true;
        }

        JsonObject loggerSettings = settings.getChild("logger").asObject();
        if (!loggerSettings.hasChild("console") || !loggerSettings.getChild("console").isObjectType()) {
            return true;
        }

        JsonObject consoleSettings = loggerSettings.getChild("console").asObject();
        if (!consoleSettings.hasChild("print") || !consoleSettings.getChild("print").isArrayType()) {
            return true;
        }

        JsonArray printArray = consoleSettings.getChild("print").asArray();
        return printArray.contains(new NativeString("print", level.name()));
    }

    /**
     * Get if the logger works asynchronously
     *
     * @return if the logger works in another
     * thread
     */
    public boolean asyncConsoleLogger() {
        if (settings == null || !settings.hasChild("logger") || !settings.getChild("logger").isObjectType()) {
            return true;
        }

        JsonObject loggerSettings = settings.getChild("logger").asObject();
        if (!loggerSettings.hasChild("console") || !loggerSettings.getChild("console").isObjectType()) {
            return true;
        }

        JsonObject consoleSettings = loggerSettings.getChild("console").asObject();
        if (!consoleSettings.hasChild("async") || !consoleSettings.getChild("async").isNativeType()) {
            return true;
        }

        JsonNative primitive = consoleSettings.getChild("async").asNative();
        return (!primitive.isBoolean() || primitive.getBoolean());
    }

    /**
     * Get if the logger works asynchronously
     *
     * @return if the logger works in another
     * thread
     */
    public boolean asyncFileLogger() {
        if (settings == null || !settings.hasChild("logger") || !settings.getChild("logger").isObjectType()) {
            return true;
        }

        JsonObject loggerSettings = settings.getChild("logger").asObject();
        if (!loggerSettings.hasChild("file") || !loggerSettings.getChild("file").isNativeType()) {
            return true;
        }

        JsonObject fileSettings = loggerSettings.getChild("file").asObject();
        if (!fileSettings.hasChild("async") || !fileSettings.getChild("async").isNativeType()) {
            return true;
        }

        JsonNative primitive = fileSettings.getChild("async").asNative();
        return (!primitive.isBoolean() || primitive.getBoolean());
    }

    /**
     * Get if the configuration requests the URL
     * response codes to be 200
     *
     * @return if the requests URL response codes
     * must be 200
     */
    public boolean strictURLCodes() {
        if (settings == null || !settings.hasChild("url") || !settings.getChild("url").isObjectType()) {
            return false;
        }

        JsonObject urlSettings = settings.getChild("url").asObject();
        if (!urlSettings.hasChild("strict") || !urlSettings.getChild("strict").isNativeType()) {
            return false;
        }

        JsonNative primitive = urlSettings.getChild("strict").asNative();
        return (primitive.isBoolean() && primitive.getBoolean());
    }

    /**
     * Get the configuration URL requests timeout
     *
     * @return the requests timeout
     */
    public int requestTimeout() {
        if (settings == null || !settings.hasChild("url") || !settings.getChild("url").isObjectType()) {
            return 5000;
        }

        JsonObject urlSettings = settings.getChild("url").asObject();
        if (!urlSettings.hasChild("timeout") || !urlSettings.getChild("timeout").isNativeType()) {
            return 5000;
        }

        JsonNative primitive = urlSettings.getChild("timeout").asNative();
        return (primitive.isNumber() ? primitive.getInteger() : 5000);
    }

    /**
     * Get if the configuration allows the use
     * of experimental features
     *
     * @return if the configuration allows
     * experimental features
     */
    public boolean enableExperimental() {
        if (settings == null || !settings.hasChild("experimental") || !settings.getChild("experimental").isNativeType()) {
            return false;
        }

        JsonNative primitive = settings.getChild("experimental").asNative();
        return primitive.isBoolean() && primitive.getBoolean();
    }
}
