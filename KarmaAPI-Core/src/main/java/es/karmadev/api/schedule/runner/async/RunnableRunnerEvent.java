package es.karmadev.api.schedule.runner.async;

import es.karmadev.api.schedule.runner.TaskRunner;
import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.schedule.runner.event.TaskRunnerEvent;
import es.karmadev.api.schedule.runner.task.RunTask;

class RunnableRunnerEvent implements TaskRunnerEvent<Runnable> {

    private final TaskRunner<? extends Number> executor;
    private final TaskEvent event;
    private final Runnable action;

    public RunnableRunnerEvent(final TaskRunner<? extends Number> executor, final TaskEvent event, final RunTask action) {
        this.executor = executor;
        this.event = event;
        this.action = action;
    }

    /**
     * Get the task of this event
     *
     * @return the task
     */
    @Override
    public TaskRunner<? extends Number> task() {
        return executor;
    }

    /**
     * Get the event trigger
     *
     * @return the task event trigger
     */
    @Override
    public TaskEvent trigger() {
        return event;
    }

    /**
     * Get the runner event
     *
     * @return the runner
     */
    @Override
    public Runnable get() {
        return action;
    }
}
