package es.karmadev.api.schedule.task.scheduler;

import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.schedule.task.ScheduledTask;
import es.karmadev.api.schedule.task.TaskScheduler;
import org.jetbrains.annotations.Nullable;

/**
 * KarmaAPI thread blocking scheduler
 */
public class ThreadBlockingScheduler implements TaskScheduler {

    /**
     * Schedule a task
     *
     * @param task the task to schedule
     * @return the scheduled task
     */
    @Override
    public ScheduledTask schedule(final Runnable task) {
        return null;
    }

    /**
     * Get a scheduled task
     *
     * @param id the task id
     * @return the scheduled task
     */
    @Override
    public @Nullable ScheduledTask getTask(final int id) {
        return null;
    }

    /**
     * Get the scheduler source
     *
     * @return the scheduler source
     */
    @Override
    public KarmaSource getSource() {
        return null;
    }

    /**
     * Get the task scheduler size
     *
     * @return the scheduler size
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Get the task scheduler overload queue
     * size
     *
     * @return the overloaded tasks size
     */
    @Override
    public int overloadSize() {
        return 0;
    }

    /**
     * Get if the scheduler is paused
     *
     * @return if the scheduler is paused
     */
    @Override
    public boolean paused() {
        return false;
    }

    /**
     * Get the amount of completed tasks
     *
     * @return the completed tasks
     */
    @Override
    public int completed() {
        return 0;
    }

    /**
     * Get the amount of cancelled tasks
     *
     * @return the cancelled tasks
     */
    @Override
    public int cancelled() {
        return 0;
    }
}
