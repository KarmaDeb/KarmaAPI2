package es.karmadev.api.schedule.runner.signal;

import es.karmadev.api.schedule.runner.signal.parameter.SignalParameter;

/**
 * Signal handler
 */
public interface SignalHandler {

    /**
     * Emit a signal
     *
     * @param parameters the signal parameters
     */
    void signal(final SignalParameter... parameters);
}
