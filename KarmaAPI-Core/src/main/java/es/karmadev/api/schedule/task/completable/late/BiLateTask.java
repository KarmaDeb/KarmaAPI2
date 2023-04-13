package es.karmadev.api.schedule.task.completable.late;

import es.karmadev.api.schedule.task.completable.BiCompletedTask;
import es.karmadev.api.schedule.task.completable.BiTaskCompletor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * KarmaAPI task completor
 *
 * @param <A> the task object
 */
@SuppressWarnings("unused")
public class BiLateTask<A, B> implements BiTaskCompletor<A, B> {

    private final long start;

    private boolean cancelled = false;
    private boolean asynchronous;
    private boolean valueSet = false;
    private A object;
    private B value;
    private Throwable error;

    private final AtomicBoolean completedOnce = new AtomicBoolean(false);
    private final AtomicReference<Consumer<BiCompletedTask<A, B>>> completorConsumer = new AtomicReference<>(null);
    private final AtomicReference<Runnable> completorTask = new AtomicReference<>(null);

    /**
     * Create a new task
     */
    public BiLateTask() {
        this(false);
    }

    /**
     * Create a new task
     *
     * @param async the async status
     */
    public BiLateTask(final boolean async) {
        start = System.currentTimeMillis();
        asynchronous = async;
    }

    /**
     * Set a task to run when this tasks
     * ends
     *
     * @param completion the task completion
     */
    @Override
    public void onComplete(final Consumer<BiCompletedTask<A, B>> completion) {
        completorConsumer.set(completion);
        if (valueSet) completeInternal();
    }

    /**
     * Set a task to run when this tasks
     * ends
     *
     * @param task the task runner
     */
    @Override
    public void onComplete(final Runnable task) {
        completorTask.set(task);
        if (valueSet) completeInternal();
    }

    /**
     * Complete the task
     *
     * @param object the object of the task
     * @param value  the object value of the task
     */
    @Override
    public void complete(final A object, final B value) {
        complete(object, value, null);
    }

    /**
     * Complete the task
     *
     * @param object the object of the task
     * @param value  the object value of the task
     * @param error  the task error
     */
    @Override
    public void complete(final A object, final B value, final Throwable error) {
        if (cancelled || this.object != null || this.value != null) return;
        this.object = object;
        this.value = value;
        this.error = error;
        valueSet = true;

        completeInternal();
    }

    /**
     * Cancel the task
     */
    @Override
    public void cancel() {
        if (object != null) return; //We cannot cancel a completed task
        cancelled = true;
    }

    /**
     * Set the task asynchronous status
     *
     * @param status the asynchronous status
     */
    @Override
    public void setAsync(final boolean status) {
        if (object != null || cancelled) return;
        asynchronous = status;
    }

    /**
     * Get if the task is already
     * completed
     *
     * @return if the task is completed
     */
    @Override
    public boolean isComplete() {
        return completedOnce.get();
    }

    /**
     * Get if the task is cancelled
     *
     * @return if the task
     * is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Get if this task will be
     * run asynchronously
     *
     * @return the task asynchronous
     * status
     */
    @Override
    public boolean isAsync() {
        return asynchronous;
    }

    /**
     * Get the object
     *
     * @return the object
     */
    @Override
    public A get() {
        return object;
    }

    /**
     * Get the object value
     *
     * @return the object value
     */
    @Override
    public B getValue() {
        return value;
    }

    /**
     * Get the error
     *
     * @return the error
     */
    @Override
    public Throwable getError() {
        return error;
    }

    /**
     * Complete the task
     */
    private void completeInternal() {
        long now = System.currentTimeMillis();
        BiCompletedTask<A, B> task = SimpleBiCompleted.of(object, value, error, now - start);

        Consumer<BiCompletedTask<A, B>> consumer = completorConsumer.getAndSet(null);
        Runnable runnable = completorTask.getAndSet(null);

        if (consumer == null && runnable == null) return;

        if (asynchronous) {
            CompletableFuture.runAsync(() -> {
                if (consumer != null) {
                    consumer.accept(task);
                }
                if (runnable != null) {
                    runnable.run();
                }

                completedOnce.set(true);
            });
        } else {
            if (consumer != null) {
                consumer.accept(task);
            }
            if (runnable != null) {
                runnable.run();
            }

            completedOnce.set(true);
        }
    }
}
