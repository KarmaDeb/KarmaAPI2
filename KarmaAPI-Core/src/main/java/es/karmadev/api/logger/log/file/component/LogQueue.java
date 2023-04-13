package es.karmadev.api.logger.log.file.component;

import es.karmadev.api.logger.log.console.LogLevel;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Log queue
 */
public class LogQueue {

    private final Queue<QuePair<LogLevel, Instant, Object>> logMessageQueue = new ConcurrentLinkedDeque<>();

    /**
     * Append content to the log queue
     *
     * @param level the level
     * @param raw the raw content
     */
    public void append(final LogLevel level, final Object raw) {
        logMessageQueue.add(QuePair.build(level, Instant.now(), raw));
    }

    /**
     * Get the next que content
     *
     * @return the next queue content
     */
    public QuePair<LogLevel, Instant, Object> next() {
        return logMessageQueue.poll();
    }

    /**
     * Get if the queue contains items
     *
     * @return if the queue contains items
     */
    public boolean hasItems() {
        return !logMessageQueue.isEmpty();
    }
}
