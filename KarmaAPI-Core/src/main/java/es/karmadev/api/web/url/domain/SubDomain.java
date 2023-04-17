package es.karmadev.api.web.url.domain;

/**
 * Subdomain
 */
public class SubDomain {

    private final String[] sub;

    /**
     * Initialize the subdomain
     *
     * @param sub the subdomain
     */
    public SubDomain(final String... sub) {
        String[] reversed = new String[sub.length];

        for (int i = 0; i < sub.length; i++) {
            reversed[i] = sub[sub.length - 1 - i];
        }

        this.sub = reversed;
    }

    /**
     * Get the subdomain level
     *
     * @param level the level
     * @return the subdomain level
     */
    public String getLevel(final int level) {
        if (sub.length == 0) return "";
        return sub[Math.max(0, Math.min(sub.length - 1, level))];
    }

    /**
     * Get the subdomain levels
     *
     * @return the subdomain levels
     */
    public int getLevels() {
        return sub.length;
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder subBuilder = new StringBuilder();
        for (int i = sub.length - 1; i >= 0; i--) {
            String sub = this.sub[i];
            subBuilder.append(sub).append((i != 0) ? "." : "");
        }

        return subBuilder.toString();
    }
}
