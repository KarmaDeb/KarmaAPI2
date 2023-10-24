package es.karmadev.api.spigot.inventory.helper.exceptions;

/**
 * This error is thrown when a book inventory page is tried
 * to open whit empty pages
 */
public class EmptyBookException extends Exception {

    /**
     * Initializes the exception
     */
    public EmptyBookException() {
        super("Cannot open inventory book with empty pages for player");
    }
}
