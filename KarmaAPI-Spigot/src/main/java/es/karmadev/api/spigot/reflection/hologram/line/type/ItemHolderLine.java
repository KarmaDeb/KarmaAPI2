package es.karmadev.api.spigot.reflection.hologram.line.type;

import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
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
}
