package es.karmadev.api.minecraft.client.factory;

import java.util.UUID;

/**
 * Player context
 */
@SuppressWarnings("unused")
public final class PlayerContext {

    private String username;
    private String nickname;
    private UUID uuid;

    private String world = null;
    private double x, y, z = Double.MIN_VALUE;
    private float yaw, pitch = Float.MIN_VALUE;
    private Object instance;

    /**
     * Create a new player context instance
     */
    public PlayerContext() {}

    /**
     * Set the username the player context has
     *
     * @param username the username
     * @return the player context
     */
    public PlayerContext withUsername(final String username) {
        this.username = username;
        return this;
    }

    /**
     * Set the nickname the player context has
     *
     * @param nickname the nickname
     * @return the player context
     */
    public PlayerContext withNickname(final String nickname) {
        this.nickname = nickname;
        return this;
    }

    /**
     * Set the UUID the player context has
     *
     * @param id the UUID
     * @return the player context
     */
    public PlayerContext withUID(final UUID id) {
        this.uuid = id;
        return this;
    }

    /**
     * Set the world the player context is at
     *
     * @param world the world
     * @return the player context
     */
    public PlayerContext atWorld(final String world) {
        this.world = world;
        return this;
    }

    /**
     * Set the position of the player context
     *
     * @param x the X position
     * @param y the Y position
     * @param z the Z position
     * @return the player context
     */
    public PlayerContext atPosition(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Set the direction the player context
     * is looking at
     *
     * @param yaw the direction yaw
     * @param pitch the direction pitch
     * @return the player context
     */
    public PlayerContext lookingAt(final float yaw, final float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return this;
    }

    /**
     * Set the player context instance
     *
     * @param playerInstance the player instance
     * @return the player context
     */
    public PlayerContext usingInstance(final Object playerInstance) {
        this.instance = playerInstance;
        return this;
    }

    /**
     * Get the player name
     *
     * @return the player name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the player nickname
     *
     * @return the player nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Get the player UUID
     *
     * @return the player UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the player world
     *
     * @return the player world
     */
    public String getWorld() {
        return world;
    }

    /**
     * Get the player X position
     *
     * @return X position
     */
    public double getX() {
        return x;
    }

    /**
     * Get the player Y position
     *
     * @return Y position
     */
    public double getY() {
        return y;
    }

    /**
     * Get the player Z position
     *
     * @return Z position
     */
    public double getZ() {
        return z;
    }

    /**
     * Get the player yaw axis
     *
     * @return yaw axis
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Get the player pitch axis
     *
     * @return pitch axis
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Get the player handler
     *
     * @return the player handler
     */
    public Object getHandler() {
        return instance;
    }
}
