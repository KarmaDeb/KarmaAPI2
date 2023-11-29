package es.karmadev.api.cml.argument.type.numeric;

import es.karmadev.api.cml.argument.AbstractArgument;
import es.karmadev.api.cml.argument.ClassArgument;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * Represents an argument
 */
public class FloatArgument extends AbstractArgument<Float> {

    /**
     * Initialize the argument
     *
     * @param key         the argument key
     * @param description the argument description
     * @param help        the argument help message
     */
    FloatArgument(final String key, final String description, final String help) {
        this(key, description, help, false);
    }

    /**
     * Initialize the argument
     *
     * @param key         the argument key
     * @param description the argument description
     * @param help        the argument help message
     * @param isSwitch    the argument switch status
     */
    FloatArgument(final String key, final String description, final String help, final boolean isSwitch) {
        super(key, description, help, isSwitch, Float.class);
    }

    /**
     * Converse from string value
     *
     * @param value the value
     * @return the typed value
     */
    @Override
    public Optional<Float> converse(final Object value) {
        Float v = null;
        try {
            v = (Float) value;
        } catch (ClassCastException ignored) {}

        return Optional.ofNullable(v);
    }

    /**
     * Map the argument into the instance object
     * fields
     *
     * @param instance the object instance in where
     *                 to put the argument
     * @param value    the value to map
     * @param safe safely set the value, if true, the value
     *             won't write if null
     */
    @Override
    public void mapArgument(final Object instance, final Float value, final boolean safe) {
        if (value == null && safe) return;

        Class<?> instanceClass = instance.getClass();
        for (Field field : instanceClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                if (field.isAnnotationPresent(ClassArgument.class)) {
                    ClassArgument argument = field.getAnnotation(ClassArgument.class);
                    if (argument.name().equals(key)) {
                        try {
                            field.setAccessible(true);
                            field.set(instance, value);
                        } catch (IllegalAccessException ignored) {}
                    }
                }
            }
        }
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @return the argument
     */
    public static FloatArgument valueOf(final String key) {
        return valueOf(key, "", "", false);
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @param description the description
     * @return the argument
     */
    public static FloatArgument valueOf(final String key, final String description) {
        return valueOf(key, description, "", false);
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @param description the description
     * @param help the help message
     * @return the argument
     */
    public static FloatArgument valueOf(final String key, final String description, final String help) {
        return valueOf(key, description, help, false);
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @param isSwitch the switch status
     * @return the argument
     */
    public static FloatArgument valueOf(final String key, final boolean isSwitch) {
        return valueOf(key, "", "", isSwitch);
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @param description the description
     * @param isSwitch the switch status
     * @return the argument
     */
    public static FloatArgument valueOf(final String key, final String description, final boolean isSwitch) {
        return valueOf(key, description, "", isSwitch);
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @param description the description
     * @param help the help message
     * @param isSwitch the switch status
     * @return the argument
     */
    public static FloatArgument valueOf(final String key, final String description, final String help, final boolean isSwitch) {
        return new FloatArgument(key, description, help, isSwitch);
    }
}
