package es.karmadev.api.object;

/**
 * Object utilities
 */
@SuppressWarnings("unused")
public class ObjectUtils {

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
