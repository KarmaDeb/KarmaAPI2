package es.karmadev.api.logger.log;

import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.logger.log.console.LogLevel;

/**
 * Unbounded console. Mainly made for the kore source
 */
@SuppressWarnings("unused")
public class UnboundedLogger implements SourceLogger {

    private KarmaSource source;
    private SourceLogger logger;

    /**
     * Bind the source
     *
     * @param source the source
     */
    @SuppressWarnings("UnusedReturnValue")
    public UnboundedLogger bind(final KarmaSource source) {
        this.source = source;
        logger = LogManager.getLogger(source);
        return this;
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
        if (logger != null) {
            logger.send(level, message, replaces);
            return;
        }

        String finalMessage = buildMessage(level, message, replaces);
        System.out.println(ConsoleColor.parse(finalMessage));
    }

    /**
     * Send a message to the console
     *
     * @param error    the error
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void send(final Throwable error, final String message, final Object... replaces) {
        if (logger != null) {
            logger.send(error, message, replaces);
            return;
        }

        StackTraceElement[] elements = error.getStackTrace();
        String finalMessage = buildMessage(LogLevel.ERROR, message, replaces);

        System.out.println(ConsoleColor.parse(finalMessage + " &7(&b " + error.fillInStackTrace() + " &8|&b " + error.getLocalizedMessage() + " &7)"));
        for (StackTraceElement element : elements) {
            String clazz = element.getClassName();
            String method = element.getMethodName();
            String file = element.getFileName();
            int line = element.getLineNumber();

            if (file == null) {
                System.out.printf(ConsoleColor.parse("\t\t\t&c%s&8&c&f#&7%s&8 (&cat line &b%d&8)%n"), clazz, method, line);
            } else {
                file = file.replace(".java", "");
                System.out.printf(ConsoleColor.parse("\t\t\t&c%s&f#&7%s&8 (&cat &7%s&f:&b%d&8)%n"), clazz, method, file, line);
            }
        }
    }

    /**
     * Send a message to the console
     *
     * @param message  the message to send
     * @param replaces the message replaces
     */
    @Override
    public void send(final String message, final Object... replaces) {
        if (logger != null) {
            logger.send(message, replaces);
            return;
        }

        send((LogLevel) null, message, replaces);
    }

    /**
     * Log a message into the console
     *
     * @param level    the log level
     * @param message  the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException always
     */
    @Override
    public void log(final LogLevel level, final String message, final Object... replaces) throws UnsupportedOperationException {
        if (logger != null) {
            logger.log(level, message, replaces);
            return;
        }
        throw new UnsupportedOperationException("Cannot log from unbounded source");
    }

    /**
     * Log a message into the console
     *
     * @param error    the error
     * @param message  the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException always
     */
    @Override
    public void log(final Throwable error, final String message, final Object... replaces) throws UnsupportedOperationException {
        if (logger != null) {
            logger.log(error, message, replaces);
            return;
        }

        throw new UnsupportedOperationException("Cannot log from unbounded source");
    }

    /**
     * Log a message into the console
     *
     * @param message  the message
     * @param replaces the message replaces
     * @throws UnsupportedOperationException always
     */
    @Override
    public void log(final String message, final Object... replaces) throws UnsupportedOperationException {
        if (logger != null) {
            logger.log(message, message, replaces);
            return;
        }

        throw new UnsupportedOperationException("Cannot log from unbounded source");
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

    /**
     * Build the message
     *
     * @param level the message level
     * @param message the message
     * @param replaces the message replaces
     * @return the message
     */
    private String buildMessage(final LogLevel level, final String message, final Object... replaces) {
        String prefix = "&7[&cKarmaAPI - Unbounded&7]&b&8: &f{1}";
        if (level != null) {
            prefix = "&7[&cKarmaAPI - Unbounded&7]&b {&3" + level.name() + "&b}&8: &f{1}";
        }

        String finalMessage = message;
        for (int i = 0; i < replaces.length; i++) {
            String placeholder = "{" + i + "}";
            Object replace = replaces[i];
            finalMessage = finalMessage.replace(placeholder, String.valueOf(replace));
        }
        return prefix.replace("{1}", finalMessage);
    }
}
