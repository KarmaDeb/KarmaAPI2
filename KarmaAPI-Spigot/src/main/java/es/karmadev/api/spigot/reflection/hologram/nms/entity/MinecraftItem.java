package es.karmadev.api.spigot.reflection.hologram.nms.entity;

import es.karmadev.api.spigot.reflection.hologram.nms.MinecraftEntity;
import es.karmadev.api.spigot.reflection.hologram.nms.MinecraftRideable;
import org.bukkit.inventory.ItemStack;

/**
 * Minecraft item
 */
@SuppressWarnings("unused")
public interface MinecraftItem extends MinecraftEntity, MinecraftRideable {

    /**
     * Set the item stack
     *
     * @param stack the stack
     */
    void setStack(final ItemStack stack);

    /**
     * Set if players should be able to
     * pick up this item
     *
     * @param allow if players/entities are allowed to
     *              pickup
     */
    void allowPickup(final boolean allow);

    /**
     * Get the minecraft item stack
     *
     * @return the item stack
     */
    Object getStack();
}
