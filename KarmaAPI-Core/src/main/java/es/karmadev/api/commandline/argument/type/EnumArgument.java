package es.karmadev.api.commandline.argument.type;

import es.karmadev.api.commandline.argument.AbstractArgument;
import es.karmadev.api.commandline.argument.ClassArgument;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * Represents an argument
 */
public class EnumArgument<T extends Enum<?>> extends AbstractArgument<T> {

    /**
     * Initialize the argument
     *
     * @param key         the argument key
     * @param description the argument description
     * @param help        the argument help message
     */
    EnumArgument(final String key, final String description, final String help, final Class<T> e) {
        this(key, description, help, false, e);
    }

    /**
     * Initialize the argument
     *
     * @param key         the argument key
     * @param description the argument description
     * @param help        the argument help message
     * @param isSwitch    the argument switch status
     */
    EnumArgument(final String key, final String description, final String help, final boolean isSwitch, final Class<T> e) {
        super(key, description, help, isSwitch, e);
    }

    /**
     * Converse from string value
     *
     * @param value the value
     * @return the typed value
     */
    @Override
    public Optional<T> converse(final Object value) {
        T v = null;
        for (T constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(String.valueOf(value))) {
                v = constant;
                break;
            }

            try {
                int id = (int) value;
                if (constant.ordinal() == id) {
                    v = constant;
                    break;
                }
            } catch (ClassCastException ex) {
                try {
                    int id = Integer.parseInt(String.valueOf(value));
                    if (constant.ordinal() == id) {
                        v = constant;
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

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
    public void mapArgument(final Object instance, final T value, final boolean safe) {
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
    public static <T extends Enum<?>> EnumArgument<T> valueOf(final Class<T> clazz, final String key) {
        return valueOf(clazz, key, "", "", false);
    }

    /**
     * Create a new argument
     * 
     * @param key the key
     * @param description the description
     * @return the argument
     */
    public static <T extends Enum<?>> EnumArgument<T> valueOf(final Class<T> clazz, final String key, final String description) {
        return valueOf(clazz, key, description, "", false);
    }

    /**
     * Create a new argument
     * 
     * @param key the key
     * @param description the description
     * @param help the help message
     * @return the argument
     */
    public static <T extends Enum<?>> EnumArgument<T> valueOf(final Class<T> clazz, final String key, final String description, final String help) {
        return valueOf(clazz, key, description, help, false);
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @param isSwitch the switch status
     * @return the argument
     */
    public static <T extends Enum<?>> EnumArgument<T> valueOf(final Class<T> clazz, final String key, final boolean isSwitch) {
        return valueOf(clazz, key, "", "", isSwitch);
    }

    /**
     * Create a new argument
     *
     * @param key the key
     * @param description the description
     * @param isSwitch the switch status
     * @return the argument
     */
    public static <T extends Enum<?>> EnumArgument<T> valueOf(final Class<T> clazz, final String key, final String description, final boolean isSwitch) {
        return valueOf(clazz, key, description, "", isSwitch);
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
    public static <T extends Enum<?>> EnumArgument<T> valueOf(final Class<T> clazz, final String key, final String description, final String help, final boolean isSwitch) {
        return new EnumArgument<>(key, description, help, isSwitch, clazz);
    }
}
