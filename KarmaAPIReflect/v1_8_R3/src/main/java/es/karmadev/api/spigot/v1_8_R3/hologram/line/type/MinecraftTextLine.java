package es.karmadev.api.spigot.v1_8_R3.hologram.line.type;

import es.karmadev.api.spigot.reflection.hologram.Hologram;
import es.karmadev.api.spigot.reflection.hologram.line.type.TextHolderLine;
import es.karmadev.api.spigot.reflection.hologram.nms.MinecraftEntity;
import es.karmadev.api.spigot.reflection.hologram.nms.NameableEntity;
import es.karmadev.api.spigot.v1_8_R3.hologram.V1_8_R3HologramManager;
import es.karmadev.api.spigot.v1_8_R3.hologram.line.MinecraftHologramLine;

public class MinecraftTextLine extends MinecraftHologramLine implements TextHolderLine {

    private String text;

    /**
     * Create a new hologram line
     *
     * @param parent the parent hologram
     * @param text the hologram text to display
     */
    public MinecraftTextLine(final Hologram parent, final String text) {
        super(parent);
        this.text = text;
    }

    /**
     * Get the line text
     *
     * @return the text
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     * Set the line text
     *
     * @param text the text
     */
    @Override
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Get the hologram height
     *
     * @return the hologram height
     */
    @Override
    public double height() {
        return -1.25;
    }

    /**
     * Get the entity that represents this
     * line
     *
     * @return the entity line
     */
    @Override
    public NameableEntity getEntity() {
        return (NameableEntity) super.getEntity();
    }

    /**
     * Create the entity
     *
     * @return the entity
     */
    @Override
    protected MinecraftEntity makeEntity() {
        return V1_8_R3HologramManager.spawnArmorStand(world, x, y + height(), z, this);
    }
}
