package es.karmadev.api.logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.logging.Level;

/**
 * Karma logging level
 */
public enum LogLevel {
    /**
     * Non important debug message
     */
    DEBUG("&7[DEBUG] {0}:&f {1}", Level.INFO),
    /**
     * Important debug message
     */
    DEBUG_SEVERE("&7[SEVERE DEBUG] {0}:&f {1}", Level.INFO),
    /**
     * Success message
     */
    SUCCESS("&7[SUCCESS] {0}:&f {1}", Level.FINE),
    /**
     * Informative message
     */
    INFO("&7[INFO] {0}:&f {1}", Level.INFO),
    /**
     * Warning message
     */
    WARNING("&7[WARNING] {0}:&f {1}", Level.WARNING),
    /**
     * Severe message
     */
    SEVERE("&7[SEVERE] {0}:&f {1}", Level.SEVERE),
    /**
     * Error message
     */
    ERROR("&7[ERROR] {0}:&f {1}", Level.SEVERE);

    /**
     * Level prefix
     */
    @Getter
    private final String prefix;
    @Getter
    private final JsonElement nameElement;
    private final Level level;

    /**
     * Initialize the log level
     *
     * @param prefix the level prefix
     * @param level the java logger level
     */
    LogLevel(final String prefix, final Level level) {
        this.prefix = prefix;
        this.nameElement = new JsonPrimitive(this.name());
        this.level = level;
    }

    /**
     * Get the java logger level
     * equivalent
     *
     * @return the java logger level
     */
    public Level toJavaLevel() {
        return level;
    }
}
