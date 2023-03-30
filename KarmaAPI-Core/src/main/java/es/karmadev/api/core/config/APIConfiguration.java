package es.karmadev.api.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.file.PathUtilities;
import es.karmadev.api.file.StreamUtils;
import es.karmadev.api.logger.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * KarmaAPI configuration
 */
public final class APIConfiguration {

    private final static KarmaKore source = KarmaKore.INSTANCE();
    private final JsonObject settings;

    public APIConfiguration() throws RuntimeException {
        if (source == null) throw new RuntimeException("Cannot access API configuration because KarmaAPI source is null");
        Path config = source.getWorkingDirectory().resolve("settings.json");
        boolean write = !Files.exists(config);

        if (PathUtilities.createPath(config)) {
            if (write) {
                if (PathUtilities.copy(PathUtilities.DEFAULT_LOADER, "karmaSettings.json", config)) {
                    source.getConsole().log(LogLevel.DEBUG, "Exported source settings.json");
                } else {
                    PathUtilities.destroy(config);
                    throw new RuntimeException("Cannot access API configuration because configuration was unable to write");
                }
            }

            Gson gson = new GsonBuilder().create();
            try (InputStream stream = PathUtilities.toStream(config)) {
                String raw = StreamUtils.streamToString(stream);
                JsonElement element = gson.fromJson(raw, JsonElement.class);

                if (!element.isJsonObject()) throw new RuntimeException("Cannot access API configuration because configuration is invalid");
                settings = element.getAsJsonObject();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            throw new RuntimeException();
        }

        throw new RuntimeException("Cannot access API configuration because the configuration file failed to resolve");
    }

    /**
     * Get the logger prefix
     *
     * @param level the log level
     * @return the prefix
     */
    public String getPrefix(final LogLevel level) {
        return null; //TODO: Read configuration
    }
}
