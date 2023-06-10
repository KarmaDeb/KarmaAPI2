package es.karmadev.api.spigot.reflection.hologram.line.type;

import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.line.handler.PickupHandler;
import org.bukkit.inventory.ItemStack;

/**
 * Hologram item line
 */
public interface ItemHolderLine extends HologramLine {

    /**
     * Get the line item
     *
     * @return the item
     */
    ItemStack item();

    /**
     * Set the line pickup handler
     *
     * @param handler the pickup handler
     */
    void setPickupHandler(final PickupHandler handler);

    /**
     * Get the line pickup handler
     *
     * @return the line pickup handler
     */
    PickupHandler getPickupHandler();
}
