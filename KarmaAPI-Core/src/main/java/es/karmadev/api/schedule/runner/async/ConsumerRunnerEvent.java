package es.karmadev.api.schedule.runner.async;

import es.karmadev.api.schedule.runner.TaskRunner;
import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.schedule.runner.event.TaskRunnerEvent;

import java.util.function.Consumer;

class ConsumerRunnerEvent implements TaskRunnerEvent<Consumer<Long>> {

    private final AsyncTaskExecutor executor;
    private final TaskEvent event;
    private final Consumer<Long> action;

    public ConsumerRunnerEvent(final AsyncTaskExecutor executor, final TaskEvent event, final Consumer<Long> action) {
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
    public TaskRunner task() {
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
    public Consumer<Long> get() {
        return action;
    }

    /**
     * Get if the event is hooked
     *
     * @return if the event is hooked
     */
    @Override
    public boolean isHooked() {
        return executor.events.contains(this);
    }
}
