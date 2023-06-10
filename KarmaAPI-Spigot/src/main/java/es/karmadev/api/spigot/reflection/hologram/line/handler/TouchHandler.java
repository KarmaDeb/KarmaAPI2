package es.karmadev.api.spigot.reflection.hologram.line.handler;

import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import org.bukkit.entity.Player;

/**
 * Hologram touch handler
 */
@SuppressWarnings("unused")
public interface TouchHandler {

    /**
     * When a player touches the line
     *
     * @param player the player who touch the
     *               line
     */
    void touch(final Player player);

    /**
     * Get the line
     *
     * @return the touched line
     */
    HologramLine line();

    /**
     * Unregister the handler
     *
     * @return if the handler was able to be
     * unregistered
     */
    boolean unregister();
}
