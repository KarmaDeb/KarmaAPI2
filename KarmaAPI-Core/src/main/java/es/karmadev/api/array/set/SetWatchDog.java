package es.karmadev.api.array.set;

import es.karmadev.api.object.ObjectUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A watchdog for a set
 * @param <T> the set type
 */
public abstract class SetWatchDog<T> {

    protected final Set<T> set;
    protected Function<T, T> addAction;
    protected Function<T, T> removeAction;

    /**
     * Initialize the watchdog
     *
     * @param set the hashset to watchdog
     * @param addAction the action to perform when adding an item
     * @param removeAction the action to perform when removing an item
     */
    public SetWatchDog(final Set<T> set, final Function<T, T> addAction, final Function<T, T> removeAction) {
        this.set = set;
        this.addAction = addAction;
        this.removeAction = removeAction;
    }

    /**
     * Initialize the watchdog
     *
     * @param set the hashset to watchdog
     * @param addAction the action to perform when adding an item
     * @param removeAction the action to perform when removing an item
     */
    public SetWatchDog(final Set<T> set, final Consumer<T> addAction, final Consumer<T> removeAction) {
        this.set = set;
        this.addAction = (unused) -> {
            addAction.accept(unused);
            return unused;
        };
        this.removeAction = (unused) -> {
            removeAction.accept(unused);
            return unused;
        };
    }

    /**
     * Initialize the watchdog
     *
     * @param set the hashset to watchdog
     * @param addAction the action to perform when adding an item
     * @param removeAction the action to perform when removing an item
     */
    public SetWatchDog(final Set<T> set, final Runnable addAction, final Runnable removeAction) {
        this.set = set;
        this.addAction = (unused) -> {
            addAction.run();
            return unused;
        };
        this.removeAction = (unused) -> {
            removeAction.run();
            return unused;
        };
    }

    /**
     * Set the action to perform on add
     *
     * @param addAction the action to perform
     */
    public void onAdd(final Function<T, Boolean> addAction) {
        this.addAction = (element) -> {
            boolean proceed = addAction.apply(element);
            if (proceed) {
                return element;
            }

            return null;
        };
    }

    /**
     * Set the action to perform on remove
     *
     * @param removeAction the action to perform
     */
    public void onRemove(final Function<T, Boolean> removeAction) {
        this.removeAction = (element) -> {
            boolean proceed = removeAction.apply(element);
            if (proceed) {
                return element;
            }

            return null;
        };
    }

    /**
     * Add an element
     *
     * @param element the element to add
     * @return if the action was successful
     */
    public boolean add(final T element) {
        boolean added = set.add(element);
        if (added && addAction != null) {
            T result = addAction.apply(element);
            if (result == null) {
                set.remove(element);
                return false;
            }

            if (!ObjectUtils.equalsIgnoreCase(element, result)) {
                set.remove(element);
                set.add(result);
                return true;
            }
        }

        return added;
    }

    /**
     * Removes an element
     *
     * @param element the element to remove
     * @return if the action was successful
     */
    public boolean remove(final T element) {
        boolean removed = set.remove(element);
        if (removed && removeAction != null) {
            T result = addAction.apply(element);
            if (result == null) {
                set.add(element);
                return false;
            }

            if (!ObjectUtils.equalsIgnoreCase(element, result)) {
                set.add(element);
                set.remove(result);
                return true;
            }
        }

        return removed;
    }

    /**
     * Get the set size
     */
    public int size() {
        return set.size();
    }

    /**
     * Get the parent hashset
     *
     * @return the parent hashset
     */
    protected Set<T> getHashSet() {
        return set;
    }

    /**
     * Clear the set
     */
    protected int clearSet() {
        Iterator<T> iterator = set.iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            T element = iterator.next();
            if (removeAction != null) {
                T result = addAction.apply(element);
                if (ObjectUtils.equalsIgnoreCase(element, result)) {
                    iterator.remove();
                    removed++;
                }
            }
        }

        return removed;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link Object#equals(Object)}
     *     method, then calling the {@code hashCode} method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class {@code Object} does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java&trade; programming language.)
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        return set.hashCode();
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
        if (obj instanceof Set) {
            return set.equals(obj);
        }

        if (obj instanceof SetWatchDog) {
            SetWatchDog<?> dog = (SetWatchDog<?>) obj;
            return dog.set.equals(set);
        }

        return false;
    }

    /**
     * Get the set string representation
     *
     * @return the set string
     */
    @Override
    public String toString() {
        return set.toString();
    }
}