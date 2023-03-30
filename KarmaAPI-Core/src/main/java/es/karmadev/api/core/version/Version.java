package es.karmadev.api.core.version;

import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

/**
 * Version object
 */
public class Version {

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
    Version(final int mayor, final int minor, final int patch, final String build) {
        this.mayor = mayor;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
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
}
