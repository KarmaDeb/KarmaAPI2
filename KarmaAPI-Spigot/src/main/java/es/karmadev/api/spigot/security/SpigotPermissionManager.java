package es.karmadev.api.spigot.security;

import es.karmadev.api.security.PermissionManager;
import es.karmadev.api.spigot.security.permission.SpigotPermissionNode;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SpigotPermissionManager extends PermissionManager<SpigotPermissionNode, OfflinePlayer> {

    private final Set<SpigotPermissionNode> permissions = ConcurrentHashMap.newKeySet();

    /**
     * Tries to register a permission
     *
     * @param permission the permission to register
     * @return if the permission was able to be registered
     */
    @Override
    public boolean register(final SpigotPermissionNode permission) {
        if (permissions.stream().noneMatch((node) -> node.getIndex() == permission.getIndex())) {
            return permissions.add(permission);
        }

        return false;
    }

    /**
     * Get a permission by name
     *
     * @param name the permission name
     * @return the permissions matching the specified name
     */
    @Override
    public SpigotPermissionNode[] getByName(final String name) {
        if (name == null) return new SpigotPermissionNode[0];

        List<SpigotPermissionNode> nodes = new ArrayList<>();
        permissions.forEach((node) -> {
            if (node != null && node.getName() != null && node.getName().equals(name)) {
                nodes.add(node);
            }
        });

        return nodes.toArray(new SpigotPermissionNode[0]);
    }

    /**
     * Get a permission by index
     *
     * @param index the permission index
     * @return the permission matching the specified index
     */
    @Override
    public SpigotPermissionNode getByIndex(final int index) {
        return permissions.stream().filter((node) -> node != null && node.getIndex() == index).findAny().orElse(null);
    }
}
