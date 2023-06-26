package es.karmadev.api.minecraft.client.exception;

/**
 * This exception is thrown when a field from {@link es.karmadev.api.minecraft.client.GlobalPlayer}
 * is tried to be fetched from a platform with does not allow
 * or does not provide a way to obtain the data
 */
public class NonAvailableException extends Exception {

    /**
     * Initialize the exception
     *
     * @param field the field name
     */
    public NonAvailableException(final String field) {
        super("Cannot obtain field " + field + " because current server platform and GlobalPlayer does not support it");
    }
}
