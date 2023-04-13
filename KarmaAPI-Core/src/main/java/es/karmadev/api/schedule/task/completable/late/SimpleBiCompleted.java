package es.karmadev.api.schedule.task.completable.late;

import es.karmadev.api.schedule.task.completable.BiCompletedTask;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Simple completed task
 *
 * @param <A> the task item
 */
@Accessors(fluent = true)
@Value(staticConstructor = "of")
@AllArgsConstructor(staticName = "of")
public class SimpleBiCompleted<A, B> implements BiCompletedTask<A, B> {

    A get;
    B getValue;

    Throwable error;

    long completionTime;
}
