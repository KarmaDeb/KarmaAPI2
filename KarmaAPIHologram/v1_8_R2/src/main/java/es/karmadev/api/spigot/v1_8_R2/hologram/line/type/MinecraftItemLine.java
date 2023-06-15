package es.karmadev.api.spigot.v1_8_R2.hologram.line.type;

import es.karmadev.api.spigot.reflection.hologram.Hologram;
import es.karmadev.api.spigot.reflection.hologram.line.handler.PickupHandler;
import es.karmadev.api.spigot.reflection.hologram.line.type.ItemHolderLine;
import es.karmadev.api.spigot.reflection.hologram.nms.MinecraftEntity;
import es.karmadev.api.spigot.reflection.hologram.nms.entity.MinecraftItem;
import es.karmadev.api.spigot.v1_8_R2.hologram.MinecraftHologramManager;
import es.karmadev.api.spigot.v1_8_R2.hologram.line.MinecraftHologramLine;
import org.bukkit.inventory.ItemStack;

public class MinecraftItemLine extends MinecraftHologramLine implements ItemHolderLine {

    private final ItemStack item;
    private PickupHandler handler = PickupHandler.EMPTY;

    /**
     * Create the hologram line
     *
     * @param hologram the hologram holding this line
     * @param item the item to display
     */
    public MinecraftItemLine(final Hologram hologram, final ItemStack item) {
        super(hologram);
        this.item = item;
    }

    /**
     * Get the line item
     *
     * @return the item
     */
    @Override
    public ItemStack item() {
        return item;
    }

    /**
     * Set the line pickup handler
     *
     * @param handler the pickup handler
     */
    @Override
    public void setPickupHandler(final PickupHandler handler) {
        this.handler = handler;
    }

    /**
     * Get the line pickup handler
     *
     * @return the line pickup handler
     */
    @Override
    public PickupHandler getPickupHandler() {
        return handler;
    }

    /**
     * Get the hologram height
     *
     * @return the hologram height
     */
    @Override
    public double height() {
        return -1.48;
    }

    /**
     * Get the entity that represents this
     * line
     *
     * @return the entity line
     */
    @Override
    public MinecraftItem getEntity() {
        return (MinecraftItem) super.getEntity();
    }

    /**
     * Create the entity
     *
     * @return the entity
     */
    @Override
    protected MinecraftEntity makeEntity() {
        return MinecraftHologramManager.spawnItem(world, x, y + height(), z, item, this);
    }
}
