package es.karmadev.api.schedule.task.scheduler;

import es.karmadev.api.schedule.task.ScheduledTask;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * KarmaAPI task
 */
public class Task implements ScheduledTask {

    private final static AtomicInteger globalId = new AtomicInteger(0);

    private final int id = globalId.incrementAndGet();
    private final Runnable task;
    private final Class<?> owner;

    private boolean cancelled = false;
    private boolean running = false;
    private Runnable onRun;
    private Runnable onEnd;

    /**
     * Initialize the task
     *
     * @param task the task
     * @param owner the task owner
     * @throws NullPointerException if the task is null
     */
    public Task(final Runnable task, final Class<?> owner) throws NullPointerException {
        if (task == null) throw new NullPointerException("Cannot create task for null task");
        this.task = task;
        this.owner = owner;
    }

    /**
     * Get the class that issued this
     * task
     *
     * @return the task owner
     */
    @Override
    public Class<?> owner() {
        return owner;
    }

    /**
     * Get the task id
     *
     * @return the task id
     */
    @Override
    public int id() {
        return id;
    }

    /**
     * Cancel this task
     */
    @Override
    public void cancel() {
        cancelled = true;
    }

    /**
     * Get if the task is cancelled
     *
     * @return if the task is cancelled
     */
    @Override
    public boolean cancelled() {
        return cancelled;
    }

    /**
     * Get if the task is running
     *
     * @return if the task is running
     */
    @Override
    public boolean running() {
        return running;
    }

    /**
     * Set the task run consumer
     *
     * @param action the action to perform when the task runs
     */
    @Override
    public void onRun(final Runnable action) {
        onRun = action;
    }

    /**
     * Set the task end consumer
     *
     * @param action the action to perform when the task ends
     */
    @Override
    public void onEnd(final Runnable action) {
        onEnd = action;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (onRun != null) onRun.run();

        if (!cancelled) {
            running = true;
            task.run();
            running = false;
            if (onEnd != null) onEnd.run();
        }
    }
}
