package es.karmadev.api.schedule.task.completable;

/**
 * KarmaAPI completed task for two
 * items
 *
 * @param <A> the first item
 * @param <B> the second item
 */
public interface BiCompletedTask<A, B> extends CompletedTask<A> {

    /**
     * Get the task B value
     *
     * @return the task value
     */
    B getValue();
}
