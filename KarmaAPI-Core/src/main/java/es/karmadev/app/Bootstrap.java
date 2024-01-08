package es.karmadev.app;

/**
 * Represents a bootstrap for
 * a class appender
 */
public abstract class Bootstrap {

    private final ClassAppender appender;

    /**
     * Create a new bootstrap
     * instance
     *
     * @param appender the class appender
     */
    public Bootstrap(final ClassAppender appender) {
        this.appender = appender;
    }

    /**
     * Initialize the bootstrap
     */
    public abstract void init();

    /**
     * Get the class appender
     *
     * @return the class appender
     */
    public final ClassAppender getAppender() {
        return appender;
    }
}
