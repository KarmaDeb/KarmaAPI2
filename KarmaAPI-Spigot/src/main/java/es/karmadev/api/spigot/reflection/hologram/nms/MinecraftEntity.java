package es.karmadev.api.spigot.reflection.hologram.nms;

import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import org.bukkit.entity.Entity;

/**
 * Minecraft entity
 */
public interface MinecraftEntity {

    /**
     * Get the line attached to this
     * entity
     *
     * @return the entity line
     */
    HologramLine getLine();

    /**
     * Set if the entity is locked in its
     * current tick
     *
     * @param lock the entity lockTick status
     */
    void setLockTick(final boolean lock);

    /**
     * Move the entity
     *
     * @param x the entity new X position
     * @param y the entity new Y position
     * @param z the entity new Z position
     */
    void moveTo(final double x, final double y, final double z);

    /**
     * Get if the entity is alive
     *
     * @return if the entity is alive
     */
    boolean isAlive();

    /**
     * Kill the minecraft entity
     */
    void killEntity();

    /**
     * Get the minecraft entity ID
     *
     * @return the minecraft entity ID
     */
    int getEntityId();

    /**
     * Get the minecraft entity bukkit instance
     *
     * @return the minecraft entity as a bukkit
     * entity
     */
    Entity toBukkitEntity();
}
