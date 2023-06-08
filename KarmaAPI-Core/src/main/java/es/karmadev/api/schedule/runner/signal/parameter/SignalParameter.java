package es.karmadev.api.schedule.runner.signal.parameter;

/**
 * Signal parameter
 */
public interface SignalParameter {

    /**
     * Get the signal parameter name
     *
     * @return the parameter name
     */
    String name();

    /**
     * Get the signal parameter value
     *
     * @return the parameter value
     */
    Object value();
}
