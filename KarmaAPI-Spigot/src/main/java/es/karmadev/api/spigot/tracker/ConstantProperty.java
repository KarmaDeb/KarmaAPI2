package es.karmadev.api.spigot.tracker;

import es.karmadev.api.spigot.entity.trace.ray.impl.SyncRayTrace;
import es.karmadev.api.spigot.tracker.exception.IncongruentPropertyException;
import es.karmadev.api.spigot.tracker.stand.TrackerStand;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constant tracker properties
 */
@SuppressWarnings("unused")
public final class ConstantProperty<Type> {

    private final static Map<Class<?>, List<ConstantProperty<?>>> assignments = new ConcurrentHashMap<>();
    private final static Set<ConstantProperty<?>> propagated = ConcurrentHashMap.newKeySet();

    public final static ConstantProperty<Double> TRACKER_RADIUS = new ConstantProperty<>("tracker_radius", 8d, false, Double.class);
    public final static ConstantProperty<Double> ROTATION_SMOOTHNESS = new ConstantProperty<>("tracker_smoothness", 0d, false, Double.class);
    public final static ConstantProperty<Double> ROTATION_FRAMES = new ConstantProperty<>("tracker_frames", 20d, false, Double.class);
    public final static ConstantProperty<Boolean> ALWAYS_TRACK = new ConstantProperty<>("tracker_nonstop", false, false, Boolean.class);
    public final static ConstantProperty<Boolean> TRACK_LOCK = new ConstantProperty<>("tracker_locked", true, false, Boolean.class);
    public final static ConstantProperty<Double> PRECISION = new ConstantProperty<>("precision", SyncRayTrace.MEDIUM_PRECISION, false, Double.class);
    public final static ConstantProperty<Double> TOLERANCE = new ConstantProperty<>("tolerance", 0.05d, false, Double.class);

    private final String rawName;
    private final Type deffault;
    private final boolean nullable;
    private final Class<Type> type;

    static {
        try {
            propagateInternals();
            TrackerStand.precacheAssignments();
        } catch (IllegalAccessException ignored) {}
    }

    /**
     * Propagate the internal properties
     *
     * @throws IllegalAccessException shouldn't be thrown
     */
    public static void propagateInternals() throws IllegalAccessException {
        Field[] fields = ConstantProperty.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(ConstantProperty.class)) {
                ConstantProperty<?> property = (ConstantProperty<?>) field.get(ConstantProperty.class);
                property.propagate();
            }
        }
    }

    /**
     * Initialize the constant property
     *
     * @param rawName the property name
     * @param deffault the default value
     * @param nullable if the property allows null values
     * @param type the property type
     * @throws IncongruentPropertyException if the exception properties don't match
     */
    private ConstantProperty(final String rawName, final Type deffault, final boolean nullable, final Class<Type> type) throws IncongruentPropertyException {
        this.rawName = rawName;
        this.deffault = deffault;
        this.nullable = nullable;
        this.type = type;

        if (!nullable && deffault == null) throw new IncongruentPropertyException(rawName, type);
    }

    /**
     * Set the value
     *
     * @param entity the entity to set the value to
     * @param value the value
     * @throws UnsupportedOperationException if the constant property key is null
     */
    public void set(final TrackerEntity entity, final Type value) throws UnsupportedOperationException {
        if (rawName == null) throw new UnsupportedOperationException("Cannot set property of null key");

        Optional<ConstantProperty<?>> property = propagated.stream().filter((pro) -> pro.rawName.equalsIgnoreCase(rawName)).findAny();
        if (property.isPresent()) {
            ConstantProperty<?> existing = property.get();
            if (!existing.equals(this)) {
                if (!existing.type.equals(type)) return;
                if (value == null && !existing.nullable) return;

                entity.setProperty(rawName, value);
                return;
            }

            if (value == null && !nullable) return;

            entity.setProperty(rawName, value);

            return;
        }

        Class<? extends TrackerEntity> entityClass = entity.getClass();
        ConstantProperty<?> assigned = assignments.get(entityClass).stream()
                .filter((existing) -> existing.rawName.equalsIgnoreCase(rawName))
                .findAny().orElse(null);

        if (assigned != null && !assigned.equals(this)) {
            if (!assigned.type.equals(type)) return;
            if (value == null && !assigned.nullable) return;

            entity.setProperty(rawName, value);
            return;
        }

        if (value == null && !nullable) return; //Prevent setting null values if not nullable
        entity.setProperty(rawName, value);
    }

    /**
     * Get the value
     *
     * @param entity the entity to get value from
     * @return the value
     */
    public Type get(final TrackerEntity entity) {
        return get(entity, deffault);
    }

    /**
     * Get the value
     *
     * @param entity the entity to get value from
     * @param deffault the default value if null or empty
     * @return the value
     */
    public Type get(final TrackerEntity entity, final Type deffault) {
        return entity.getProperty(rawName, deffault);
    }

    /**
     * Assign this property to a tracker
     *
     * @param tracker the tracker
     */
    public void assignTo(final Class<? extends TrackerEntity> tracker) {
        List<ConstantProperty<?>> assigned = assignments.computeIfAbsent(tracker, (ls) -> new ArrayList<>());
        if (assigned.stream().anyMatch((existing) -> existing.rawName.equalsIgnoreCase(rawName))) return;

        assigned.add(this);
        assignments.put(tracker, assigned);
    }

    /**
     * Propagate the property
     */
    private void propagate() {
        if (propagated.stream().anyMatch((pro) -> pro.rawName.equalsIgnoreCase(rawName))) return;
        propagated.add(this);
    }

    /**
     * Create a new property builder
     *
     * @param type the type
     * @return the property builder
     * @param <T> the property type
     */
    public static <T> ConstantPropertyBuilder<T> builder(final Class<T> type) {
        return new ConstantPropertyBuilder<>(type);
    }

    /**
     * Create a custom property
     *
     * @param name the property name
     * @param type the property type
     * @param <T> the property type
     * @return the property
     * @throws IncongruentPropertyException if the exception properties don't match
     */
    public static <T> ConstantProperty<T> customProperty(final String name, final Class<T> type) throws IncongruentPropertyException {
        return customProperty(name, null, true, type);
    }

    /**
     * Create a custom property
     *
     * @param name the property name
     * @param deffault the default value
     * @param type the property type
     * @param <T> the property type
     * @return the property
     * @throws IncongruentPropertyException if the exception properties don't match
     */
    public static <T> ConstantProperty<T> customProperty(final String name, final T deffault, final Class<T> type) throws IncongruentPropertyException {
        return customProperty(name, deffault, deffault == null, type);
    }

    /**
     * Create a custom property
     *
     * @param name the property name
     * @param deffault the default value
     * @param nullable if the property is nullable
     * @param type the property type
     * @param <T> the property type
     * @return the property
     * @throws IncongruentPropertyException if the exception properties don't match
     */
    public static <T> ConstantProperty<T> customProperty(final String name, final T deffault, final boolean nullable, final Class<T> type) throws IncongruentPropertyException {
        ConstantPropertyBuilder<T> builder = ConstantProperty.builder(type).name(name).whenNull(deffault);
        return (nullable ? builder.asNullable().build() : builder.build());
    }

    /**
     * Constant property builder
     * @param <Type> the property type
     */
    public final static class ConstantPropertyBuilder<Type> {

        private String name;
        private Type deffault;
        private boolean nullable = false;
        private final Class<Type> type;

        /**
         * Initialize the property builder
         *
         * @param type the property type
         */
        private ConstantPropertyBuilder(final Class<Type> type) {
            this.type = type;
        }

        /**
         * Set the property name
         *
         * @param name the property name
         * @return the property builder
         */
        public ConstantPropertyBuilder<Type> name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the default value if the
         * value is null
         *
         * @param deffault the value to replace for null
         * @return the property builder
         */
        public ConstantPropertyBuilder<Type> whenNull(final Type deffault) {
            this.deffault = deffault;
            return this;
        }

        /**
         * Set the property nullable option
         *
         * @return the property builder
         */
        public ConstantPropertyBuilder<Type> asNullable() {
            nullable = true;
            return this;
        }

        /**
         * Build the property
         *
         * @return the property
         * @throws IncongruentPropertyException if the exception properties don't match
         */
        public ConstantProperty<Type> build() throws IncongruentPropertyException {
            if (!nullable && deffault == null)
                throw new IncongruentPropertyException(name, type);

            return new ConstantProperty<>(name, deffault, nullable, type);
        }
    }
}
