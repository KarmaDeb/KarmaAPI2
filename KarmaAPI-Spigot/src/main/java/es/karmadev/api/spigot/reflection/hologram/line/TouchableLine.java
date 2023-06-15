package es.karmadev.api.spigot.reflection.hologram.line;

import es.karmadev.api.spigot.reflection.hologram.line.handler.TouchHandler;

/**
 * Touchable hologram line
 */
@SuppressWarnings("unused")
public interface TouchableLine extends Iterable<TouchHandler> {

    /**
     * Add a touch handler to the line
     *
     * @param handler the touch handler
     */
    void addTouchHandler(final TouchHandler handler);

    /**
     * Remove a touch handler from the line
     *
     * @param handler the touch handler
     */
    void removeTouchHandler(final TouchHandler handler);

    /**
     * Get the touch handlers
     *
     * @return the touch handlers
     */
    TouchHandler[] handlers();
}
