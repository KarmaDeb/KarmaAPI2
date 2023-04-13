package es.karmadev.api.logger.log.file.component.header;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * KarmaAPI log header
 */
@SuppressWarnings("unused")
public class LogHeader implements Iterable<HeaderLine> {

    private final List<HeaderLine> header = new CopyOnWriteArrayList<>();

    /**
     * Add a line to the header
     *
     * @param line the header line
     * @return the header
     */
    public LogHeader add(final String line) {
        header.add(new HeaderLine(line));
        return this;
    }

    /**
     * Add a line to the header
     *
     * @param line the header line
     * @return the header
     */
    public LogHeader add(final HeaderLine line) {
        header.add(line);
        return this;
    }

    /**
     * Remove a line from the header
     *
     * @param line the header line
     * @return if the line could be removed
     */
    public synchronized boolean remove(final HeaderLine line) {
        return header.remove(line);
    }

    /**
     * Remove a line from the header
     *
     * @param index the line index
     * @return if the line could be removed
     */
    public synchronized HeaderLine remove(final int index) {
        if (header.size() < index) return null;
        return header.remove(index);
    }

    /**
     * Build the header
     *
     * @return the header
     */
    public synchronized String build() {
        List<HeaderLine> lines = Collections.unmodifiableList(header);
        StringBuilder builder = new StringBuilder();

        for (HeaderLine line : lines) builder.append(line.get()).append("\n");
        return builder.toString();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<HeaderLine> iterator() {
        return Collections.unmodifiableList(header).iterator();
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
    public synchronized void forEach(final Consumer<? super HeaderLine> action) {
        List<HeaderLine> lines = Collections.unmodifiableList(header);
        for (HeaderLine line : lines) action.accept(line);
    }

    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * @return a {@code Spliterator} over the elements described by this
     * {@code Iterable}.
     * @implSpec The default implementation creates an
     * <em><a href="Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * @implNote The default implementation should usually be overridden.  The
     * spliterator returned by the default implementation has poor splitting
     * capabilities, is unsized, and does not report any spliterator
     * characteristics. Implementing classes can nearly always provide a
     * better implementation.
     * @since 1.8
     */
    @Override
    public synchronized Spliterator<HeaderLine> spliterator() {
        return Collections.unmodifiableList(header).spliterator();
    }
}
