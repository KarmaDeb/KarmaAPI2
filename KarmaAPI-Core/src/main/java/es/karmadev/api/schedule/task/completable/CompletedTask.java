package es.karmadev.api.schedule.task.completable;

/**
 * KarmaAPI completed task
 *
 * @param <A> the task item
 */
@SuppressWarnings("unused")
public interface CompletedTask<A> {

    /**
     * Get the task value
     *
     * @return the task value
     */
    A get();

    /**
     * Get the task error
     *
     * @return the task error or null
     * if none
     */
    Throwable error();

    /**
     * Get how much time did the task took
     * to complete
     *
     * @return the task completion time
     */
    long completionTime();
}
