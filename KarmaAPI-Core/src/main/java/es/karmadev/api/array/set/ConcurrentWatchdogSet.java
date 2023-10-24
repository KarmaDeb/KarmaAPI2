package es.karmadev.api.array.set;

import es.karmadev.api.object.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Concurrent set watchdog
 *
 * @param <T> the set type
 */
public class ConcurrentWatchdogSet<T> extends SetWatchDog<T> implements Set<T> {

    /**
     * Create an empty watchdog set
     */
    public ConcurrentWatchdogSet() {
        super(ConcurrentHashMap.newKeySet(), (unused) -> unused, (unused) -> unused);
    }

    /**
     * Create an empty watchdog set
     *
     * @param initialSize the initial set capacity
     */
    public ConcurrentWatchdogSet(final int initialSize) {
        super(ConcurrentHashMap.newKeySet(initialSize), (unused) -> unused, (unused) -> unused);
    }

    /**
     * Set the add action
     *
     * @param addAction the action to perform
     */
    public void setOnAdd(final Function<T, T> addAction) {
        this.addAction = addAction;
    }

    /**
     * Set the remove action
     *
     * @param removeAction the action to perform
     */
    public void setOnRemove(final Function<T, T> removeAction) {
        this.removeAction = removeAction;
    }

    /**
     * Set the add action
     *
     * @param addAction the action to perform
     */
    public void onAdd(final Consumer<T> addAction) {
        this.addAction = (unused) -> {
            addAction.accept(unused);
            return unused;
        };
    }

    /**
     * Set the remove action
     *
     * @param removeAction the action to perform
     */
    public void onRemove(final Consumer<T> removeAction) {
        this.removeAction = (unused) -> {
            removeAction.accept(unused);
            return unused;
        };
    }

    /**
     * Set the add action
     *
     * @param addAction the action to perform
     */
    public void onAdd(final Runnable addAction) {
        this.addAction = (unused) -> {
            addAction.run();
            return unused;
        };
    }

    /**
     * Set the remove action
     *
     * @param removeAction the action to perform
     */
    public void onRemove(final Runnable removeAction) {
        this.removeAction = (unused) -> {
            removeAction.run();
            return unused;
        };
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              set does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(final Object o) {
        return set.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set
     */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Unless otherwise specified by the implementing class,
     * actions are performed in the order of iteration (if an iteration order
     * is specified).  Exceptions thrown by the action are relayed to the
     * caller.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     * @since 1.8
     */
    @Override
    public void forEach(final Consumer<? super T> action) {
        set.forEach(action);
    }

    /**
     * Returns an array containing all of the elements in this set.
     * If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the
     * elements in the same order.
     *
     * <p>The returned array will be "safe" in that no references to it
     * are maintained by this set.  (In other words, this method must
     * allocate a new array even if this set is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all the elements in this set
     */
    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return set.toArray();
    }

    /**
     * Returns an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array.
     * If the set fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this set.
     *
     * <p>If this set fits in the specified array with room to spare
     * (i.e., the array has more elements than this set), the element in
     * the array immediately following the end of the set is set to
     * <tt>null</tt>.  (This is useful in determining the length of this
     * set <i>only</i> if the caller knows that this set does not contain
     * any null elements.)
     *
     * <p>If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements
     * in the same order.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a set known to contain only strings.
     * The following code can be used to dump the set into a newly allocated
     * array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     * <p>
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this set are to be
     *          stored, if it is big enough; otherwise, a new array of the same
     *          runtime type is allocated for this purpose.
     * @return an array containing all the elements in this set
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in this
     *                              set
     * @throws NullPointerException if the specified array is null
     */
    @NotNull
    @Override
    public <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        return set.toArray(a);
    }

    /**
     * Removes the specified element from this set if it is present
     * (optional operation).  More formally, removes an element <tt>e</tt>
     * such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
     * this set contains such an element.  Returns <tt>true</tt> if this set
     * contained the element (or equivalently, if this set changed as a
     * result of the call).  (This set will not contain the element once the
     * call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this set
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this
     *                                       set does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this set
     */
    @Override @SuppressWarnings("unchecked")
    public boolean remove(final Object o) {
        boolean success = set.remove(o);
        if (success && removeAction != null) {
            try {
                T element = (T) o;
                T result = removeAction.apply(element);

                if (result == null) {
                    set.add(element);
                    return false;
                }

                if (!ObjectUtils.equalsIgnoreCase(element, result)) {
                    set.add(element);
                    set.remove(result);
                    return true;
                }
            } catch (ClassCastException ignored) {}
        }

        return success;
    }

    /**
     * Returns <tt>true</tt> if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
     *
     * @param c collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements of the
     * specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this set does not permit null
     *                              elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return set.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation).  If the specified
     * collection is also a set, the <tt>addAll</tt> operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     *
     * @param c collection containing elements to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of the
     *                                       specified collection prevents it from being added to this set
     * @throws NullPointerException          if the specified collection contains one
     *                                       or more null elements and this set does not permit null
     *                                       elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this set
     * @see #add(Object)
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean added = false;
        for (T element : c) {
            boolean success = set.add(element);
            if (success && addAction != null) {
                T result = removeAction.apply(element);

                if (result == null) {
                    set.remove(element);
                    continue;
                }

                if (!ObjectUtils.equalsIgnoreCase(element, result)) {
                    set.remove(element);
                    set.add(result);
                    added = true;
                }
            }
        }

        return added;
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this set all of its elements that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     *
     * @param c collection containing elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of this set
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean modified = false;
        for (Iterator<T> it = iterator(); it.hasNext();) {
            T element = it.next();
            if (!c.contains(element)) {
                if (removeAction != null) {
                    T result = removeAction.apply(element);
                    if (!ObjectUtils.equalsIgnoreCase(element, result)) {
                        continue;
                    }

                    it.remove();
                    modified = true;
                }
            }
        }

        return modified;
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     *
     * @param c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of this set
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(final @NotNull Collection<?> c) {
        boolean modified = false;
        for (Iterator<T> it = iterator(); it.hasNext();) {
            T element = it.next();
            if (c.contains(element)) {
                if (removeAction != null) {
                    T result = removeAction.apply(element);
                    if (!ObjectUtils.equalsIgnoreCase(element, result)) {
                        continue;
                    }

                    it.remove();
                    modified = true;
                }
            }
        }

        return modified;
    }

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method
     *                                       is not supported by this set
     */
    @Override
    public void clear() {
        clearSet();
    }

    /**
     * Removes all of the elements of this collection that satisfy the given
     * predicate.  Errors or runtime exceptions thrown during iteration or by
     * the predicate are relayed to the caller.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     *               removed
     * @return {@code true} if any elements were removed
     * @throws NullPointerException          if the specified filter is null
     * @throws UnsupportedOperationException if elements cannot be removed
     *                                       from this collection.  Implementations may throw this exception if a
     *                                       matching element cannot be removed or if, in general, removal is not
     *                                       supported.
     * @implSpec The default implementation traverses all elements of the collection using
     * its {@link #iterator}.  Each matching element is removed using
     * {@link Iterator#remove()}.  If the collection's iterator does not
     * support removal then an {@code UnsupportedOperationException} will be
     * thrown on the first matching element.
     * @since 1.8
     */
    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        if (filter == null) return false;

        boolean removed = false;
        final Iterator<T> each = iterator();
        while (each.hasNext()) {
            T element = each.next();
            if (filter.test(element)) {
                if (removeAction != null) {
                    T result = removeAction.apply(element);
                    if (!ObjectUtils.equalsIgnoreCase(each, result)) {
                        continue;
                    }

                    each.remove();
                    removed = true;
                }
            }
        }

        return removed;
    }

    /**
     * Creates a {@code Spliterator} over the elements in this set.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#DISTINCT}.
     * Implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this set
     * @implSpec The default implementation creates a
     * <em><a href="Spliterator.html#binding">late-binding</a></em> spliterator
     * from the set's {@code Iterator}.  The spliterator inherits the
     * <em>fail-fast</em> properties of the set's iterator.
     * <p>
     * The created {@code Spliterator} additionally reports
     * {@link Spliterator#SIZED}.
     * @implNote The created {@code Spliterator} additionally reports
     * {@link Spliterator#SUBSIZED}.
     * @since 1.8
     */
    @Override
    public Spliterator<T> spliterator() {
        return set.spliterator();
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * <p>This method should be overridden when the {@link #spliterator()}
     * method cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @implSpec The default implementation creates a sequential {@code Stream} from the
     * collection's {@code Spliterator}.
     * @since 1.8
     */
    @Override
    public Stream<T> stream() {
        return set.stream();
    }

    /**
     * Returns a possibly parallel {@code Stream} with this collection as its
     * source.  It is allowable for this method to return a sequential stream.
     *
     * <p>This method should be overridden when the {@link #spliterator()}
     * method cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @return a possibly parallel {@code Stream} over the elements in this
     * collection
     * @implSpec The default implementation creates a parallel {@code Stream} from the
     * collection's {@code Spliterator}.
     * @since 1.8
     */
    @Override
    public Stream<T> parallelStream() {
        return set.parallelStream();
    }
}
