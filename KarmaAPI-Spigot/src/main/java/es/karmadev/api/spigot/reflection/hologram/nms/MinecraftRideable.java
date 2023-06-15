package es.karmadev.api.spigot.reflection.hologram.nms;

/**
 * Minecraft rideable entity
 */
public interface MinecraftRideable extends MinecraftEntity {

    /**
     * Mount an entity
     *
     * @param entity the entity to mount
     */
    void mount(final MinecraftEntity entity);
}
