package es.karmadev.api.spigot.entity.global;

import es.karmadev.api.minecraft.client.GlobalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("unused")
public class BukkitPlayer extends GlobalPlayer {

    /**
     * Create a new bukkit player
     *
     * @param username the player name
     * @param nickname the player nickname
     * @param id the player uuid
     */
    public BukkitPlayer(final String username, final String nickname, final UUID id) {
        super(username, nickname, id);
    }

    /**
     * Create a new bukkit player
     *
     * @param username the player name
     * @param nickname the player nickname
     * @param id the player uuid
     * @param world the player world
     * @param x the player X position
     * @param y the player Y position
     * @param z the player Z position
     * @param yaw the player yaw direction
     * @param pitch the player pitch direction
     */
    public BukkitPlayer(final String username, final String nickname, final UUID id, final String world,
                        final double x, final double y, final double z, final float yaw, final float pitch) {
        super(username, nickname, id, world, x, y, z, yaw, pitch);
    }

    /**
     * Create a new bukkit player
     *
     * @param player the player
     */
    public BukkitPlayer(final Player player) {
        this(player.getName(),
                player.getPlayerListName(),
                player.getUniqueId(),
                player.getWorld().getName(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch());
    }

    /**
     * Get the player handler
     *
     * @return the player handler
     */
    @Override
    public Object getHandler() {
        return Bukkit.getServer().getOfflinePlayer(uuid);
    }

    /**
     * Get if the player is connected
     * to the server
     *
     * @return if the player is connected
     */
    @Override
    public boolean isOnline() {
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
        return player.isOnline();
    }

    /**
     * Move the player
     *
     * @param world the new world
     * @param x     the new X position
     * @param y     the new Y position
     * @param z     the new Z position
     */
    public void move(final String world, final double x, final double y, final double z) {
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
        if (!player.isOnline()) return;

        Player online = player.getPlayer();
        assert online != null;

        float yaw = online.getLocation().getYaw();
        float pitch = online.getLocation().getPitch();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Teleport the player
     *
     * @param world the new world
     * @param x     the new X position
     * @param y     the new Y position
     * @param z     the new Z position
     */
    @Override
    public void teleport(final String world, final double x, final double y, final double z) {
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
        if (!player.isOnline()) return;

        Player online = player.getPlayer();
        assert online != null;

        World worldInstance = Bukkit.getServer().getWorld(world);
        if (worldInstance == null) return;

        float yaw = online.getLocation().getYaw();
        float pitch = online.getLocation().getPitch();
        Location targetLocation = new Location(worldInstance, x, y, z);
        targetLocation.setYaw(yaw);
        targetLocation.setPitch(pitch);

        if (online.teleport(targetLocation)) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
