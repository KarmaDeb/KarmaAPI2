package es.karmadev.api.schedule.task.completable.late;

import es.karmadev.api.schedule.task.completable.CompletedTask;
import es.karmadev.api.schedule.task.completable.TaskCompletor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * KarmaAPI task completor
 *
 * @param <A> the task object
 */
public class LateTask<A> implements TaskCompletor<A> {

    private final long start;

    private boolean cancelled = false;
    private boolean asynchronous;
    private boolean valueSet = false;
    private A object;
    private Throwable error;

    private final AtomicBoolean completedOnce = new AtomicBoolean(false);
    private final AtomicReference<Consumer<CompletedTask<A>>> completorConsumer = new AtomicReference<>(null);
    private final AtomicReference<Runnable> completorTask = new AtomicReference<>(null);

    /**
     * Create a new task
     */
    public LateTask() {
        this(false);
    }

    /**
     * Create a new task
     *
     * @param async the async status
     */
    public LateTask(final boolean async) {
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
    public void onComplete(final Consumer<CompletedTask<A>> completion) {
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
     */
    @Override
    public <V extends A> void complete(final V object) {
        complete(object, null);
    }

    /**
     * Complete the task
     *
     * @param object the object of the task
     * @param error  the task error
     */
    @Override
    public <V extends A> void complete(V object, Throwable error) {
        if (cancelled) return;
        this.object = object;
        this.error = error;
        valueSet = true;

        completeInternal();
    }

    /**
     * Complete the task if it's not completed
     *
     * @param object the object of the task
     */
    @Override
    public <V extends A> void completeFirst(final V object) {
        if (completedOnce.get()) return;
        complete(object, null);
    }

    /**
     * Complete the task if it's not completed
     *
     * @param object the object of the task
     * @param error  the task error
     */
    @Override
    public <V extends A> void completeFirst(final V object, final Throwable error) {
        if (completedOnce.get()) return;
        complete(object, error);
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
        CompletedTask<A> task = SimpleCompleted.of(object, error, now - start);

        Consumer<CompletedTask<A>> consumer = completorConsumer.getAndSet(null);
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
