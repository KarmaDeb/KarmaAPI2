package es.karmadev.api.logger.console;

import es.karmadev.api.logger.LogLevel;
import es.karmadev.api.core.source.KarmaSource;

import java.util.logging.Logger;

public class DefaultConsole implements ConsoleLogger {

    private final KarmaSource source;
    private final Logger logger;

    /**
     * Initialize the console
     *
     * @param owner the console owner
     */
    public DefaultConsole(final KarmaSource owner) {
        this.source = owner;
        logger = Logger.getLogger(owner.getName());
    }

    /**
     * Send a message to the console
     *
     * @param level    the message level
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void send(final LogLevel level, final String message, final Object... replaces) {
        switch (level) {
            case DEBUG:
                break;
            case DEBUG_SEVERE:
                break;
            case SUCCESS:
                break;
            case INFO:
                break;
            case WARNING:
                break;
            case SEVERE:
                break;
            case ERROR:
                break;
        }
    }

    /**
     * Send a message to the console
     *
     * @param level    the message level
     * @param error    the error
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void send(final LogLevel level, final Throwable error, final String message, final Object... replaces) {

    }

    /**
     * Send a message to the console
     *
     * @param message  the message to send
     * @param replaces the message replaces
     */
    @Override
    public void send(final String message, final Object... replaces) {

    }

    /**
     * Log a message into the console
     *
     * @param level    the log level
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void log(final LogLevel level, final String message, final Object... replaces) {

    }

    /**
     * Log a message into the console
     *
     * @param level    the message level
     * @param error    the error
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void log(final LogLevel level, final Throwable error, final String message, final Object... replaces) {

    }

    /**
     * Get the source owning this console
     *
     * @return the console source
     */
    @Override
    public KarmaSource owner() {
        return source;
    }
}
