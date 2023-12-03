package es.karmadev.api.collect.priority;

import es.karmadev.api.collect.reflection.ArrayHelper;
import es.karmadev.api.collect.IteratorModel;
import es.karmadev.api.collect.container.SimpleContainer;
import es.karmadev.api.collect.map.LinkedIndexedMap;
import es.karmadev.api.collect.model.Container;
import es.karmadev.api.collect.model.PriorityCollection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Priority array is the default implementation
 * of a {@link PriorityCollection}
 * @param <T> the array type
 */
public class PriorityArray<T> implements PriorityCollection<T> {

    private final static double EPSILON = 0.0000000000000000000000d;

    @Getter
    private final Class<T> type;

    private int maxReadableIndex = 0;
    private int currentIndex = 0;
    private int currentSubIndex = 0;

    private final transient LinkedIndexedMap<Double, SimpleContainer<T>> elements = new LinkedIndexedMap<>();

    /**
     * Initialize the priority array
     *
     * @param type the array type
     */
    public PriorityArray(final Class<T> type) {
        this.type = type;
    }

    /**
     * Get the item priority
     *
     * @param item the item
     * @return the item priority
     */
    @Override
    public double getPriority(final T item) {
        int index = 0;
        for (SimpleContainer<T> container : elements.values()) {
            if (container.contains(item)) {
                return elements.getKey(index);
            }

            index++;
        }

        return -1d;
    }

    /**
     * Set the priority for the item at
     * the specified index
     *
     * @param index       the item index
     * @param newPriority the new priority
     * @return if the operation was successful
     */
    @Override
    public boolean setPriority(final int index, final double newPriority) {
        if (index < 0 || index > maxReadableIndex) {
            throw new IndexOutOfBoundsException("Cannot read from array at index " + index + " (max readable index is " + maxReadableIndex + ")");
        }

        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        int vIndex = 0;
        T itemToSet = null;
        for (double priority : elements.keySet()) {
            Container<T> container = elements.get(priority);

            T[] items = container.getItems(empty);
            for (T item : items) {
                if (priority != newPriority) {
                    if (vIndex == index) {
                        container.removeItem(item);
                        itemToSet = item;
                        break;
                    }
                }

                vIndex++;
            }
        }

        if (itemToSet == null) return false;

        Container<T> container = this.elements.computeIfAbsent(newPriority, (t) -> new SimpleContainer<>(-1, type));
        if (container.contains(itemToSet)) {
            return false;
        }

        return container.addItem(itemToSet);
    }

    /**
     * Set the priority for all the items
     * matching the item
     *
     * @param item        the item
     * @param newPriority the new item priority
     * @return if the operation was successful
     */
    @Override
    public boolean setPriority(final T item, final double newPriority) {
        if (item == null) throw new NullPointerException();

        double currentPriority = getPriority(item);
        if (currentPriority == newPriority) return false;

        SimpleContainer<T> container = elements.remove(currentPriority);
        if (container == null) return false;

        container.removeItem(item);
        SimpleContainer<T> targetContainer = elements.computeIfAbsent(newPriority, (t) -> new SimpleContainer<>(-1, type));
        return targetContainer.addItem(item);
    }

    /**
     * Add an item with priority
     *
     * @param item     the item to add
     * @param priority the priority
     */
    @Override
    public boolean add(final T item, final double priority) {
        if (item == null) return false;

        maxReadableIndex++;
        SimpleContainer<T> container = elements.computeIfAbsent(priority, (t) -> new SimpleContainer<>(-1, type));
        if (container.contains(item)) return false;

        return container.addItem(item);
    }

    /**
     * Add an item with priority
     *
     * @param index    the index to add at
     * @param item     the item to add
     * @param priority the priority
     */
    @Override
    public boolean add(final int index, final T item, final double priority) {
        if (index < 0 || index > maxReadableIndex) {
            throw new IndexOutOfBoundsException("Cannot write to array at index " + index + " (max readable index is " + maxReadableIndex + ")");
        }

        if (item == null) return false;

        maxReadableIndex++;
        SimpleContainer<T> container = elements.computeIfAbsent(priority, (t) -> new SimpleContainer<>(-1, type));
        if (container.contains(item)) return false;
        if (container.addItem(item)) {
            return container.relocate(item, index);
        }

        return false;
    }

    /**
     * Remove an item at the specified
     * index
     *
     * @param index the index item to remove
     * @return if the operation was successful
     */
    @Override
    public boolean remove(final int index) {
        if (index < 0 || index > maxReadableIndex) {
            throw new IndexOutOfBoundsException("Cannot read from array at index " + index + " (max readable index is " + maxReadableIndex + ")");
        }

        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        int vIndex = 0;
        for (double priority : elements.keySet()) {
            Container<T> container = elements.get(priority);

            T[] items = container.getItems(empty);
            for (T item : items) {
                if (vIndex == index) {
                    container.removeItem(item);
                    return true;
                }

                vIndex++;
            }
        }

        return false;
    }

    /**
     * Get an element on the specified
     * index
     *
     * @param index the index
     * @return the element
     */
    @Override
    public T get(final int index) {
        if (index < 0 || index > maxReadableIndex) {
            throw new IndexOutOfBoundsException("Cannot read from array at index " + index + " (max readable index is " + maxReadableIndex + ")");
        }

        int mIndex = 0;
        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        for (Container<T> container : elements.values()) {
            T[] items = container.getItems(empty);
            for (T item : items) {
                if (mIndex++ == index) {
                    return item;
                }
            }
        }

        return null;
    }

    /**
     * Get the index of an item
     *
     * @param item the item
     * @return the index
     */
    @Override
    public int indexOf(final T item) {
        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        int vIndex = 0;
        for (Container<T> container : elements.values()) {
            T[] items = container.getItems(empty);
            for (T element : items) {
                if (Objects.equals(element, item))
                    return vIndex;

                vIndex++;
            }
        }

        return -1;
    }

    /**
     * Get the index of an item with
     * the specified priority
     *
     * @param item     the item
     * @param priority the priority
     * @return the index
     */
    @Override
    public int indexOf(final T item, final double priority) {
        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        int vIndex = 0;
        for (double pr : elements.keySet()) {
            Container<T> container = elements.get(pr);

            T[] items = container.getItems(empty);
            for (T element : items) {
                if (pr == priority &&
                        Objects.equals(element, item)) {
                    return vIndex;
                }

                vIndex++;
            }

            if (pr == priority) return -1;
        }

        return -1;
    }

    /**
     * Get the next element in the
     * sorted version of the priority
     * array
     *
     * @return the next element
     */
    @Override
    public T next() {
        if (currentIndex >= maxReadableIndex) {
            return null;
        }
        if (currentIndex < 0) currentIndex = 0;

        LinkedIndexedMap<Double, SimpleContainer<T>> containerMap = toSortedMap();
        Double priority = containerMap.getKey(currentIndex);
        if (priority == null) return null;

        SimpleContainer<T> container = containerMap.get(priority);

        if (container.getSize() == currentSubIndex) {
            priority = containerMap.getKey(++currentIndex);
            if (priority == null) return null;

            container = containerMap.get(priority);
            currentSubIndex = 0;
        }

        T[] empty = ArrayHelper.typedArrayFor(type, 0);
        T[] items = container.getItems(empty);

        return items[currentSubIndex++];
    }

    /**
     * Removes the current element in
     * the sorted version of the priority
     * array
     *
     * @return if the operation was successful
     */
    @Override
    public boolean consume() {
        LinkedIndexedMap<Double, SimpleContainer<T>> containerMap = toSortedMap();
        Double priority = containerMap.getKey(currentIndex);
        if (priority == null) return false;

        SimpleContainer<T> container = elements.get(priority);
        T[] empty = ArrayHelper.typedArrayFor(type, 0);
        T[] items = container.getItems(empty);

        int targetIndex = currentSubIndex - 1;
        if (targetIndex < 0) {
            targetIndex = 0;
        }

        T item = items[targetIndex];
        if (container.removeItem(item)) {
            if (container.getSize() == 0) {
                currentIndex--;
                currentSubIndex = 1;
            } else {
                currentSubIndex--;
            }

            return true;
        }

        return false;
    }

    /**
     * Get the previous element in the
     * sorted version of the priority
     * array
     *
     * @return the previous element
     */
    @Override
    public T previous() {
        if (currentIndex <= 0 && currentSubIndex <= 0) {
            return null;
        }

        LinkedIndexedMap<Double, SimpleContainer<T>> containerMap = toSortedMap();

        Double priority = containerMap.getKey(currentIndex);

        boolean reset = false;
        while (priority == null) {
            priority = containerMap.getKey(--currentIndex);
            reset = true;
        }

        SimpleContainer<T> container = containerMap.get(priority);
        if (reset) {
            currentSubIndex = container.getSize();
        } else {
            currentSubIndex--;
        }

        if (currentSubIndex - 1 < 0) {
            priority = containerMap.getKey(--currentIndex);
            if (priority == null) return null;

            container = containerMap.get(priority);
            currentSubIndex = container.getSize();
        }

        T[] empty = ArrayHelper.typedArrayFor(type, 0);
        T[] items = container.getItems(empty);

        return items[--currentSubIndex];
    }

    /**
     * Get if the array has a next
     * element
     *
     * @return if the array has next
     */
    @Override
    public boolean hasNext() {
        LinkedIndexedMap<Double, SimpleContainer<T>> containerMap = toSortedMap();
        Double priority = containerMap.getKey(currentIndex);

        if (priority == null) return false;
        SimpleContainer<T> container = containerMap.get(priority);

        if (container.getSize() == currentSubIndex) {
            priority = containerMap.getKey(currentIndex + 1);
            return priority != null;
        }

        return true;
    }

    /**
     * Get if the array has a previous
     * element
     *
     * @return if the array has previous
     */
    @Override
    public boolean hasPrevious() {
        LinkedIndexedMap<Double, SimpleContainer<T>> containerMap = toSortedMap();
        Double priority = containerMap.getKey(currentIndex);

        if (priority == null) return false;
        if (currentSubIndex < 0) {
            priority = containerMap.getKey(currentIndex - 1);
            return priority != null;
        }

        return true;
    }

    /**
     * Get the sorted array copy
     * of the collection
     *
     * @param array the array to write to
     * @return the sorted array
     */
    @Override
    @SuppressWarnings("all")
    public <T1> T1[] sorted(final T1[] array) {
        Map<Double, SimpleContainer<T>> ordered = toSortedMap();

        List<T> elements = new ArrayList<>();
        T[] typedArray = ArrayHelper.typedArrayFor(type, 0);
        for (SimpleContainer<T> container : ordered.values()) {
            elements.addAll(Arrays.asList(container.getItems(typedArray)));
        }

        T[] sorted = elements.toArray(typedArray);
        if (array.length < sorted.length) {
            return (T1[]) Arrays.copyOf(sorted, sorted.length, array.getClass());
        }

        System.arraycopy(sorted, 0, array, 0, sorted.length);
        if (array.length > sorted.length)
            array[sorted.length] = null;

        return array;
    }

    private LinkedIndexedMap<Double, SimpleContainer<T>> toSortedMap() {
        return elements.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedIndexedMap::new
                ));
    }

    /**
     * Collect the collection into a
     * sorted array
     *
     * @return the sorted array
     */
    @Override
    public Collection<T> sortedCollect() {
        T[] sorted = sorted(ArrayHelper.typedArrayFor(type, 0));

        return Collections.unmodifiableCollection(
                Arrays.asList(sorted)
        );
    }

    /**
     * Get the unsorted collection
     * instance of the priority collection
     *
     * @return the unsorted collection
     */
    @Override
    public Collection<T> unsortedCollect() {
        List<T> list = new ArrayList<>();
        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        for (Container<T> container : elements.values()) {
            for (T item : container.getItems(empty)) {
                if (item == null) continue;
                list.add(item);
            }
        }

        return list;
    }

    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this collection
     */
    @Override
    public int size() {
        return maxReadableIndex;
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    @Override
    public boolean isEmpty() {
        return maxReadableIndex == 0;
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains the specified
     * element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this collection
     *                              (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              collection does not permit null elements
     *                              (<a href="#optional-restrictions">optional</a>)
     */
    @Override @SuppressWarnings("unchecked")
    public boolean contains(final Object o) {
        if (o == null) return false;

        for (Container<T> container : elements.values()) {
            try {
                if (container.contains((T) o)) return true;
            } catch (ClassCastException ignored) {}
        }

        return false;
    }

    /**
     * Returns an iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned
     * (unless this collection is an instance of some class that provides a
     * guarantee).
     *
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        Object[] sortedElements = sorted();
        return (Iterator<T>) new IteratorModel<>(sortedElements);
    }

    /**
     * Perform a quick sort on the array
     *
     * @param elements the elements
     * @param priorities the elements priorities
     */
    private static void quickSort(final Object[] elements, final double[] priorities) {
        if (elements.length > 1) {
            quickSort(elements, priorities, 0, elements.length - 1);
        }
    }

    /**
     * Execute the quick sort algorithm on
     * the array of elements by using the
     * priorities
     *
     * @param elements the elements to sort
     * @param priorities the priorities
     * @param low the lowest index
     * @param high the highest index
     */
    private static void quickSort(final Object[] elements, final double[] priorities, int low, int high) {
        if (low < high) {
            int pi = partition(elements, priorities, low, high);

            quickSort(elements, priorities, low, pi - 1);
            quickSort(elements, priorities, pi + 1, high);
        }
    }

    /**
     * Partition an array and sort
     * it
     *
     * @param elements the array elements
     * @param priorities the array priorities
     * @param low the lowest index
     * @param high the highest index
     * @return the pivot
     */
    private static int partition(final Object[] elements, final double[] priorities, int low, int high) {
        double pivot = priorities[low];
        int i = low + 1;
        int j = high;

        while (i <= j) {
            while (i <= j && priorities[i] < pivot) {
                i++;
            }

            while (i <= j && priorities[j] > pivot) {
                j--;
            }

            if (i <= j) {
                moveRight(elements, priorities, i, j);

                i += (j - i + 1);
                j = i - 1;
            }
        }

        return j;
    }

    /**
     * Move the array block element to the right
     *
     * @param elements the element array to move
     * @param priorities the priority array to move
     * @param start the being moved index
     * @param end the end index
     */
    private static void moveRight(Object[] elements, double[] priorities, int start, int end) {
        Object tempElement = elements[end];
        double tempPriority = priorities[end];

        for (int i = end; i > start; i--) {
            elements[i] = elements[i - 1];
            priorities[i] = priorities[i - 1];
        }

        elements[start] = tempElement;
        priorities[start] = tempPriority;
    }

    /**
     * Returns an array containing all of the elements in this collection.
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this collection
     */
    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        List<T> elements = new ArrayList<>();
        T[] typedArray = ArrayHelper.typedArrayFor(type, 0);
        for (SimpleContainer<T> container : this.elements.values()) {
            elements.addAll(Arrays.asList(container.getItems(typedArray)));
        }

        return elements.toArray();
    }

    /**
     * Returns an array containing all of the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     * If the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.
     *
     * <p>If this collection fits in the specified array with room to spare
     * (i.e., the array has more elements than this collection), the element
     * in the array immediately following the end of the collection is set to
     * <tt>null</tt>.  (This is useful in determining the length of this
     * collection <i>only</i> if the caller knows that this collection does
     * not contain any <tt>null</tt> elements.)
     *
     * <p>If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a collection known to contain only strings.
     * The following code can be used to dump the collection into a newly
     * allocated array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     * <p>
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this collection are to be
     *          stored, if it is big enough; otherwise, a new array of the same
     *          runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this collection
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this collection
     * @throws NullPointerException if the specified array is null
     */
    @NotNull
    @Override
    @SuppressWarnings("all")
    public <T1> T1 @NotNull [] toArray(final T1[] a) {
        List<T> elements = new ArrayList<>();
        T[] typedArray = ArrayHelper.typedArrayFor(type, 0);
        for (SimpleContainer<T> container : this.elements.values()) {
            elements.addAll(Arrays.asList(container.getItems(typedArray)));
        }

        return elements.toArray(a);
    }

    /**
     * Ensures that this collection contains the specified element (optional
     * operation).  Returns <tt>true</tt> if this collection changed as a
     * result of the call.  (Returns <tt>false</tt> if this collection does
     * not permit duplicates and already contains the specified element.)<p>
     * <p>
     * Collections that support this operation may place limitations on what
     * elements may be added to this collection.  In particular, some
     * collections will refuse to add <tt>null</tt> elements, and others will
     * impose restrictions on the type of elements that may be added.
     * Collection classes should clearly specify in their documentation any
     * restrictions on what elements may be added.<p>
     * <p>
     * If a collection refuses to add a particular element for any reason
     * other than that it already contains the element, it <i>must</i> throw
     * an exception (rather than returning <tt>false</tt>).  This preserves
     * the invariant that a collection always contains the specified element
     * after this call returns.
     *
     * @param t element whose presence in this collection is to be ensured
     * @return <tt>true</tt> if this collection changed as a result of the
     * call
     */
    @Override
    public boolean add(final T t) {
        return add(t, EPSILON);
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).  More formally,
     * removes an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
     * this collection contains one or more such elements.  Returns
     * <tt>true</tt> if this collection contained the specified element (or
     * equivalently, if this collection changed as a result of the call).
     *
     * @param o element to be removed from this collection, if present
     * @return <tt>true</tt> if an element was removed as a result of this call
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this collection
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this
     *                                       collection does not permit null elements
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this collection
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object o) {
        for (Container<T> container : elements.values()) {
            try {
                if (container.removeItem((T) o)) {
                    maxReadableIndex--;
                    return true;
                }
            } catch (ClassCastException ignored) {}
        }

        return false;
    }

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return <tt>true</tt> if this collection contains all of the elements
     * in the specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              collection
     *                              (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this collection does not permit null
     *                              elements
     *                              (<a href="#optional-restrictions">optional</a>),
     *                              or if the specified collection is null.
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(final @NotNull Collection<?> c) {
        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        for (Container<T> container : elements.values()) {
            try {
                if (c.containsAll(Arrays.asList(container.getItems(empty)))) {
                    return true;
                }
            } catch (ClassCastException ignored) {}
        }

        return false;
    }

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param c collection containing elements to be added to this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(Object)
     */
    @Override
    public boolean addAll(final @NotNull Collection<? extends T> c) {
        boolean modifications = false;
        for (T element : c) {
            if (add(element)) {
                modifications = true;
            }
        }

        return modifications;
    }

    /**
     * Removes all of this collection's elements that are also contained in the
     * specified collection (optional operation).  After this call returns,
     * this collection will contain no elements in common with the specified
     * collection.
     *
     * @param c collection containing elements to be removed from this collection
     * @return <tt>true</tt> if this collection changed as a result of the
     * call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
     *                                       is not supported by this collection
     * @throws ClassCastException            if the types of one or more elements
     *                                       in this collection are incompatible with the specified
     *                                       collection
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this collection contains one or more
     *                                       null elements and the specified collection does not support
     *                                       null elements
     *                                       (<a href="#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(final @NotNull Collection<?> c) {
        boolean modifications = false;
        for (Object element : c) {
            if (remove(element)) {
                modifications = true;
            }
        }

        return modifications;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes from
     * this collection all of its elements that are not contained in the
     * specified collection.
     *
     * @param c collection containing elements to be retained in this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the types of one or more elements
     *                                       in this collection are incompatible with the specified
     *                                       collection
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this collection contains one or more
     *                                       null elements and the specified collection does not permit null
     *                                       elements
     *                                       (<a href="#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean retainAll(final @NotNull Collection<?> c) {
        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        boolean modifications = false;
        for (Container<T> container : elements.values()) {
            T[] values = container.getItems(empty);
            for (T value : values) {
                if (!c.contains(value) && remove(value)) {
                    modifications = true;
                }
            }
        }

        return modifications;
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this method returns.
     */
    @Override
    public void clear() {
        elements.clear();
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
        StringBuilder builder = new StringBuilder("PriorityArray@").append(hashCode()).append("[");

        T[] empty = ArrayHelper.typedArrayFor(type, 0);

        int index = 0;
        for (Container<T> container : elements.values()) {
            T[] values = container.getItems(empty);
            for (int i = 0; i < values.length; i++) {
                T value = values[i];
                builder.append(value);

                if (index == elements.size() - 1) {
                    if (i == values.length - 1) {
                        break;
                    }
                }

                builder.append(", ");
            }

            index++;
        }

        return builder.append("]").toString();
    }
}
