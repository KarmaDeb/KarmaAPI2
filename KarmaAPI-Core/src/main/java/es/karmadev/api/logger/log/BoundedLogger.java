package es.karmadev.api.logger.log;

import es.karmadev.api.core.config.APIConfiguration;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.logger.log.file.LogFile;
import lombok.SneakyThrows;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import java.util.logging.*;

/**
 * KarmaAPI bounded logger
 */
@SuppressWarnings("unused")
public class BoundedLogger extends Logger implements SourceLogger {

    private final static APIConfiguration config = new APIConfiguration();

    private final APISource source;
    private final LogFile log;
    private boolean logToConsole;
    private Function<String, Void> logFunction;

    /**
     * Initialize the console
     *
     * @param owner the console owner
     */
    public BoundedLogger(final APISource owner) {
        super("KarmaAPI - Logger", null);
        this.source = owner;
        log = new LogFile(source);

        BoundedConsoleHandler handler = new BoundedConsoleHandler();
        addHandler(handler) ;
    }

    /**
     * Override the log function
     *
     * @param function the new log function
     * @return the modified logger
     */
    @Override
    public BoundedLogger overrideLogFunction(final Function<String, Void> function) {
        this.logFunction = function;
        return this;
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
            String finalMessage = buildMessage(level, source.sourceName(), message, replaces);
            if (logFunction != null) {
                logFunction.apply(finalMessage);
                return;
            }

            doLog(finalMessage);
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
            String finalMessage = buildMessage(LogLevel.ERROR, source.sourceName(), message + " &7(&b " + error.getClass().getCanonicalName() + ": " + error.getMessage() + " &7)", replaces);

            Throwable cause = error.getCause();
            if (error.getSuppressed().length > 0) {
                finalMessage += "&b AND " + (error.getSuppressed().length + (cause != null ? 1 : 0)) + " MORE";
            }

            if (logFunction != null) {
                logFunction.apply(finalMessage);
            } else {
                doLog(finalMessage);
            }

            for (StackTraceElement element : elements) {
                String clazz = element.getClassName();
                String method = element.getMethodName();
                String file = element.getFileName();
                int line = element.getLineNumber();

                if (file == null) {
                    String eMsg = String.format("\t\t\t&c%s&8&c&f#&7%s&8 (&cat line &b%d&8)", clazz, method, line);
                    if (logFunction != null) {
                        logFunction.apply(eMsg);
                    } else {
                        doLog(eMsg);
                    }
                } else {
                    file = file.replace(".java", "");
                    String eMsg = String.format("\t\t\t&c%s&f#&7%s&8 (&cat &7%s&f:&b%d&8)", clazz, method, file, line);
                    if (logFunction != null) {
                        logFunction.apply(eMsg);
                    } else {
                        doLog(eMsg);
                    }
                }
            }

            /*
            We should log instead of displaying the whole error
            thing
            for (Throwable sup : error.getSuppressed()) {
                send(sup, message, replaces);
            }*/
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
        if (replaces.length >= 1) {
            Object firstReplace = replaces[0];
            if (firstReplace instanceof LogLevel) {
                LogLevel level = (LogLevel) firstReplace;
                Object[] finalReplaces = Arrays.copyOfRange(replaces, 1, replaces.length);

                send(level, message, finalReplaces);
                return;
            }
        }

        String finalMessage = buildMessage(null, source.sourceName(), message, replaces);
        if (logFunction != null) {
            logFunction.apply(finalMessage);
            return;
        }

        doLog(finalMessage);
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
        for (Throwable sup : error.getSuppressed()) {
            log(sup, message, replaces);
        }
    }

    /**
     * Log a message into the console
     *
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void log(final String message, final Object... replaces) {
        if (replaces.length >= 1) {
            Object firstReplace = replaces[0];
            if (firstReplace instanceof LogLevel) {
                LogLevel level = (LogLevel) firstReplace;
                Object[] finalReplaces = Arrays.copyOfRange(replaces, 1, replaces.length);

                log(level, message, finalReplaces);
                return;
            }
        }
        
        if (logToConsole)
            send(message, replaces);

        log.append(LogLevel.INFO, parseReplaces(message, replaces));
    }

    /**
     * Log a LogRecord.
     * <p>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    @Override
    public void log(final LogRecord record) {
        Filter theFilter = getFilter();
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return;
        }

        Logger logger = this;
        final Handler[] loggerHandlers = logger.getHandlers();

        for (Handler handler : loggerHandlers) {
            handler.publish(record);
            handler.flush();
        }
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
    private String buildMessage(final LogLevel level, final String name, final String message, final Object... replaces) {
        String finalMessage = parseReplaces(message, replaces);

        APIConfiguration config = new APIConfiguration();
        String prefix = config.getPrefix(level).replace("{0}", name);

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
            String prefix = "{" + i + "}";
            Object value = replaces[i];

            finalMessage = finalMessage.replace(prefix, String.valueOf(value));
        }

        return finalMessage;
    }

    protected void doLog(final String message) {
        LogRecord record = new LogRecord(Level.ALL, message);
        record.setLoggerName("KarmaAPI - Logger");
        log(record);
    }
}

class BoundedConsoleHandler extends Handler {

    private final static Writer writer = new OutputStreamWriter(System.err, StandardCharsets.UTF_8);

    /**
     * Format and publish a <tt>LogRecord</tt>.
     * <p>
     * The <tt>StreamHandler</tt> first checks if there is an <tt>OutputStream</tt>
     * and if the given <tt>LogRecord</tt> has at least the required log level.
     * If not it silently returns.  If so, it calls any associated
     * <tt>Filter</tt> to check if the record should be published.  If so,
     * it calls its <tt>Formatter</tt> to format the record and then writes
     * the result to the current output stream.
     * <p>
     * If this is the first <tt>LogRecord</tt> to be written to a given
     * <tt>OutputStream</tt>, the <tt>Formatter</tt>'s "head" string is
     * written to the stream before the <tt>LogRecord</tt> is written.
     *
     * @param record description of the log event. A null record is
     *               silently ignored and is not published
     */
    @Override @SneakyThrows
    public synchronized void publish(final LogRecord record) {
        writer.write(ConsoleColor.parse(record.getMessage()) + ConsoleColor.RESET.toOsCode() + "\n");
    }

    /**
     * Flush any buffered output.
     */
    @Override @SneakyThrows
    public void flush() {
        writer.flush();
    }

    /**
     * Close the <tt>Handler</tt> and free all associated resources.
     * <p>
     * The close method will perform a <tt>flush</tt> and then close the
     * <tt>Handler</tt>.   After close has been called this <tt>Handler</tt>
     * should no longer be used.  Method calls may either be silently
     * ignored or may throw runtime exceptions.
     *
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    @Override @SneakyThrows
    public void close() throws SecurityException {
        throw new SecurityException("Cannot close");
    }
}