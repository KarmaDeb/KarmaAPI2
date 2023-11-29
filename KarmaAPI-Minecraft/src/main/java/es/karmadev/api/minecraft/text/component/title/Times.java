package es.karmadev.api.minecraft.text.component.title;

import es.karmadev.api.strings.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to help
 * with title times
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Times {

    /**
     * Do not modify times
     */
    public final static Times NONE = new Times(0);

    /**
     * Default fadeIn value
     */
    public final static Times DEFAULT_FADE_IN = new Times(10);

    /**
     * Default stay value
     */
    public final static Times DEFAULT_STAY = new Times(70);

    /**
     * Default fadeOut value
     */
    public final static Times DEFAULT_FADE_OUT = new Times(20);

    private final long ticks;

    /**
     * Create a times element from
     * an exact tick
     *
     * @param tick the tick
     * @return the times
     */
    public static Times exact(final long tick) {
        return new Times(tick);
    }

    /**
     * Create a time
     *
     * @param time the time
     * @param unit the time unit
     * @return the time
     */
    public static Times createTime(final long time, final TimeUnit unit) {
        long ms = TimeUnit.MILLISECONDS.convert(time, unit);
        return new Times(Math.max(1, ms / 50));
    }

    /**
     * Parse the string into a
     * times instance
     *
     * @param raw the raw content
     * @param type the times type
     * @return the times instance
     */
    public static Times parse(final String raw, final TimesType type) {
        if (!raw.toLowerCase().matches("(?<fiTime>[0-9]+(?<fiUnit>ms|s|m|h|d)?|reset|default|none)")) {
            return null;
        }

        if (raw.toLowerCase().matches("(reset|default|none)")) {
            if (raw.equalsIgnoreCase("none")) {
                return NONE;
            } else {
                switch (type) {
                    case FADE_IN:
                        return DEFAULT_FADE_IN;
                    case STAY:
                        return DEFAULT_STAY;
                    case FADE_OUT:
                    default:
                        return DEFAULT_FADE_OUT;
                }
            }
        }

        long time = StringUtils.extractNumbers(raw)[0].longValue();
        String unit = raw.replace(String.valueOf(time), "");

        TimeUnit tm = null;
        switch (unit.toLowerCase()) {
            case "ms":
                tm = TimeUnit.MILLISECONDS;
                break;
            case "s":
                tm = TimeUnit.SECONDS;
                break;
            case "m":
                tm = TimeUnit.MINUTES;
                break;
            case "h":
                tm = TimeUnit.HOURS;
                break;
            case "d":
                tm = TimeUnit.DAYS;
                break;
            default:
                break;
        }

        if (tm == null) return new Times(time);
        return createTime(time, tm);
    }

    /**
     * Get the tick as a human
     * format time unit
     *
     * @param unit the time unit
     * @return the tick as a human
     * format
     */
    public long toTime(final TimeUnit unit) {
        long ms = unit.convert(this.ticks * 50, TimeUnit.MILLISECONDS);
        return unit.convert(ms, TimeUnit.MILLISECONDS);
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
        return String.valueOf(ticks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Times)) return false;

        Times times = (Times) o;
        return ticks == times.ticks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticks);
    }

    /**
     * Times type
     */
    public enum TimesType {
        /**
         * Fade in
         */
        FADE_IN,
        /**
         * Stay
         */
        STAY,
        /**
         * Fade out
         */
        FADE_OUT
    }
}
