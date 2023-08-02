package es.karmadev.api.logger.log;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.logger.log.console.LogLevel;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Unbounded console. Mainly made for the kore source
 */
@SuppressWarnings("unused")
public class UnboundedLogger implements SourceLogger {

    private APISource source;
    private SourceLogger logger;
    private Function<String, Void> logFunction;

    /**
     * Initialize the unbounded logger
     */
    public UnboundedLogger() {}

    /**
     * Bind the source
     *
     * @param source the source
     */
    @SuppressWarnings("UnusedReturnValue")
    public UnboundedLogger bind(final APISource source) {
        this.source = source;
        logger = LogManager.getLogger(source);
        return this;
    }

    /**
     * Override the log function
     *
     * @param function the new log function
     * @return the modified logger
     */
    @Override
    public UnboundedLogger overrideLogFunction(final Function<String, Void> function) {
        this.logFunction = function;
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
        if (logFunction != null) {
            logFunction.apply(finalMessage);
            return;
        }

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

        String msg = finalMessage + " &7(&b " + error.fillInStackTrace() + " &8|&b " + error.getLocalizedMessage() + " &7)";
        if (logFunction != null) {
            logFunction.apply(msg);
        } else {
            System.out.println(ConsoleColor.parse(msg));
        }
        for (StackTraceElement element : elements) {
            String clazz = element.getClassName();
            String method = element.getMethodName();
            String file = element.getFileName();
            int line = element.getLineNumber();

            if (file == null) {
                String eMsg = String.format("\t\t\t&c%s&8&c&f#&7%s&8 (&cat line &b%d&8)%n", clazz, method, line);
                if (logFunction != null) {
                    logFunction.apply(eMsg);
                } else {
                    System.out.println(ConsoleColor.parse(eMsg));
                }
            } else {
                file = file.replace(".java", "");
                String eMsg = String.format("\t\t\t&c%s&f#&7%s&8 (&cat &7%s&f:&b%d&8)%n", clazz, method, file, line);
                if (logFunction != null) {
                    logFunction.apply(eMsg);
                } else {
                    System.out.println(ConsoleColor.parse(eMsg));
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
        if (logger != null) {
            logger.send(message, replaces);
            return;
        }

        if (replaces.length >= 1) {
            Object firstReplace = replaces[0];
            if (firstReplace instanceof LogLevel) {
                LogLevel level = (LogLevel) firstReplace;
                Object[] finalReplaces = Arrays.copyOfRange(replaces, 1, replaces.length);

                send(level, message, finalReplaces);
                return;
            }
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
            logger.log(message, replaces);
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
    public APISource owner() {
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
