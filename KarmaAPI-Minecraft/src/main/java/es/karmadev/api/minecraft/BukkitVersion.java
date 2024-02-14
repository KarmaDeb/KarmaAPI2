package es.karmadev.api.minecraft;

import es.karmadev.api.version.Version;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static es.karmadev.api.minecraft.MinecraftVersion.*;

/**
 * Represents bukkit version, used mostly for
 * reflection
 */
@Getter
@SuppressWarnings("unused")
public class BukkitVersion extends Version {

    public final static BukkitVersion v1_8_R1 = new BukkitVersion(v1_8_X, v1_8_2, 1);
    public final static BukkitVersion v1_8_R2 = new BukkitVersion(v1_8_3, v1_8_7, 2);
    public final static BukkitVersion v1_8_R3 = new BukkitVersion(v1_8_8, v1_8_9, 3);
    public final static BukkitVersion v1_9_R1 = new BukkitVersion(v1_9_X, v1_9_3, 1);
    public final static BukkitVersion v1_9_R2 = new BukkitVersion(v1_9_4, 2);
    public final static BukkitVersion v1_10_R1 = new BukkitVersion(v1_10_X, v1_10_2, 1);
    public final static BukkitVersion v1_11_R1 = new BukkitVersion(v1_11_X, v1_11_2, 1);
    public final static BukkitVersion v1_12_R1 = new BukkitVersion(v1_12_X, v1_12_2, 2);
    public final static BukkitVersion v1_13_R1 = new BukkitVersion(v1_13_X, 1);
    public final static BukkitVersion v1_13_R2 = new BukkitVersion(v1_13_1, v1_13_2, 2);
    public final static BukkitVersion v1_14_R1 = new BukkitVersion(v1_14_X, v1_14_4, 1);
    public final static BukkitVersion v1_15_R1 = new BukkitVersion(v1_15_X, v1_15_2, 1);
    public final static BukkitVersion v1_16_R1 = new BukkitVersion(v1_16_X, v1_16_1, 1);
    public final static BukkitVersion v1_16_R2 = new BukkitVersion(v1_16_2, v1_16_3, 2);
    public final static BukkitVersion v1_16_R3 = new BukkitVersion(v1_16_4, v1_16_5, 3);
    public final static BukkitVersion v1_17_R1 = new BukkitVersion(v1_17_X, v1_17_1, 1);
    public final static BukkitVersion v1_18_R1 = new BukkitVersion(v1_18_X, v1_18_1, 1);
    public final static BukkitVersion v1_18_R2 = new BukkitVersion(v1_18_2, v1_18_2, 2);
    public final static BukkitVersion v1_19_R1 = new BukkitVersion(v1_19_X, v1_19_2, 1);
    public final static BukkitVersion v1_19_R2 = new BukkitVersion(v1_19_3, v1_19_3, 2);
    public final static BukkitVersion v1_19_R3 = new BukkitVersion(v1_19_4, v1_19_4, 3);
    public final static BukkitVersion v1_20_R1 = new BukkitVersion(v1_20_X, v1_20_1, 1);
    public final static BukkitVersion v1_20_R2 = new BukkitVersion(v1_20_2, v1_20_2, 2);
    public final static BukkitVersion v1_20_R3 = new BukkitVersion(v1_20_3, v1_20_5, 3);
    public final static BukkitVersion FUTURE = new BukkitVersion(MinecraftVersion.of(0, 0, 0), 1);
    public final static BukkitVersion LEGACY = new BukkitVersion(MinecraftVersion.of(-1, -1, -1), 1);

    private static BukkitVersion[] values;

    /**
     * Get all the known minecraft
     * versions
     *
     * @return the known minecraft versions
     */
    public static BukkitVersion[] values() {
        if (values == null) {
            List<BukkitVersion> collected = new ArrayList<>();

            Field[] fields = BukkitVersion.class.getDeclaredFields();
            for (Field f : fields) {
                Class<?> fType = f.getType();
                if (!fType.equals(BukkitVersion.class)) continue;
                if (f.isAnnotationPresent(ApiStatus.Internal.class)) continue;

                try {
                    Object value = f.get(null);
                    if (value.equals(FUTURE) || value.equals(LEGACY)) continue;

                    collected.add((BukkitVersion) value);
                } catch (Throwable ignored) {}
            }

            values = collected.toArray(new BukkitVersion[0]);
        }

        return values.clone();
    }

    /**
     * Get the current bukkit version
     *
     * @return the current version
     */
    @Nullable
    public static BukkitVersion getCurrent() {
        BukkitVersion[] versions = values();
        for (BukkitVersion bv : versions) {
            if (bv.isCurrent()) return bv;
        }

        return null;
    }

    /**
     * Get a version from its string value
     *
     * @param name the version string
     * @return the bukkit version
     */
    @Nullable
    public static BukkitVersion valueOf(final String name) {
        BukkitVersion[] versions = values();
        for (BukkitVersion bv : versions) {
            if (bv.toString().equals(name)) return bv;
        }

        return null;
    }

    /**
     * -- GETTER --
     *  Get the minecraft version this
     *  bukkit version supports to
     *
     * @return the max supported minecraft
     * version
     */
    private final MinecraftVersion to;
    /**
     * -- GETTER --
     *  Get the release version of this
     *  bukkit release
     *
     * @return the release version
     */
    private final String releaseVersion;

    /**
     * Initialize the version
     *
     * @param from the version to start from
     * @param releaseVersion the release version (ex: r1)
     */
    BukkitVersion(final MinecraftVersion from, final int releaseVersion) {
        this(from, from, releaseVersion);
    }

    /**
     * Initialize the version
     *
     * @param from the version to start from
     * @param to the version to end at
     * @param releaseVersion the release version (ex: r1)
     */
    BukkitVersion(final MinecraftVersion from, final MinecraftVersion to, final int releaseVersion) {
        super(from.getMayor(), from.getMinor(), from.getPatch(), null);
        this.to = to;
        this.releaseVersion = String.format("R%d", releaseVersion);
    }

    /**
     * Get if the version is hold
     * in this bukkit version
     *
     * @param other the version
     * @return if the version is from this
     * bukkit version
     */
    public boolean contains(final Version other) {
        int compareFrom = this.compareTo(other);
        int compareTo = to.compareTo(other);

        return compareFrom >= 0 && compareTo <= 0;
    }

    /**
     * Get the enum type of the
     * bukkit version
     *
     * @return the enum type
     */
    public VersionType toEnum() {
        return VersionType.valueOf(this.toString().toUpperCase());
    }

    /**
     * Get if the bukkit version is the
     * current server bukkit version
     *
     * @return if the version is the current
     * one
     */
    public boolean isCurrent() {
        String nmsVersion = Bukkit.getServer()
                .getClass()
                .getPackage()
                .getName()
                .split("\\.")[3];

        return this.toString().equalsIgnoreCase(nmsVersion);
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
        if (mayor == -1) return "legacy";
        if (mayor == 0) return "future";
        return String.format("%d_%d_%s", mayor, minor, releaseVersion);
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
        if (!(obj instanceof BukkitVersion)) return false;
        return super.equals(obj);
    }
}
