package es.karmadev.api.schedule.task;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;
import org.jetbrains.annotations.Nullable;

/**
 * Task scheduler
 */
public interface TaskScheduler {

    /**
     * Schedule a task
     *
     * @param task the task to schedule
     * @return the scheduled task
     */
    ScheduledTask schedule(final Runnable task);

    /**
     * Get a scheduled task
     *
     * @param id the task id
     * @return the scheduled task
     */
    @Nullable
    ScheduledTask getTask(final int id);

    /**
     * Get the scheduler source
     *
     * @return the scheduler source
     */
    APISource getSource();

    /**
     * Get the task scheduler size
     *
     * @return the scheduler size
     */
    int size();

    /**
     * Get the task scheduler overload queue
     * size
     *
     * @return the overloaded tasks size
     */
    int overloadSize();

    /**
     * Get if the scheduler is paused
     *
     * @return if the scheduler is paused
     */
    boolean paused();

    /**
     * Get if the system is overloaded in
     * for this scheduler
     *
     * @return if the system is overloaded
     */
    boolean overloaded();

    /**
     * Get the amount of completed tasks
     *
     * @return the completed tasks
     */
    int completed();

    /**
     * Get the amount of cancelled tasks
     *
     * @return the cancelled tasks
     */
    int cancelled();
}
