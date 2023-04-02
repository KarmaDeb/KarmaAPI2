package es.karmadev.api.logger.console;

import es.karmadev.api.core.config.APIConfiguration;
import es.karmadev.api.logger.LogLevel;
import es.karmadev.api.core.source.KarmaSource;

public class SourceConsole implements ConsoleLogger {

    private final static APIConfiguration config = new APIConfiguration();

    private final KarmaSource source;

    /**
     * Initialize the console
     *
     * @param owner the console owner
     */
    public SourceConsole(final KarmaSource owner) {
        this.source = owner;
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
        send(level, message, replaces);
        //TODO: Log in file
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
        send(error, message, replaces);
        //TODO: Log in file
    }

    /**
     * Log a message into the console
     *
     * @param message  the message
     * @param replaces the message replaces
     */
    @Override
    public void log(final String message, final Object... replaces) {
        send(message, replaces);
        //TODO: Log in file
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

        String finalMessage = message;
        for (int i = 0; i < replaces.length; i++) {
            String placeholder = "{" + i + "}";
            Object replace = replaces[i];
            finalMessage = finalMessage.replace(placeholder, String.valueOf(replace));
        }
        return prefix.replace("{1}", finalMessage);
    }
}
