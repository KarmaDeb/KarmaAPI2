package es.karmadev.api.schedule.runner.async;

import es.karmadev.api.schedule.runner.TaskRunner;
import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.schedule.runner.event.TaskRunnerEvent;

import java.util.function.Consumer;

class ConsumerRunnerEvent<T extends Number> implements TaskRunnerEvent<Consumer<T>> {

    private final TaskRunner<T> executor;
    private final TaskEvent event;
    private final Consumer<T> action;

    private ConsumerRunnerEvent(final TaskRunner<T> executor, final TaskEvent event, final Consumer<T> action) {
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
    public Consumer<T> get() {
        return action;
    }

    /**
     * Create a runner event for the type
     *
     * @param executor the executor
     * @param event the event
     * @param action the action to perform
     * @return the runner event
     * @param <T> the type
     */
    public static <T extends Number> ConsumerRunnerEvent<T> forType(final TaskRunner<T> executor, final TaskEvent event, final Consumer<T> action) {
        return new ConsumerRunnerEvent<>(executor, event, action);
    }
}
