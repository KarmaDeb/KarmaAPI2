package es.karmadev.api.object.var.reference;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.object.var.Reference;

import java.io.Closeable;
import java.lang.ref.WeakReference;

/**
 * Represents a not null reference. A not null
 * reference value can still be null, but the
 * method {@link #isSet()} will return false if
 * the value is null.
 *
 * @param <T> the reference type
 */
public class NotNullReference<T> implements Reference<T> {

    private WeakReference<T> object;

    /**
     * Create the reference
     *
     * @param object the reference object
     */
    public NotNullReference(final T object) {
        this.object = new WeakReference<>(object);
    }

    /**
     * Get the reference value
     *
     * @return the value
     */
    @Override
    public T get() {
        return object.get();
    }

    /**
     * Get if the reference is set
     *
     * @return if the reference is
     * set
     */
    @Override
    public boolean isSet() {
        return !object.isEnqueued() && object.get() != null;
    }

    /**
     * Set the reference
     *
     * @param object the new value
     */
    @Override
    public void set(final T object) {
        T current = this.object.get();
        if (current instanceof Closeable) {
            Closeable closeable = (Closeable) current;

            try {
                closeable.close();
            } catch (Exception ex) {
                ExceptionCollector.catchException(current.getClass(), ex);
            }
        }

        this.object.clear();
        this.object.enqueue();

        this.object = new WeakReference<>(object);
    }
}
