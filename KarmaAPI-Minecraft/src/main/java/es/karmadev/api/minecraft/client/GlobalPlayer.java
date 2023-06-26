package es.karmadev.api.minecraft.client;

import es.karmadev.api.minecraft.client.exception.NonAvailableException;

import java.util.UUID;

/**
 * Minecraft player object
 */
@SuppressWarnings("unused")
public abstract class GlobalPlayer {

    protected final String username;
    protected final String nickname;
    protected final UUID uuid;

    protected String world = null;
    protected double x, y, z = Double.MIN_VALUE;
    protected float yaw, pitch = Float.MIN_VALUE;

    /**
     * Create a new global player instance
     *
     * @param username the player name
     * @param nickname the player nickname
     * @param id the player uuid
     */
    protected GlobalPlayer(final String username, final String nickname, final UUID id) {
        this.username = username;
        this.nickname = nickname;
        this.uuid = id;
    }

    /**
     * Create a new global player instance
     *
     * @param username the player name
     * @param nickname the player nickname
     * @param id the player uuid
     * @param world the player world
     * @param x the player x position
     * @param y the player y position
     * @param z the player z position
     * @param yaw the player yaw position
     * @param pitch the player pitch position
     */
    protected GlobalPlayer(final String username, final String nickname, final UUID id, final String world, final double x, final double y, final double z, final float yaw, final float pitch) {
        this(username, nickname, id);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Get the player name
     *
     * @return the player name
     */
    public final String getUsername() {
        return username;
    }

    /**
     * Get the player nickname
     *
     * @return the player nickname
     */
    public final String getNickname() {
        return nickname;
    }

    /**
     * Get the player UUID
     *
     * @return the player UUID
     */
    public final UUID getUUID() {
        return uuid;
    }

    /**
     * Get the player world
     *
     * @return the player world
     * @throws NonAvailableException if the current instance does not support
     * this method
     */
    public final String getWorld() throws NonAvailableException {
        if (world == null) throw new NonAvailableException("world");
        return world;
    }

    /**
     * Get the player X position
     *
     * @return X position
     * @throws NonAvailableException if the current instance does not support
     * this method
     */
    public final double getX() throws NonAvailableException {
        if (x == Double.MIN_VALUE) throw new NonAvailableException("x");
        return x;
    }

    /**
     * Get the player Y position
     *
     * @return Y position
     * @throws NonAvailableException if the current instance does not support
     * this method
     */
    public final double getY() throws NonAvailableException {
        if (y == Double.MIN_VALUE) throw new NonAvailableException("y");
        return y;
    }

    /**
     * Get the player Z position
     *
     * @return Z position
     * @throws NonAvailableException if the current instance does not support
     * this method
     */
    public final double getZ() throws NonAvailableException {
        if (z == Double.MIN_VALUE) throw new NonAvailableException("z");
        return z;
    }

    /**
     * Get the player yaw axis
     *
     * @return yaw axis
     * @throws NonAvailableException if the current instance does not support
     * this method
     */
    public final float getYaw() throws NonAvailableException {
        if (yaw == Float.MIN_VALUE) throw new NonAvailableException("yaw");
        return yaw;
    }

    /**
     * Get the player pitch axis
     *
     * @return pitch axis
     * @throws NonAvailableException if the current instance does not support
     * this method
     */
    public final float getPitch() throws NonAvailableException {
        if (pitch == Float.MIN_VALUE) throw new NonAvailableException("pitch");
        return pitch;
    }

    /**
     * Get the player handler
     *
     * @return the player handler
     */
    public abstract Object getHandler();

    /**
     * Get if the player is connected
     * to the server
     *
     * @return if the player is connected
     */
    public abstract boolean isOnline();

    /**
     * Teleport the player
     *
     * @param world the new world
     * @param x the new X position
     * @param y the new Y position
     * @param z the new Z position
     * @throws NonAvailableException if the current instance does not support
     * this method
     */
    public abstract void teleport(final String world, final double x, final double y, final double z) throws NonAvailableException;
}
