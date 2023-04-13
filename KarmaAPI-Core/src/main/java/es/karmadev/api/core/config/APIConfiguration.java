package es.karmadev.api.core.config;

import com.google.gson.*;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.logger.log.console.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * KarmaAPI configuration
 */
@SuppressWarnings("unused")
public final class APIConfiguration {

    private final static Logger logger = Logger.getLogger("KarmaSource - Configuration");
    private final static KarmaKore source = KarmaKore.INSTANCE();
    private static JsonObject settings;

    /**
     * Initialize the API configuration
     *
     * @throws RuntimeException if something happens while initializing the
     * configuration
     */
    public APIConfiguration() throws RuntimeException {
        if (settings == null) {
            if (source == null)
                throw new RuntimeException("Cannot access API configuration because KarmaAPI source is null");
            Path config = source.getWorkingDirectory().resolve("settings.json");
            boolean write = !Files.exists(config);

            if (PathUtilities.createPath(config)) {
                if (write) {
                    if (PathUtilities.copy(PathUtilities.DEFAULT_LOADER, "karmaSettings.json", config)) {
                        //source.getConsole().log(LogLevel.DEBUG, "Exported source settings.json");
                        logger.log(Level.FINE, "Exported source settings.json");
                    } else {
                        PathUtilities.destroy(config);
                        throw new RuntimeException("Cannot access API configuration because configuration was unable to write");
                    }
                }

                Gson gson = new GsonBuilder().create();
                try (InputStream stream = PathUtilities.toStream(config)) {
                    String raw = StreamUtils.streamToString(stream);
                    JsonElement element = gson.fromJson(raw, JsonElement.class);

                    if (!element.isJsonObject())
                        throw new RuntimeException("Cannot access API configuration because configuration is invalid");

                    settings = element.getAsJsonObject();
                    return;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            throw new RuntimeException("Cannot access API configuration because the configuration file failed to resolve");
        }
    }

    /**
     * Get the logger prefix
     *
     * @param level the log level
     * @return the prefix
     */
    public String getPrefix(final LogLevel level) {
        if (!settings.has("logger") || !settings.get("logger").isJsonObject()) {
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
        if (!settings.has("logger") || !settings.get("logger").isJsonObject()) {
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
        return printArray.contains(level.getNameElement());
    }

    /**
     * Get if the logger works asynchronously
     *
     * @return if the logger works in another
     * thread
     */
    public boolean asyncConsoleLogger() {
        if (!settings.has("logger") || !settings.get("logger").isJsonObject()) {
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
        if (!settings.has("logger") || !settings.get("logger").isJsonObject()) {
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
     * Get if the configuration allows the use
     * of experimental features
     *
     * @return if the configuration allows
     * experimental features
     */
    public boolean enableExperimental() {
        if (!settings.has("experimental") || !settings.get("experimental").isJsonPrimitive()) {
            return false;
        }

        JsonPrimitive primitive = settings.get("experimental").getAsJsonPrimitive();
        return primitive.isBoolean() && primitive.getAsBoolean();
    }
}
