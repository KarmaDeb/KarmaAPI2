package es.karmadev.api.logger.log;

import es.karmadev.api.core.config.APIConfiguration;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.logger.log.file.LogFile;

/**
 * KarmaAPI bounded logger
 */
@SuppressWarnings("unused")
public class BoundedLogger implements SourceLogger {

    private final static APIConfiguration config = new APIConfiguration();

    private final KarmaSource source;
    private final LogFile log;
    private boolean logToConsole;

    /**
     * Initialize the console
     *
     * @param owner the console owner
     */
    public BoundedLogger(final KarmaSource owner) {
        this.source = owner;
        log = new LogFile(source);
    }

    /**
     * Set the logger log to console
     *
     * @param status the log console out status
     */
    public void setLogToConsole(final boolean status) {
        logToConsole = status;
    }

    /**
     * get if the logger log prints to
     * console
     *
     * @return if the logger logs to console
     */
    public boolean logToConsole() {
        return logToConsole;
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
        if (config.isLevelEnabled(level)) {
            String finalMessage = buildMessage(level, message, replaces);
            System.out.println(ConsoleColor.parse(finalMessage));
        }
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
        if (config.isLevelEnabled(LogLevel.ERROR)) {
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
    }

    /**
     * Send a message to the console
     *
     * @param message  the message to send
     * @param replaces the message replaces
     */
    @Override
    public void send(final String message, final Object... replaces) {
        String finalMessage = buildMessage(null, message, replaces);
        System.out.println(ConsoleColor.parse(finalMessage));
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
        if (logToConsole)
            send(level, message, replaces);

        log.append(level, parseReplaces(message, replaces));
    }

    /**
     * Log a message into the console
     *
     * @param error    the error
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void log(final Throwable error, final String message, final Object... replaces) {
        if (logToConsole)
            send(error, message, replaces);

        log.append(LogLevel.ERROR, error, parseReplaces(message, replaces));
    }

    /**
     * Log a message into the console
     *
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void log(final String message, final Object... replaces) {
        if (logToConsole)
            send(message, replaces);

        log.append(LogLevel.INFO, parseReplaces(message, replaces));
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
        String prefix = config.getPrefix(level).replace("{0}", source.getName());

        String finalMessage = parseReplaces(message, replaces);
        return prefix.replace("{1}", finalMessage);
    }

    /**
     * Parse the message replaces
     *
     * @param message the message
     * @param replaces the message replaces
     * @return the message
     */
    private String parseReplaces(final String message, final Object... replaces) {
        String finalMessage = message;
        for (int i = 0; i < replaces.length; i++) {
            String placeholder = "{" + i + "}";
            Object replace = replaces[i];
            finalMessage = finalMessage.replace(placeholder, String.valueOf(replace));
        }

        return finalMessage;
    }
}
