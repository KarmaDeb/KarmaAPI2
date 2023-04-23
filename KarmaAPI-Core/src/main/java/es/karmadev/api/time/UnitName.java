package es.karmadev.api.time;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporal unit names
 */
public class UnitName {

    private final Map<TemporalUnit, String> names = new ConcurrentHashMap<>();

    /**
     * Initialize the time name
     */
    UnitName() {
        for (TemporalUnit unit : TemporalUnit.values())
            names.put(unit, unit.getUnit());
    }


    /**
     * Add a custom karma unit name
     *
     * @param unit the unit
     * @param name the unit name
     * @return this instance
     */
    public UnitName add(final TemporalUnit unit, final String name) {
        names.put(unit, name);

        return this;
    }

    /**
     * Get a custom unit name
     *
     * @param unit the unit
     * @return the unit name
     */
    public String get(final TemporalUnit unit) {
        return names.getOrDefault(unit, unit.getUnit());
    }

    /**
     * Create a new time name instance
     *
     * @return a new time name instance
     */
    public static UnitName create() {
        return new UnitName();
    }
}
