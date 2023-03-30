package es.karmadev.api.logger.console;

import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.logger.LogLevel;

/**
 * Karma console logger
 */
public interface ConsoleLogger {

    /**
     * Send a message to the console
     *
     * @param level the message level
     * @param message the message
     * @param replaces the message replaces
     */
    void send(final LogLevel level, final String message, final Object... replaces);

    /**
     * Send a message to the console
     *
     * @param level the message level
     * @param error the error
     * @param message the message
     * @param replaces the message replaces
     */
    void send(final LogLevel level, final Throwable error, final String message, final Object... replaces);

    /**
     * Send a message to the console
     *
     * @param message the message to send
     * @param replaces the message replaces
     */
    void send(final String message, final Object... replaces);

    /**
     * Log a message into the console
     *
     * @param level the log level
     * @param message the message
     * @param replaces the message replaces
     */
    void log(final LogLevel level, final String message, final Object... replaces);

    /**
     * Log a message into the console
     *
     * @param level the message level
     * @param error the error
     * @param message the message
     * @param replaces the message replaces
     */
    void log(final LogLevel level, final Throwable error, final String message, final Object... replaces);

    /**
     * Get the source owning this console
     *
     * @return the console source
     */
    KarmaSource owner();
}
