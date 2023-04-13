package es.karmadev.api.schedule.task.completable.late;

import es.karmadev.api.schedule.task.completable.CompletedTask;
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
public class SimpleCompleted<A> implements CompletedTask<A> {

    A get;

    Throwable error;

    long completionTime;
}
