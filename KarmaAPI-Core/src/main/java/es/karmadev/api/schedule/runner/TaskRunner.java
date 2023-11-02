package es.karmadev.api.schedule.runner;

import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.schedule.runner.event.TaskRunnerEvent;
import es.karmadev.api.schedule.runner.task.RunTask;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Task runner
 */
@SuppressWarnings("unused")
public interface TaskRunner<TaskType extends Number> {

    /**
     * Get the task ID
     *
     * @return the task ID
     */
    long id();

    /**
     * Force the task runner time left
     *
     * @param newTimeLeft the new runner time left
     */
    void forceTimeLeft(final TaskType newTimeLeft);

    /**
     * Force the task runner max time
     *
     * @param newMaxTime the new runner max time
     */
    void forceMaxTime(final TaskType newMaxTime);

    /**
     * Set if the task repeats infinitely
     *
     * @param status the task repeat status
     */
    void setRepeating(final boolean status);

    /**
     * Start the task
     */
    void start();

    /**
     * Stops the task completely.
     */
    void stop();

    /**
     * Pauses the task and tries to
     * store its current state
     */
    void pause();

    /**
     * Resume the task
     */
    void resume();

    /**
     * Get if this task is a repeating
     * task
     *
     * @return if the task repeats constantly
     */
    boolean repeating();

    /**
     * Get the task status
     *
     * @return the task status
     */
    TaskStatus status();

    /**
     * Get the task interval
     *
     * @return the task interval
     */
    default TaskType interval() {
        return interval(workingUnit());
    }

    /**
     * Get the time left for this
     * task to end
     *
     * @return the task time left
     */
    default TaskType timeLeft() {
        return timeLeft(workingUnit());
    }

    /**
     * Get the task interval
     *
     * @param unit the unit to get the
     *             interval as
     * @return the task interval
     */
    TaskType interval(final TimeUnit unit);

    /**
     * Get the time left for this
     * task to end
     *
     * @param unit the unit to get the
     *             time as
     * @return the task time left
     */
    TaskType timeLeft(final TimeUnit unit);

    /**
     * Get the task working unit
     *
     * @return the task working unit
     */
    TimeUnit workingUnit();

    /**
     * Add an event listener for this task
     *
     * @param event the event
     * @return the task runner
     */
    TaskRunnerEvent<Consumer<TaskType>> onAny(final BiConsumer<TaskEvent, TaskType> event);

    /**
     * Add an event listener for this task
     *
     * @param event the event
     * @return the task runner
     */
    TaskRunnerEvent<Runnable> onAny(final Consumer<TaskEvent> event);

    /**
     * Add an event listener for this task
     *
     * @param event the event
     * @param tick the event consumer and the current tick
     * @return the task runner
     */
    @SuppressWarnings("UnusedReturnValue")
    TaskRunnerEvent<Consumer<TaskType>> on(final TaskEvent event, final Consumer<TaskType> tick);

    /**
     * Add an event listener for this task
     *
     * @param event the event
     * @param action the event consumer
     * @return the task runner
     */
    @SuppressWarnings("UnusedReturnValue")
    TaskRunnerEvent<Runnable> on(final TaskEvent event, final Runnable action);

    /**
     * Unhook a task event
     *
     * @param event the event
     */
    void off(final TaskRunnerEvent<?> event);
}
