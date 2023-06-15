package es.karmadev.api.object;

import java.util.function.Consumer;

/**
 * Object utilities
 */
@SuppressWarnings("unused")
public class ObjectUtils {

    /**
     * Check if the objects equals ignoring
     * specific conditions. For example, this will ignore
     * strings uppercase/lowercase
     *
     * @param object1 the first object to check
     * @param object2 the second object to check
     * @return if the objects are the same
     */
    public static boolean equalsIgnoreCase(final Object object1, final Object object2) {
        if (object1 == null && object2 == null) return true; //Both objects are null
        if (object1 == null || object2 == null) {
            return String.valueOf(object1).equalsIgnoreCase(String.valueOf(object2)); //We check if objectX equals "null" (maybe?)
        }

        CharSequence main = null;
        CharSequence slave = null;
        if (object1 instanceof CharSequence) {
            main = (CharSequence) object1;

            if (object2 instanceof CharSequence) {
                slave = (CharSequence) object2;
            }
            if (object2 instanceof Character) {
                main = main.subSequence(0, 1);
                slave = String.valueOf(object2);
            }
        }
        if (object2 instanceof CharSequence) {
            main = (CharSequence) object2;

            if (object1 instanceof CharSequence) {
                slave = (CharSequence) object1;
            }
            if (object1 instanceof Character) {
                main = main.subSequence(0, 1);
                slave = String.valueOf(object1);
            }
        }

        if (main != null && slave != null) return main.toString().equalsIgnoreCase(slave.toString());
        Character principal = null;
        Character secondary = null;
        if (object1 instanceof Character) {
            principal = (Character) object1;

            if (object2 instanceof Character) {
                secondary = (Character) object2;
            }
            if (object2 instanceof Number) {
                Number number = (Number) object2;
                int charCode = number.intValue();

                secondary = (char) charCode;
            }
        }
        if (object2 instanceof Character) {
            principal = (Character) object2;

            if (object1 instanceof Character) {
                secondary = (Character) object1;
            }
            if (object1 instanceof Number) {
                Number number = (Number) object1;
                int charCode = number.intValue();

                secondary = (char) charCode;
            }
        }

        if (principal != null && secondary != null) return principal.toString().equalsIgnoreCase(secondary.toString());
        if (object1 == object2) return true; //They are in the same memory slot (I guess?)

        //From here, any of the objects are null
        Class<?> object1clazz = object1.getClass();
        Class<?> object2clazz = object2.getClass();

        if (object1clazz.equals(object2clazz)) return true;
        if (object1clazz.isAssignableFrom(object2clazz) || object2clazz.isAssignableFrom(object1clazz)) return true;
        if (object1clazz.isInstance(object2clazz) || object2clazz.isInstance(object1clazz)) return true;

        return object1.equals(object2);
    }

    /**
     * Check if the objects equals ignoring
     * specific conditions. For example, this will ignore
     * strings uppercase/lowercase
     *
     * @param object1 the first object to check
     * @param object2 the second object to check
     * @param executor the action to perform
     */
    public static void equalsIgnoreCase(final Object object1, final Object object2, final Consumer<Boolean> executor) {
        executor.accept(equalsIgnoreCase(object1, object2));
    }

    /**
     * Assert that an object is not null or empty
     *
     * @param object the object
     * @param message the message
     * @throws NullPointerException if the object is null or empty
     */
    public static void assertNullOrEmpty(final Object object, final String message) throws NullPointerException {
        if (object == null) throw new NullPointerException(message);
        String str = String.valueOf(object);

        if (str.replaceAll("\\s", "").isEmpty()) throw new NullPointerException(message);
    }

    /**
     * Assert that the objects are not null or empty
     *
     * @param objects the objects
     * @param message the message
     * @param checkAll check all the objects
     * @throws NullPointerException if the objects are null or empty
     */
    public static void assertNullOrEmpty(final Object[] objects, final String message, final boolean checkAll) throws NullPointerException {
        int nulls = 0;
        for (Object object : objects) {
            if (object == null) {
                if (checkAll) {
                    nulls++;
                    continue;
                } else {
                    throw new NullPointerException(message);
                }
            }

            String str = String.valueOf(object);
            if (str.replaceAll("\\s", "").isEmpty()) {
                if (checkAll) {
                    nulls++;
                } else {
                    throw new NullPointerException(message);
                }
            }
        }

        if (nulls == objects.length && objects.length > 0) throw new NullPointerException(message);
    }

    /**
     * Check if an object is null or empty
     *
     * @param object the object
     * @return if the object is null or empty
     */
    public static boolean isNullOrEmpty(final Object object) {
        try {
            assertNullOrEmpty(object, "");
            return false;
        } catch (NullPointerException ex) {
            return true;
        }
    }

    /**
     * Check if the objects are null or empty
     *
     * @param checkAll check all the objects
     * @param objects the objects
     * @return if the objects are null or empty
     */
    public static boolean areNullOrEmpty(final boolean checkAll, final Object... objects) {
        try {
            assertNullOrEmpty(objects, "", checkAll);
            return false;
        } catch (NullPointerException ex) {
            return true;
        }
    }
}
