package es.karmadev.api.schedule.task.completable;

import java.util.function.Consumer;

/**
 * KarmaAPI late task
 */
@SuppressWarnings("unused")
public interface TaskCompletor<A> {

    /**
     * Set a task to run when this tasks
     * ends
     *
     * @param completion the task completion
     */
    void onComplete(final Consumer<CompletedTask<A>> completion);

    /**
     * Set a task to run when this tasks
     * ends
     *
     * @param task the task runner
     */
    void onComplete(final Runnable task);

    /**
     * Complete the task
     *
     * @param object the object of the task
     */
    <V extends A> void complete(final V object);

    /**
     * Complete the task
     *
     * @param object the object of the task
     * @param error the task error
     */
    <V extends A> void complete(final V object, Throwable error);

    /**
     * Complete the task if it's not completed
     *
     * @param object the object of the task
     */
    <V extends A> void completeFirst(final V object);

    /**
     * Complete the task if it's not completed
     *
     * @param object the object of the task
     * @param error the task error
     */
    <V extends A> void completeFirst(final V object, Throwable error);

    /**
     * Cancel the task
     */
    void cancel();

    /**
     * Set the task asynchronous status
     *
     * @param status the asynchronous status
     */
    void setAsync(final boolean status);

    /**
     * Get if the task is already
     * completed
     *
     * @return if the task is completed
     */
    boolean isComplete();

    /**
     * Get if the task is cancelled
     *
     * @return if the task
     * is cancelled
     */
    boolean isCancelled();

    /**
     * Get if this task will be
     * run asynchronously
     *
     * @return the task asynchronous
     * status
     */
    boolean isAsync();

    /**
     * Get the object
     *
     * @return the object
     */
    A get();

    /**
     * Get the error
     *
     * @return the error
     */
    Throwable getError();
}
