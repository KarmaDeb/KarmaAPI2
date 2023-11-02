package es.karmadev.api.schedule.runner.task;

/**
 * Run the task
 */
public final class RunTask implements Runnable {

    private boolean consumed = false;
    private Runnable task;

    private RunTask(final Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        if (consumed) return;
        consumed = true;
        task.run();
    }

    /**
     * Reset the task status
     */
    public void reset() {
        consumed = false;
    }

    /**
     * Create a new run task from
     * a runnable task
     *
     * @param task the task to run
     * @return the run task
     */
    public static RunTask forTask(final Runnable task) {
        return new RunTask(task);
    }
}
