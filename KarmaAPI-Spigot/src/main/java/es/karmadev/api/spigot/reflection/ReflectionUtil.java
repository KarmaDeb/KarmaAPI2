package es.karmadev.api.spigot.reflection;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ReflectionUtil {

    /**
     * Get a constructor from a class
     *
     * @param clazz the class
     * @param args the constructor arguments
     * @return the constructor
     */
    @Nullable
    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... args) {
        try {
            Constructor<?> constructor;
            try {
                constructor = clazz.getDeclaredConstructor(args);
            } catch (NoSuchMethodException ex) {
                constructor = clazz.getConstructor(args);
            }

            return constructor;
        } catch (ReflectiveOperationException ignored) {}
        return null;
    }

    /**
     * Get a method from a class
     *
     * @param clazz the class
     * @param method the method name
     * @param args the method arguments
     * @return the method
     */
    public static Method getMethod(final Class<?> clazz, final String method, final Class<?>... args) {
        try {
            Method function;
            try {
                function = clazz.getDeclaredMethod(method, args);
            } catch (NoSuchMethodException ex) {
                function = clazz.getMethod(method, args);
            }

            return function;
        } catch (ReflectiveOperationException ignored) {}
        return null;
    }

    /**
     * Create an instance from a constructor
     *
     * @param constructor the constructor
     * @param args the constructor arguments
     * @return the instance
     */
    public static Object instantiate(final Constructor<?> constructor, final Object... args) {
        try {
            Object instance;
            try {
                instance = constructor.newInstance(args);
            } catch (IllegalAccessException ex) {
                constructor.setAccessible(true);
                instance = constructor.newInstance(args);
            }

            return instance;
        } catch (ReflectiveOperationException ignored) {}
        return null;
    }

    /**
     * Invoke a static method
     *
     * @param method the method
     * @param args the method arguments
     * @return the method result
     */
    public static Object invokeStatic(final Method method, final Object... args) {
        return invoke(method, method.getDeclaringClass(), args);
    }

    public static Object invoke(final Method method, final Object instance, final Object... args) {
        try {
            Object response = null;
            try {
                response = method.invoke(instance, args);
            } catch (IllegalAccessException ex) {
                method.setAccessible(true);
                response = method.invoke(instance, args);
            }

            return response;
        } catch (ReflectiveOperationException ignored) {}
        return null;
    }

    public static Enum<?> getEnumValue(final Class<?> enumClass, final String name) {
        if (!enumClass.isEnum()) return null;
        Object[] enums = enumClass.getEnumConstants();
        for (Object object : enums) {
            if (object instanceof Enum) {
                Enum<?> objectEnum = (Enum<?>) object;
                if (objectEnum.name().equalsIgnoreCase(name)) {
                    return objectEnum;
                }
            }
        }

        return null;
    }

    public static Object getEnumValue(final Class<?> enumClass, final int index) {
        if (!enumClass.isEnum() || index < 0) return null;
        Object[] enums = enumClass.getEnumConstants();

        for (int i = 0; i < enums.length; i++) {
            if (i > index) return null;

            Object object = enums[i];
            if (!(object instanceof Enum)) continue;

            Enum<?> e = (Enum<?>) object;
            if (i == index) return e;
        }

        return null;
    }
}
