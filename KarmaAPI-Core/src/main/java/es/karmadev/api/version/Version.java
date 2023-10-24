package es.karmadev.api.version;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;

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
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Version)) return false;
        Version other = (Version) obj;

        int mayor = other.mayor;
        int minor = other.minor;
        int patch = other.patch;
        String build = other.build;

        if (this.mayor != mayor) return false;
        if (this.minor != minor) return false;
        if (this.patch != patch) return false;
        if (this.build == null && build != null) return false;
        if (this.build != null && build == null) return false;
        if (this.build != null) return this.build.equals(build);

        return true;
    }

    /**
     * Parse the version string
     *
     * @param raw the raw version
     * @return the parsed version
     */
    public static Version parse(final String raw) {
        String version = raw;
        String build = null;
        if (raw.contains("-")) {
            String[] data = raw.split("-");
            version = data[0];
            build = raw.substring(version.length() + 1);
        }

        return parse(version, build);
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
