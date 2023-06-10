package es.karmadev.api.spigot.reflection.hologram.nms;

/**
 * Minecraft nameable entity
 */
public interface NameableEntity extends MinecraftEntity {

    /**
     * Rename the entity
     *
     * @param name the entity new name
     */
    void renameEntity(final String name);

    /**
     * Get the entity rename
     *
     * @return the new entity name
     */
    String entityRename();
}
