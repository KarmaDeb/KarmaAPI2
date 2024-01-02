package es.karmadev.api.minecraft.component.exception;

/**
 * This exception is thrown when a formatting
 * error occurs. For instance, when a mime-colored
 * message gets parsed with invalid mime tag format
 */
public class FormattingException extends RuntimeException {

    /**
     * Initialize the exception
     *
     * @param column the column (in the text) which caused
     *               the exception
     * @param character the character which caused the exception
     * @param text the text
     */
    public FormattingException(final int column, final char character, final String text) {
        super("Unexpected character " + character + " at column " + column + ". Failed to parse: " + text);
    }
}
