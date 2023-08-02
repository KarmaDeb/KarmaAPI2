package es.karmadev.api.logger;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.logger.log.console.LogLevel;

import java.util.function.Function;

/**
 * Karma console logger
 */
@SuppressWarnings("unused")
public interface SourceLogger {

    /**
     * Override the log function
     *
     * @param function the new log function
     * @return the modified logger
     */
    SourceLogger overrideLogFunction(final Function<String, Void> function);

    /**
     * Send a message to the console
     *
     * @param level the message level
     * @param message the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException implementations may
     */
    void send(final LogLevel level, final String message, final Object... replaces) throws UnsupportedOperationException;

    /**
     * Send a message to the console
     *
     * @param error the error
     * @param message the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException implementations may
     */
    void send(final Throwable error, final String message, final Object... replaces) throws UnsupportedOperationException;

    /**
     * Send a message to the console
     *
     * @param message the message to send
     * @param replaces the message replaces
     * @throws UnsupportedOperationException implementations may
     */
    void send(final String message, final Object... replaces) throws UnsupportedOperationException;

    /**
     * Log a message into the console
     *
     * @param level the log level
     * @param message the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException implementations may
     */
    void log(final LogLevel level, final String message, final Object... replaces) throws UnsupportedOperationException;

    /**
     * Log a message into the console
     *
     * @param error the error
     * @param message the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException implementations may
     */
    void log(final Throwable error, final String message, final Object... replaces) throws UnsupportedOperationException;

    /**
     * Log a message into the console
     *
     * @param message the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException implementations may
     */
    void log(final String message, final Object... replaces) throws UnsupportedOperationException;

    /**
     * Get the source owning this console
     *
     * @return the console source
     */
    APISource owner();
}
