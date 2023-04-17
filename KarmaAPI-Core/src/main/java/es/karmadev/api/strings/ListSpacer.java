package es.karmadev.api.strings;

/**
 * List separation for {@link StringUtils string utilities}
 */
public enum ListSpacer {
    /**
     * Do not apply any separation
     */
    NONE,
    /**
     * Append a new line for each element
     */
    NEW_LINE,
    /**
     * Append a comma for each element
     */
    COMMA
}
