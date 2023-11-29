package es.karmadev.api.cml;

import es.karmadev.api.cml.argument.AbstractArgument;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Argument parser
 */
@SuppressWarnings("unused")
public class ArgumentParser {

    private final Map<AbstractArgument<?>, Object> valueMap = new HashMap<>();

    /**
     * Parse the arguments
     *
     * @param args the arguments
     * @return the argument
     * @throws IllegalArgumentException if the args are not able to be parsed
     */
    public static ArgumentParser parse(final String[] args, final AbstractArgument<?>... required) throws IllegalArgumentException {
        ArgumentParser instance = new ArgumentParser();

        for (int i = 0; i < args.length; i++) {
            String key = args[i];

            for (AbstractArgument<?> argument : required) {
                if (argument.getKey().equals(key)) {
                    if (argument.isSwitch()) {
                        instance.valueMap.put(argument, true);
                        continue;
                    }

                    if (i + 1 < args.length) {
                        String value = args[i + 1];

                        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                            if (!argument.getType().equals(Boolean.class)) {
                                throw new IllegalArgumentException("Cannot read argument " + key + " because required type does not match with provided type (" + argument.getType().getTypeName().toLowerCase() + ")");
                            }

                            boolean bool = Boolean.parseBoolean(value);
                            instance.valueMap.put(argument, bool);
                            continue;
                        }

                        if (value.startsWith("0x")) {
                            if (!argument.getType().equals(Byte.class)) {
                                throw new IllegalArgumentException("Cannot read argument " + key + " because required type does not match with provided type (" + argument.getType().getTypeName().toLowerCase() + ")");
                            }

                            String byteValue = value.substring(2);
                            try {
                                byte val = Byte.parseByte(byteValue);
                                instance.valueMap.put(argument, val);
                                continue;
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(ex);
                            }
                        }

                        if (argument.getType().equals(Short.class)) {
                            try {
                                short val = Short.parseShort(value);
                                instance.valueMap.put(argument, val);
                                continue;
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(ex);
                            }
                        }
                        if (argument.getType().equals(Integer.class)) {
                            try {
                                int val = Integer.parseInt(value);
                                instance.valueMap.put(argument, val);
                                continue;
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(ex);
                            }
                        }
                        if (argument.getType().equals(Long.class)) {
                            try {
                                long val = Long.parseLong(value);
                                instance.valueMap.put(argument, val);
                                continue;
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(ex);
                            }
                        }
                        if (argument.getType().equals(Double.class)) {
                            try {
                                double val = Double.parseDouble(value);
                                instance.valueMap.put(argument, val);
                                continue;
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(ex);
                            }
                        }
                        if (argument.getType().equals(Float.class)) {
                            try {
                                float val = Float.parseFloat(value);
                                instance.valueMap.put(argument, val);
                                continue;
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(ex);
                            }
                        }

                        if (argument.getType().equals(String.class)) {
                            instance.valueMap.put(argument, value);
                            continue;
                        }
                        if (argument.getType() instanceof Class<?>) {
                            Class<?> clazz = (Class<?>) argument.getType();

                            Object enumValue = argument.converse(value).orElse(null);
                            if (enumValue == null) {
                                StringBuilder constants = new StringBuilder();
                                for (Object constant : clazz.getEnumConstants()) {
                                    constants.append(constant.toString()).append(", ");
                                }

                                throw new IllegalArgumentException("Invalid argument value, expected enum constant (" + constants.substring(0, constants.length() - 2) + ") but got " + value);
                            }

                            instance.valueMap.put(argument, enumValue);
                            continue;
                        }

                        throw new IllegalArgumentException("Unsupported argument type: " + argument.getType().getTypeName().toLowerCase());
                    } else {
                        throw new IllegalArgumentException("Cannot read argument " + key + " value at index [expected/max] " + i + 1 + "/" + args.length);
                    }
                }
            }
        }

        for (AbstractArgument<?> argument : required) {
            if (!instance.valueMap.containsKey(argument) && argument.isSwitch()) {
                instance.valueMap.put(argument, false);
            }
        }

        return instance;
    }

    /**
     * Get an argument
     *
     * @param key the argument key
     * @return the argument
     * @param <T> the argument type
     * @throws UnsupportedOperationException if the argument type does not
     * match with the requested type
     */
    @SuppressWarnings("unchecked")
    public <T> T getArgument(final String key) throws UnsupportedOperationException {
        Optional<AbstractArgument<?>> filtered = valueMap.keySet().stream().filter((argument) -> argument.getKey().equals(key)).findAny();
        if (filtered.isPresent()) {
            try {
                AbstractArgument<?> instance = filtered.get();
                return (T) valueMap.get(instance);
            } catch (ClassCastException ex) {
                throw new UnsupportedOperationException(ex);
            }
        }

        return null;
    }

    /**
     * Get an argument
     *
     * @param key the argument key
     * @param orElse the default value
     * @return the argument
     * @param <T> the argument type
     */
    public <T> T getArgument(final String key, final T orElse) {
        try {
            return getArgument(key);
        } catch (UnsupportedOperationException ignored) {}

        return orElse;
    }

    /**
     * Get if the parser has an argument
     *
     * @param key the argument key
     * @param type the argument type
     * @return the argument
     */
    public boolean hasArgument(final String key, final Class<?> type) {
        Optional<AbstractArgument<?>> filtered = valueMap.keySet().stream().filter((argument) -> argument.getKey().equals(key)).findAny();
        return filtered.map(abstractArgument -> abstractArgument.getType().equals(type)).orElse(false);
    }

    /**
     * Map the instances with their
     * correspondent arguments
     *
     * @param safe safely parse
     * @param instances the instances
     */
    @SuppressWarnings("unchecked")
    public void map(final boolean safe, final Object... instances) {
        for (Object instance : instances) {
            for (AbstractArgument<?> argument : valueMap.keySet()) {
                if (argument.appliesTo(instance)) {
                    AbstractArgument<Object> casted = (AbstractArgument<Object>) argument;
                    Object value = getArgument(argument.getKey(), null);

                    casted.mapArgument(instance, value, safe);
                }
            }
        }
    }

    /**
     * Map the instances with their
     * correspondent arguments
     *
     * @param safeApplier safely parse
     * @param instances the instances
     */
    @SuppressWarnings("unchecked")
    public void map(final BiFunction<AbstractArgument<?>, Object, Boolean> safeApplier, final Object... instances) {
        for (Object instance : instances) {
            for (AbstractArgument<?> argument : valueMap.keySet()) {
                if (argument.appliesTo(instance)) {
                    AbstractArgument<Object> casted = (AbstractArgument<Object>) argument;
                    Object value = getArgument(argument.getKey(), null);

                    Boolean result = safeApplier.apply(argument, instance);
                    if (result == null) result = Boolean.TRUE; //Default, map safely

                    casted.mapArgument(instance, value, result);
                }
            }
        }
    }

    /**
     * Get all the passed arguments into the parser
     *
     * @return the parsed arguments
     */
    public Collection<AbstractArgument<?>> getPassedArguments() {
        return Collections.unmodifiableSet(valueMap.keySet());
    }
}
