package es.karmadev.api.schedule.runner;

/**
 * Task status
 */
public enum TaskStatus {
    /**
     * The task is running
     */
    RUNNING,
    /**
     * The task is not stopped, but neither running
     */
    PAUSED,
    /**
     * The task is resuming
     */
    RESUMING,
    /**
     * The task is stopped
     */
    STOPPED
}
