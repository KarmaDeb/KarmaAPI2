package es.karmadev.api.time;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporal units
 */
@SuppressWarnings("unused")
public enum TemporalUnit {
    /**
     * Single millisecond
     */
    MILLISECOND("ms", "milli", "s_milli"),
    /**
     * More than 1 millisecond
     */
    MILLISECONDS("ms", "millis", "p_milli"),
    /**
     * Single second
     */
    SECOND("sec", "second", "s_second"),
    /**
     * More than 1 second
     */
    SECONDS("seconds", "seconds", "p_second"),
    /**
     * Single minute
     */
    MINUTE("min", "minute", "s_minute"),
    /**
     * More than 1 minute
     */
    MINUTES("minutes", "minutes", "p_minute"),
    /**
     * Single hour
     */
    HOUR("h", "hour", "s_hour"),
    /**
     * More than 1 hour
     */
    HOURS("hours", "hours", "p_hour"),
    /**
     * Single day
     */
    DAY("d", "day", "s_day"),
    /**
     * More than 1 day
     */
    DAYS("days", "days", "p_day"),
    /**
     * Single week
     */
    WEEK("w", "week", "s_week"),
    /**
     * More than 1 week
     */
    WEEKS("weeks", "weeks", "p_week"),
    /**
     * Single month
     */
    MONTH("month", "month", "s_month"),
    /**
     * More than 1 month
     */
    MONTHS("months", "months", "p_month"),
    /**
     * Single year
     */
    YEAR("year", "year", "s_year"),
    /**
     * More than 1 year
     */
    YEARS("years", "years", "p_year");

    /**
     * The unit name
     */
    private final String unit;
    private final String[] alias;
    private final static Map<String, TemporalUnit> aliases = new ConcurrentHashMap<>();

    static {
        for (TemporalUnit tempUnit : TemporalUnit.values()) {
            for (String alias : tempUnit.alias) {
                aliases.put(alias, tempUnit);
            }
        }
    }

    /**
     * Initialize the karma unit
     *
     * @param name the unit name
     */
    TemporalUnit(final String name, final String... aliases) {
        unit = name;
        this.alias = aliases;
    }

    /**
     * Get the unit name
     *
     * @return the unit name
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Get the unit from the alias
     *
     * @param alias the alias
     * @return the temporal unit
     */
    @Nullable
    public static TemporalUnit fromAlias(final String alias) {
        return aliases.getOrDefault(alias, null);
    }

    /**
     * Get the unit from the alias
     *
     * @param alias the alias
     * @param value the value to get unit for
     * @return the temporal unit
     */
    @Nullable
    public static TemporalUnit fromAlias(final String alias, final long value) {
        String a;
        if (value == 1) {
            a = "s_" + alias;
        } else {
            a = "p_" + alias;
        }

        return aliases.getOrDefault(a, null);
    }
}
