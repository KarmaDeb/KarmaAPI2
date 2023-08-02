package es.karmadev.api.logger.log.console;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.logging.Level;

/**
 * Karma logging level
 */
@SuppressWarnings("unused")
public enum LogLevel {
    /**
     * Non important debug message
     */
    DEBUG("DEBUG", "&7[DEBUG] {0}&7:&f {1}", Level.INFO),
    /**
     * Important debug message
     */
    DEBUG_SEVERE("SEVERE DEBUG", "&7[SEVERE DEBUG] {0}&7:&f {1}", Level.INFO),
    /**
     * Success message
     */
    SUCCESS("SUCCESS", "&7[SUCCESS] {0}&7:&f {1}", Level.FINE),
    /**
     * Informative message
     */
    INFO("INFO", "&7[INFO] {0}&7:&f {1}", Level.INFO),
    /**
     * Warning message
     */
    WARNING("WARNING", "&7[WARNING] {0}&7:&f {1}", Level.WARNING),
    /**
     * Severe message
     */
    SEVERE("SEVERE", "&7[SEVERE] {0}&7:&f {1}", Level.SEVERE),
    /**
     * Error message
     */
    ERROR("ERROR", "&7[ERROR] {0}&7:&f {1}", Level.SEVERE);

    @Getter
    private final String raw;
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
    LogLevel(final String raw, final String prefix, final Level level) {
        this.raw = raw;
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
