package es.karmadev.api.spigot.security.permission;

import es.karmadev.api.security.permission.PermissionFactory;
import es.karmadev.api.security.permission.PermissionNode;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;

/**
 * Spigot permission factory
 */
public interface SpigotPermissionFactory extends PermissionFactory {

    /**
     * Create a permission
     *
     * @param name the permission name
     * @return the permission
     */
    @Override
    SpigotPermissionNode createPermission(final String name);

    /**
     * Create a permission
     *
     * @param name the permission name
     * @param group the permission group
     * @return the permission
     */
    SpigotPermissionNode createPermission(final String name, final PermissionDefault group);

    /**
     * Create a permission node
     *
     * @param name        the permission name
     * @param description the permission description
     * @return the permission
     */
    @Override
    SpigotPermissionNode createPermission(final String name, final String description);

    /**
     * Create a permission
     *
     * @param name the permission name
     * @param group the permission group
     * @param description the permission description
     * @return the permission
     */
    SpigotPermissionNode createPermission(final String name, final PermissionDefault group, final String description);

    /**
     * Create a permission node
     *
     * @param name        the permission name
     * @param description the permission description
     * @param children    the permission children
     * @return the permission
     */
    @Override
    SpigotPermissionNode createPermission(final String name, final String description, final Map<String, Boolean> children);

    /**
     * Create a permission node
     *
     * @param name        the permission name
     * @param group       the permission group
     * @param description the permission description
     * @param children    the permission children
     * @return the permission
     */
    SpigotPermissionNode createPermission(final String name, final PermissionDefault group, final String description, final Map<String, Boolean> children);
}
