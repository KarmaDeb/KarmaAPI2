package es.karmadev.api.schedule.task;

/**
 * Scheduled task
 */
public interface ScheduledTask extends Runnable {

    /**
     * Get the class that issued this
     * task
     *
     * @return the task owner
     */
    Class<?> owner();

    /**
     * Get the task id
     *
     * @return the task id
     */
    int id();

    /**
     * Cancel this task
     */
    void cancel();

    /**
     * Get if the task is cancelled
     *
     * @return if the task is cancelled
     */
    boolean cancelled();

    /**
     * Get if the task is running
     *
     * @return if the task is running
     */
    boolean running();

    /**
     * Set the task run consumer
     *
     * @param action the action to perform when the task runs
     */
    void onRun(final Runnable action);
}
