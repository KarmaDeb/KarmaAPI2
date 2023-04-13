package es.karmadev.api.schedule.runner.event;

/**
 * Task events
 */
public enum TaskEvent {
    /**
     * When the task starts
     */
    START,
    /**
     * When the task gets resumed
     */
    RESUME,
    /**
     * When the task ticks
     */
    TICK,
    /**
     * When the task restarts
     */
    RESTART,
    /**
     * When the task ends
     */
    END,
    /**
     * When the task gets paused
     */
    PAUSE,
    /**
     * When the task gets interrupted
     */
    STOP
}
