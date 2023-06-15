package es.karmadev.api.spigot.reflection.hologram.line.handler;

import org.bukkit.entity.Player;

/**
 * Hologram item pickup handler
 */
public interface PickupHandler {

    /**
     * When a player pickups the item
     *
     * @param player the player
     */
    void onPickup(final Player player);

    /**
     * Empty pickup handler
     */
    PickupHandler EMPTY = player -> {};
}
