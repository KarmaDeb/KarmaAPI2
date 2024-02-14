package es.karmadev.api.minecraft.text.component.exception;

import es.karmadev.api.kson.JsonObject;

/**
 * This exception is thrown when a json
 * object is tried to be parsed into a component,
 * but that json object does not represent
 * a component
 */
public class NotComponentException extends IllegalArgumentException {

    /**
     * Initialize the exception
     *
     * @param json the json that has been
     *             tried to parse
     */
    public NotComponentException(final JsonObject json) {
        super("Cannot parse " + json + " as a component, because it is not a component!");
    }

    /**
     * Initialize the exception
     *
     * @param json the json that has been
     *             tried to parse
     */
    public NotComponentException(final String json) {
        super("Cannot parse " + json + " as a component, because it is not a component!");
    }
}
