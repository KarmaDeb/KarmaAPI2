package es.karmadev.api.spigot.security.permission;

import es.karmadev.api.security.permission.PermissionNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.PermissionDefault;

/**
 * Spigot permission node
 */
public interface SpigotPermissionNode extends PermissionNode<OfflinePlayer> {

    /**
     * Get the permission default group
     *
     * @return the permission default group
     */
    PermissionDefault defaultGroup();
}
