package es.karmadev.api.core.scheduler;

import es.karmadev.api.schedule.task.scheduler.Task;

/**
 * Spigot task
 */
public class SpigotTask extends Task {

    private boolean synchronous = false;

    /**
     * Initialize the task
     *
     * @param task  the task
     * @param owner the task owner
     * @throws NullPointerException if the task is null
     */
    public SpigotTask(final Runnable task, final Class<?> owner) throws NullPointerException {
        super(task, owner);
    }

    /**
     * Set if the task will run synchronously
     *
     * @return the modified task
     */
    public SpigotTask markSynchronous() {
        synchronous = true;
        return this;
    }

    /**
     * Set if the task will run asynchronously
     *
     * @return the modified task
     */
    public SpigotTask markAsynchronous() {
        synchronous = false;
        return this;
    }

    /**
     * Get if the task is synchronous
     *
     * @return if the task is sync
     */
    public boolean isSynchronous() {
        return synchronous;
    }
}
