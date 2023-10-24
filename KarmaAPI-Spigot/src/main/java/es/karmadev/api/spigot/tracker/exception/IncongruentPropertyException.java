package es.karmadev.api.spigot.tracker.exception;

import java.lang.reflect.Type;

/**
 * This exception is thrown when a custom
 * property is created but its nullable and
 * default values does not match
 */
public class IncongruentPropertyException extends IllegalStateException {

    /**
     * Initialize the exception
     *
     * @param name the property name
     * @param type the property type
     */
    public IncongruentPropertyException(final String name, final Type type) {
        super("Cannot create property " + name + " of type " + type.getTypeName() + " because it has been marked as not-null but its default value is null");
    }
}
