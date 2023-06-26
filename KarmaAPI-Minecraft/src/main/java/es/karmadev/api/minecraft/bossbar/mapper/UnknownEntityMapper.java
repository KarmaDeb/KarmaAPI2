package es.karmadev.api.minecraft.bossbar.mapper;

import java.util.UUID;
import java.util.function.Function;

/**
 * Unknown entity mapper
 * @deprecated Not sure if this is still needed
 */
@SuppressWarnings("unused")
@Deprecated
public class UnknownEntityMapper<Entity> {

    private final Entity entity;
    private final Function<Entity, UUID> mapFunction;

    /**
     * Initialize the unknown entity mapper
     *
     * @param entity the entity to map
     * @param mapFunction the map function
     */
    public UnknownEntityMapper(final Entity entity, final Function<Entity, UUID> mapFunction) {
        this.entity = entity;
        this.mapFunction = mapFunction;
    }


    /**
     * Get the entity to map
     *
     * @return the entity to map
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Map the entity
     *
     * @return the mapped value
     */
    public UUID map() {
        return mapFunction.apply(entity);
    }
}
