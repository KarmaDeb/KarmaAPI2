package es.karmadev.api.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorModel<T> implements Iterator<T> {

    private final T[] elements;
    private int index;

    public IteratorModel(final T[] elements) {
        this.elements = elements;
        this.index = 0;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return index < elements.length;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public T next() {
        if (index == elements.length) {
            throw new NoSuchElementException();
        }

        return elements[index++];
    }
}
