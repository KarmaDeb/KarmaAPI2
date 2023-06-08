package es.karmadev.api.schedule.runner.signal;

import es.karmadev.api.schedule.runner.signal.parameter.SignalParameter;

/**
 * Simple signal runner
 */
public class SimpleSignal implements SignalHandler {

    private final Runnable task;

    /**
     * Initialize the signal
     *
     * @param task the task to run on
     *             signal
     */
    public SimpleSignal(final Runnable task) {
        this.task = task;
    }


    /**
     * Emit a signal
     *
     * @param parameters the signal parameters
     */
    @Override
    public void signal(final SignalParameter... parameters) {
        if (task == null) return;
        task.run();
    }
}
