package es.karmadev.api.object.var;

import es.karmadev.api.object.var.reference.NullableReference;

import java.util.function.Supplier;

class DefaultVariable<T> implements Variable<T> {

    private final Reference<T> reference;

    public DefaultVariable(final T object) {
        this.reference = new NullableReference<>(object);
    }

    public DefaultVariable(final Reference<T> reference) {
        this.reference = reference;
    }

    /**
     * Get the variable
     *
     * @return the variable
     */
    @Override
    public T get() {
        return reference.get();
    }

    /**
     * Get the variable, or get
     * the one supplied by the supplier
     *
     * @param def the default value
     * @return the variable
     */
    @Override
    public T get(final Supplier<T> def) {
        if (!reference.isSet()) return def.get();
        return reference.get();
    }

    /**
     * Get the variable, and set
     * the new value
     *
     * @param value the new value
     * @return the variable
     */
    @Override
    public T getAndSet(final Supplier<T> value) {
        T current = reference.get();
        reference.set(value.get());

        return current;
    }

    /**
     * Get the variable, or set the
     * value if the variable is null
     *
     * @param value the new value
     * @return the variable
     */
    @Override
    public T getOrSet(final Supplier<T> value) {
        T current = reference.get();
        if (current == null) {
            current = value.get();
            reference.set(current);
        }

        return current;
    }

    /**
     * Get the reference
     *
     * @return the reference
     */
    @Override
    public Reference<T> getReference() {
        return reference;
    }
}
