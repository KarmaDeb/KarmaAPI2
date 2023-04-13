package es.karmadev.api.logger.log.file.component.header;

import java.util.function.Supplier;

/**
 * KarmaAPI log header line
 */
public class HeaderLine {

    private final Supplier<String> onRequest;
    private boolean breakLine = true;

    /**
     * Create a header line
     */
    public HeaderLine() {
        this((Supplier<String>) null);
    }

    /**
     * Create a header line
     *
     * @param raw the raw line
     */
    public HeaderLine(final String raw) {
        this(() -> raw);
    }

    /**
     * Create a header line
     *
     * @param onRequest when the line is requested
     */
    public HeaderLine(final Supplier<String> onRequest) {
        this.onRequest = onRequest;
    }

    /**
     * Set if this is a line breaking
     *
     * @param br if this line is line breaking
     */
    public HeaderLine lineBreak(final boolean br) {
        breakLine = br;
        return this;
    }

    /**
     * Get the line content
     *
     * @return the line contents
     */
    public String get() {
        if (onRequest != null) return onRequest.get() + (breakLine ? "<br>" : "");
        return (breakLine ? "<br>" : "");
    }
}
