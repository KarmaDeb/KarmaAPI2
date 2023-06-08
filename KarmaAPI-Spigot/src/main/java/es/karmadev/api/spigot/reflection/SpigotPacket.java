package es.karmadev.api.spigot.reflection;

import org.bukkit.entity.Player;

/**
 * Spigot packet
 */
public interface SpigotPacket {

    /**
     * Send the packet to a player
     *
     * @param player the player to send
     *               the packet to
     */
    void send(final Player player);
}
