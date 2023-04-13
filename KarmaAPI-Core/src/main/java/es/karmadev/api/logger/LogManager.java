package es.karmadev.api.logger;

import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.logger.log.BoundedLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Karma log manager
 */
public class LogManager {

    private final static Map<KarmaSource, SourceLogger> consoleLoggers = new ConcurrentHashMap<>();

    /**
     * Get the console logger of a source
     *
     * @param source the source
     * @return the source console logger
     */
    public static SourceLogger getLogger(final KarmaSource source) {
        return consoleLoggers.computeIfAbsent(source, (logger) -> new BoundedLogger(source));
    }
}
