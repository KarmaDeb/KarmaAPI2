package es.karmadev.api.schedule.runner.event;

import es.karmadev.api.schedule.runner.TaskRunner;

/**
 * Task runner event
 *
 * @param <T> the event runner type
 */
@SuppressWarnings("unused")
public interface TaskRunnerEvent<T> {

    /**
     * Get the task of this event
     *
     * @return the task
     */
    TaskRunner task();

    /**
     * Get the event trigger
     *
     * @return the task event trigger
     */
    TaskEvent trigger();

    /**
     * Get the runner event
     *
     * @return the runner
     */
    T get();

    /**
     * Get if the event is hooked
     *
     * @return if the event is hooked
     */
    boolean isHooked();

    /**
     * Un hook the event
     */
    default void unHook() {
        task().off(this);
    }
}
