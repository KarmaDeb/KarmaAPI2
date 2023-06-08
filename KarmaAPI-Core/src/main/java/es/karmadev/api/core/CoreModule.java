package es.karmadev.api.core;

/**
 * Core module
 */
public interface CoreModule {

    /**
     * Get the module name
     *
     * @return the module name
     */
    String getName();

    /**
     * Get if the module is protected
     *
     * @return if this is a protected module
     */
    boolean isProtected();
}
