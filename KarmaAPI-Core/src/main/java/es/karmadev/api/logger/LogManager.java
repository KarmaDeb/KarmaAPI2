package es.karmadev.api.logger;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.logger.log.BoundedLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Karma log manager
 */
public class LogManager {

    private final static Map<APISource, SourceLogger> consoleLoggers = new ConcurrentHashMap<>();

    /**
     * Create a new logger
     *
     * @param owner the logger owner
     * @return the new logger
     */
    public static SourceLogger getLogger(final APISource owner) {
        return consoleLoggers.computeIfAbsent(owner, (logger) -> new BoundedLogger(owner));
    }
}
