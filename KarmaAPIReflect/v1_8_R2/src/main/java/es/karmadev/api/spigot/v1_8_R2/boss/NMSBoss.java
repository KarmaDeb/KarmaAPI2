package es.karmadev.api.spigot.v1_8_R2.boss;

import es.karmadev.api.minecraft.text.Colorize;
import es.karmadev.api.spigot.reflection.bossbar.nms.Boss;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NMSBoss implements Boss {

    private final EntityWither wither;
    private final Set<UUID> players = ConcurrentHashMap.newKeySet();

    public NMSBoss() {
        MinecraftServer server = MinecraftServer.getServer();
        World world = server.getWorld();

        //sexy
        wither = new EntityWither(world);
    }

    /**
     * Add a player to the wither view
     *
     * @param player the player to add
     */
    @Override
    public void add(final Player player) {
        if (players.add(player.getUniqueId())) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(wither);
            CraftPlayer craft = (CraftPlayer) player;
            EntityPlayer nmsPlayer = craft.getHandle();

            nmsPlayer.playerConnection.sendPacket(packet);
        }
    }

    /**
     * Remove a player from the
     * wither view
     *
     * @param player the player
     */
    @Override
    public void remove(final Player player) {
        if (players.remove(player.getUniqueId())) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(wither.getId());
            CraftPlayer craft = (CraftPlayer) player;
            EntityPlayer nmsPlayer = craft.getHandle();

            nmsPlayer.playerConnection.sendPacket(packet);
        }
    }

    /**
     * Create the wither object
     *
     * @param position the wither position
     * @param name     the wither name
     */
    @Override
    public void createWither(final Location position, final String name) {
        CraftWorld cw = (CraftWorld) position.getWorld();
        WorldServer ws = cw.getHandle();

        boolean destroy = false;
        if (!wither.world.equals(ws)) {
            wither.world = ws;
            destroy = true;
        }

        wither.setCustomName(Colorize.colorize(name));
        wither.setCustomNameVisible(true);
        wither.setPosition(position.getX(), position.getY(), position.getZ());

        update(destroy);
    }

    /**
     * Set the wither name
     *
     * @param name the wither name
     */
    @Override
    public void setName(final String name) {
        wither.setCustomName(Colorize.colorize(name));
        wither.setCustomNameVisible(true);

        update(false);
    }

    /**
     * Destroy the wither
     */
    @Override
    public void destroyWither() {
        for (UUID id : players) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) continue;

            remove(player);
        }

        players.clear();
        wither.setHealth(0f);
        wither.world = null;
        wither.dead = true;
        wither.valid = false;

        //"invalidate" wither
    }

    /**
     * Teleport the wither
     *
     * @param position the new wither position
     */
    @Override
    public void teleport(final Location position) {
        CraftWorld cw = (CraftWorld) position.getWorld();
        WorldServer ws = cw.getHandle();

        boolean destroy = false;
        if (!wither.world.equals(ws)) {
            wither.world = ws;
            destroy = true;
        }
        wither.setPosition(position.getX(), position.getY(), position.getZ());

        update(destroy);
    }

    /**
     * Set the wither health
     *
     * @param healthScale the health
     */
    @Override
    public void setHealth(final double healthScale) {
        float baseHealth = (float) healthScale * 300;
        wither.setHealth(baseHealth);

        update(false);
    }

    /**
     * Get if the wither has containers
     * or not
     *
     * @return if the wither has containers
     */
    @Override
    public boolean isEmpty() {
        return players.isEmpty();
    }

    private void update(final boolean destroyAndAdd) {
        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true);

        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;

            CraftPlayer craft = (CraftPlayer) player;
            EntityPlayer nmsPlayer = craft.getHandle();

            if (destroyAndAdd) {
                remove(player);
                add(player);
            } else {
                nmsPlayer.playerConnection.sendPacket(meta);
            }
        }
    }
}
