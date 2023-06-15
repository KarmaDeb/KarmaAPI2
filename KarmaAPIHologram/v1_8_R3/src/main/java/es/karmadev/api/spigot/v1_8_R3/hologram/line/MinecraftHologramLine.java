package es.karmadev.api.spigot.v1_8_R3.hologram.line;

import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.spigot.reflection.hologram.Hologram;
import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.line.TouchableLine;
import es.karmadev.api.spigot.reflection.hologram.line.handler.TouchHandler;
import es.karmadev.api.spigot.reflection.hologram.nms.MinecraftEntity;
import es.karmadev.api.spigot.reflection.hologram.nms.entity.MinecraftSlime;
import es.karmadev.api.spigot.v1_8_R3.hologram.MinecraftHologramManager;
import es.karmadev.api.spigot.v1_8_R3.hologram.entity.HologramSlime;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class MinecraftHologramLine implements HologramLine, TouchableLine {

    private static int globalId = 0;
    private final int id = ++globalId;
    protected final Hologram parent;
    private final List<TouchHandler> HANDLER_LIST = new ArrayList<>();

    protected MinecraftEntity entity;
    protected MinecraftSlime touchEntity;
    private boolean exists = false;
    protected World world;
    protected double x, y, z;
    private boolean touchable = false;

    /**
     * Create a new hologram line
     *
     * @param parent the parent hologram
     */
    public MinecraftHologramLine(final Hologram parent) {
        this.parent = parent;
    }

    /**
     * Get the line id
     *
     * @return the line id
     */
    @Override
    public int id() {
        return id;
    }

    /**
     * Get the parent hologram
     *
     * @return the parent hologram
     */
    @Override
    public Hologram parent() {
        return parent;
    }

    /**
     * Remove the line from the hologram
     */
    @Override
    public void remove() {
        parent.remove(this);
    }

    /**
     * Get if the line exists
     *
     * @return if the line exists
     */
    @Override
    public boolean exists() {
        return exists;
    }

    /**
     * Spawn the hologram line
     *
     * @param world the world to spawn at
     * @param x     the line position x
     * @param y     the line position y
     * @param z     the line position z
     */
    @Override
    public void spawn(final World world, final double x, final double y, final double z) {
        if (!exists) {
            exists = true;

            entity = makeEntity();
            //entity.renameEntity(text);
            entity.setLockTick(true);

            ObjectUtils.equalsIgnoreCase(this.world, world, (status) -> {
                if (!status) this.world = world;
            });
            ObjectUtils.equalsIgnoreCase(this.x, x, (status) -> {
                if (!status) this.x = x;
            });
            ObjectUtils.equalsIgnoreCase(this.y, y, (status) -> {
                if (!status) this.y = y;
            });
            ObjectUtils.equalsIgnoreCase(this.z, z, (status) -> {
                if (!status) this.z = z;
            });
        }
    }

    /**
     * Destroy the line (de-spawn)
     */
    @Override
    public void destroy() {
        if (exists) {
            entity.killEntity();
            exists = false;
        }
        if (touchable) {
            touchEntity.killEntity();
            touchable = false;

            HANDLER_LIST.clear();
        }
    }

    /**
     * Get the attached line entities
     *
     * @return the attached entities
     */
    @Override
    public int[] entities() {
        if (exists) {
            int[] entities = new int[]{entity.getEntityId()};
            if (touchable) {
                entities = Arrays.copyOf(entities, 2);
                entities[1] = touchEntity.getEntityId();
            }

            return entities;
        }

        return new int[0];
    }

    /**
     * Change the line world
     *
     * @param world the new line world
     * @return if the line was able to be
     * moved
     */
    @Override
    public boolean teleportAt(final World world) {
        if (exists) {
            this.world = world;
            destroy();
            spawn(new Location(world, x, y, z));
            return true;
        }

        return false;
    }

    /**
     * Teleport the line
     *
     * @param x the new X location
     * @param y the new Y location
     * @param z the new Z location
     * @return if the line was able to be
     * teleported
     */
    @Override
    public boolean teleport(final double x, final double y, final double z) {
        if (exists) {
            entity.moveTo(x, y + height(), z);
            this.x = x;
            this.y = y;
            this.z = z;
            return true;
        }

        return false;
    }

    /**
     * Get the line world
     *
     * @return the line world
     */
    @Override
    public World world() {
        return world;
    }

    /**
     * Get the line X position
     *
     * @return the X position
     */
    @Override
    public double x() {
        return x;
    }

    /**
     * Get the line Y position
     *
     * @return the Y position
     */
    @Override
    public double y() {
        return y;
    }

    /**
     * Get the line Z position
     *
     * @return the Z position
     */
    @Override
    public double z() {
        return z;
    }

    /**
     * Make this line a touchable line
     * if it wasn't already
     *
     * @return the touchable line instance
     */
    @Override
    public TouchableLine touchable() {
        if (!touchable) {
            touchable = true;
            HologramSlime slime = MinecraftHologramManager.spawnSlime(world, x, y - 1.49, z, this);

            entity.setLockTick(false); //Just in case
            slime.mount(entity);
            slime.setLockTick(true);
            entity.setLockTick(true);
            touchEntity = slime;
        }

        return this;
    }

    /**
     * Get if the line is a touchable
     * line
     *
     * @return if the line is touchable
     */
    @Override
    public boolean isTouchable() {
        return touchable;
    }

    /**
     * Get the entity that represents this
     * line
     *
     * @return the entity line
     */
    public MinecraftEntity getEntity() {
        return entity;
    }

    /**
     * Add a touch handler to the line
     *
     * @param handler the touch handler
     */
    @Override
    public void addTouchHandler(final TouchHandler handler) {
        if (touchable) {
            HANDLER_LIST.add(handler);
        }
    }

    /**
     * Remove a touch handler from the line
     *
     * @param handler the touch handler
     */
    @Override
    public void removeTouchHandler(final TouchHandler handler) {
        if (touchable) {
            HANDLER_LIST.remove(handler);
        }
    }

    /**
     * Get the touch handlers
     *
     * @return the touch handlers
     */
    @Override
    public TouchHandler[] handlers() {
        if (touchable) {
            return HANDLER_LIST.toArray(new TouchHandler[0]).clone();
        }

        return new TouchHandler[0];
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<TouchHandler> iterator() {
        List<TouchHandler> handlers = new ArrayList<>();

        if (touchable) {
            handlers.addAll(HANDLER_LIST);
        }

        return handlers.iterator();
    }

    /**
     * Create the entity
     *
     * @return the entity
     */
    protected abstract MinecraftEntity makeEntity();
}
