package es.karmadev.api.commandline.argument;

import es.karmadev.api.commandline.argument.type.*;
import es.karmadev.api.commandline.argument.type.numeric.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Represents an argument
 */
public abstract class AbstractArgument<T> {

    /**
     * The argument key
     */
    protected final String key;
    /**
     * The argument description
     */
    protected final String description;
    /**
     * The argument help message
     */
    protected final String help;
    /**
     * The argument switch status
     */
    protected final boolean isSwitch;
    /**
     * The argument type, used later by
     * {@link #converse(Object)}
     */
    protected final Class<T> type;

    /**
     * Initialize the argument
     *
     * @param key the argument key
     * @param description the argument description
     * @param help the argument help message
     * @param isSwitch the argument switch status
     * @param type the argument type
     */
    protected AbstractArgument(final String key, final String description, final String help, final boolean isSwitch, final Class<T> type) {
        this.key = key;
        this.description = description;
        this.help = help;
        this.isSwitch = isSwitch;
        this.type = type;
    }

    /**
     * Get the key
     *
     * @return the key
     */
    public final String getKey() {
        return key;
    }

    /**
     * Get the description message
     *
     * @return the description message
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Get the help message
     *
     * @return the help message
     */
    public final String getHelp() {
        return help;
    }

    /**
     * Get if the argument is a switch, meaning it
     * doesn't have a value
     *
     * @return the argument switch status
     */
    public final boolean isSwitch() {
        return isSwitch;
    }

    /**
     * Get the type
     *
     * @return the type
     */
    public final Type getType() {
        return type;
    }

    /**
     * Converse from string value
     *
     * @param value the value
     * @return the typed value
     */
    public abstract Optional<T> converse(final Object value);

    /**
     * Get whether this argument applies to the specified instance
     *
     * @param instance the object instance
     * @return if the argument applies to the instance
     */
    public boolean appliesTo(final Object instance) {
        Class<?> instanceClass = instance.getClass();
        for (Field field : instanceClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                if (field.isAnnotationPresent(ClassArgument.class)) {
                    ClassArgument argument = field.getAnnotation(ClassArgument.class);
                    if (argument.name().equals(key) && field.getType().equals(type)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Map the argument into the instance object
     * fields
     *
     * @param instance the object instance in where
     *                 to put the argument
     * @param value the value to map
     * @param safe safely set the value, if true, the value
     *             won't write if null
     */
    public abstract void mapArgument(final Object instance, final T value, final boolean safe);

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@link
     *     #equals(Object) equals} method, then calling the {@code
     *     hashCode} method on each of the two objects must produce the
     *     same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link #equals(Object) equals} method, then
     *     calling the {@code hashCode} method on each of the two objects
     *     must produce distinct integer results.  However, the programmer
     *     should be aware that producing distinct integer results for
     *     unequal objects may improve the performance of hash tables.
     * </ul>
     *
     * @return a hash code value for this object.
     * @implSpec As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public final int hashCode() {
        return key.hashCode() + type.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     * @apiNote It is generally necessary to override the {@link #hashCode hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof AbstractArgument<?>)) return false;

        AbstractArgument<?> other = (AbstractArgument<?>) obj;
        return other.key.equals(key) && other.type.equals(type) && other.isSwitch == isSwitch;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     * @apiNote In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * The string output is not necessarily stable over time or across
     * JVM invocations.
     * @implSpec The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     */
    @Override
    public final String toString() {
        return "Argument[\n\t" + "key=\"" + key + "\",\n\t" +
                "description=\"" + description + "\",\n\t" +
                "help=\"" + help + "\",\n\t" +
                "switch=" + isSwitch + ",\n\t" +
                "type=" + type.getTypeName().toLowerCase() + "\n]";
    }

    /**
     * Get all the class arguments
     *
     * @param clazz the class arguments
     * @return the arguments
     * @throws IllegalStateException if the class has multiple arguments with the same
     * name
     */
    public static AbstractArgument<?>[] getFromClass(final Class<?> clazz) throws IllegalStateException {
        List<AbstractArgument<?>> arguments = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ClassArgument.class)) {
                ClassArgument arg = field.getAnnotation(ClassArgument.class);

                String name = arg.name();
                String description = arg.description();
                String help = arg.help();
                boolean isSwitch = arg.isSwitch();
                Class<?> type = field.getType();

                arguments.add(valueOf(name, description, help, isSwitch, type));
            }
        }

        return arguments.toArray(new AbstractArgument<?>[0]);
    }

    /**
     * Create an argument for a type
     *
     * @param key the key
     * @param description the description
     * @param help the help message
     * @param type the type
     * @param isSwitch the switch status
     * @return the argument
     * @param <T> the argument type
     * @throws IllegalArgumentException if the type is not supported
     */
    @SuppressWarnings("unchecked")
    public static <T> AbstractArgument<T> valueOf(final String key, final String description, final String help, final boolean isSwitch, final Class<T> type) throws IllegalArgumentException {
        if (type.equals(String.class)) {
            return (AbstractArgument<T>) StringArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return (AbstractArgument<T>) BooleanArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.equals(Byte.class) || type.equals(byte.class)) {
            return (AbstractArgument<T>) ByteArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.equals(Short.class) || type.equals(short.class)) {
            return (AbstractArgument<T>) ShortArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return (AbstractArgument<T>) IntegerArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            return (AbstractArgument<T>) LongArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.equals(Double.class) || type.equals(double.class)) {
            return (AbstractArgument<T>) DoubleArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.equals(Float.class) || type.equals(float.class)) {
            return (AbstractArgument<T>) FloatArgument.valueOf(key, description, help, isSwitch);
        }
        if (type.isEnum()) {
            return (AbstractArgument<T>) EnumArgument.valueOf(type.asSubclass(Enum.class), key, description, help, isSwitch);
        }

        throw new UnsupportedOperationException("Unsupported type: " + type.getTypeName().toLowerCase());
    }
}
