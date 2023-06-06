package es.karmadev.api.spigot.entity.trace.event;

import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when an entity is
 * hit on a ray trace
 */
public class RayTraceCollideEvent extends Event {

    private final static HandlerList handlerList = new HandlerList();
    private final Entity entity;
    private final Block block;
    private final HitPosition position;

    /**
     * Initialize the event
     *
     * @param hitEntity the entity
     * @param hitBlock the block
     * @param position the hit position
     * @throws IllegalStateException if the entity and block are null. At least one valid is needed
     */
    public RayTraceCollideEvent(final Entity hitEntity, final Block hitBlock, final HitPosition position) throws IllegalStateException {
        if (hitEntity == null && hitBlock == null) throw new IllegalStateException("Cannot initialize ray trace collide event. At least a valid entity or block is required, both null provided");

        this.entity = hitEntity;
        this.block = hitBlock;
        this.position = position;
    }

    /**
     * Get the hit entity
     *
     * @return the entity
     */
    public @Nullable Entity getEntity() {
        return entity;
    }

    /**
     * Get the hit block
     *
     * @return the block
     */
    public @Nullable Block getBlock() {
        return block;
    }

    /**
     * Get the hit position
     *
     * @return the hit position
     */
    public HitPosition getPosition() {
        return position;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
