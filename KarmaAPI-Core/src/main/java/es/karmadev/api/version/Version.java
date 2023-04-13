package es.karmadev.api.version;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Version object
 */
@SuppressWarnings("unused")
public class Version implements Comparable<Version> {

    @Getter
    private final int mayor;

    @Getter
    private final int minor;

    @Getter
    private final int patch;

    @Getter @Nullable
    private final String build;

    /**
     * Initialize the version
     *
     * @param mayor the mayor version
     * @param minor the minor version
     * @param patch the version patch
     * @param build the version build type
     */
    Version(final int mayor, final int minor, final int patch, final @Nullable String build) {
        this.mayor = mayor;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
    }

    /**
     * Create a new version comparator
     *
     * @return the version comparator
     */
    public static Comparator<Version> comparator() {
        return Version::compareTo;
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
    public String toString() {
        StringBuilder versionBuilder = new StringBuilder("v");
        versionBuilder.append(mayor).append(".").append(minor).append(".").append(patch);
        if (build != null) {
            versionBuilder.append("-").append(build);
        }

        return versionBuilder.toString();
    }

    /**
     * Parse the version string
     *
     * @param raw the raw version
     * @return the parsed version
     */
    public static Version parse(final String raw) {
        int mayor = 0;
        int minor = 0;
        int patch = 0;

        String p1;
        String p2 = null;
        String p3 = null;
        String build = null;

        if (raw.contains(".")) {
            String[] vData = raw.split("\\.");
            switch (vData.length) {
                case 1:
                    p1 = vData[0];
                    if (p1.contains("-")) {
                        String[] pData = p1.split("-");
                        build = p1.replaceFirst(p1 + "-", "");
                        p1 = p1.replace("-" + build, "");
                    }
                    break;
                case 2:
                    p1 = vData[0];
                    p2 = raw.replaceFirst(p1 + "\\.", "");
                    if (p2.contains("-")) {
                        build = p2.replaceFirst(p2 + "-", "");
                        p2 = p2.replace("-" + build, "");
                    }
                    break;
                case 3:
                default:
                    p1 = vData[0];
                    p2 = vData[1];
                    p3 = raw.replaceFirst(p1 + "\\." + p2 + "\\.", "");
                    if (p3.contains("-")) {
                        build = p3.replaceFirst(p3 + "-", "");
                        p3 = p3.replace("-" + build, "");
                    }
                    break;
            }
        } else {
            p1 = raw;
        }

        try {
            mayor = Integer.parseInt(p1);
        } catch (NumberFormatException ignored) {}
        if (p2 != null) {
            try {
                minor = Integer.parseInt(p2);
            } catch (NumberFormatException ignored) {
            }
        }
        if (p3 != null) {
            try {
                minor = Integer.parseInt(p3);
            } catch (NumberFormatException ignored) {
            }
        }

        return Version.of(mayor, minor, patch, build);
    }

    /**
     * Parse the version string
     *
     * @param raw the raw version
     * @param build the version build
     * @return the parsed version
     */
    public static Version parse(final String raw, final String build) {
        int mayor = 0;
        int minor = 0;
        int patch = 0;

        String p1;
        String p2 = null;
        String p3 = null;

        if (raw.contains(".")) {
            String[] vData = raw.split("\\.");
            switch (vData.length) {
                case 1:
                    p1 = vData[0];
                    break;
                case 2:
                    p1 = vData[0];
                    p2 = raw.replaceFirst(p1 + "\\.", "");
                    break;
                case 3:
                default:
                    p1 = vData[0];
                    p2 = vData[1];
                    p3 = raw.replaceFirst(p1 + "\\." + p2 + "\\.", "");
                    break;
            }
        } else {
            p1 = raw;
        }

        try {
            mayor = Integer.parseInt(p1);
        } catch (NumberFormatException ignored) {}
        if (p2 != null) {
            try {
                minor = Integer.parseInt(p2);
            } catch (NumberFormatException ignored) {
            }
        }
        if (p3 != null) {
            try {
                patch = Integer.parseInt(p3);
            } catch (NumberFormatException ignored) {
            }
        }

        return Version.of(mayor, minor, patch, build);
    }

    /**
     * Build a version
     *
     * @param mayor the version mayor
     * @param minor the version minor
     * @param patch the version patch
     * @return the version
     */
    public static Version of(final int mayor, final int minor, final int patch) {
        return Version.of(mayor, minor, patch, null);
    }

    /**
     * Build a version
     *
     * @param mayor the version mayor
     * @param minor the version minor
     * @param patch the version patch
     * @param build the version build type
     * @return the version
     */
    public static Version of(final int mayor, final int minor, final int patch, final String build) {
        return new Version(mayor, minor, patch, build);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     *
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param version the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull final Version version) {
        int compareMayor = Integer.compare(this.mayor, version.getMayor());
        if (compareMayor != 0) {
            return compareMayor;
        }

        int compareMinor = Integer.compare(this.minor, version.getMinor());
        if (compareMinor != 0) {
            return compareMinor;
        }

        return Integer.compare(this.patch, version.getPatch());
    }
}
