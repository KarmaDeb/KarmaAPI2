package es.karmadev.api.schedule.runner.task;

import java.util.function.Consumer;

/**
 * Run the task
 */
public final class ConsumerTask<T> implements Consumer<T> {

    private boolean consumed = false;
    private final Consumer<T> task;

    private ConsumerTask(final Consumer<T> task) {
        this.task = task;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @Override
    public void accept(final T t) {
        if (consumed) return;
        consumed = true;
        task.accept(t);
    }

    /**
     * Reset the task status
     */
    public void reset() {
        consumed = false;
    }

    /**
     * Create a new consumer task from
     * the consumer
     *
     * @param task the consumer
     * @return the consumer task
     * @param <T> the consumer type
     */
    public static <T> ConsumerTask<T> forTask(final Consumer<T> task) {
        return new ConsumerTask<>(task);
    }
}
