package es.karmadev.api.security.permission;

import java.util.Map;

/**
 * Permission factory
 */
public interface PermissionFactory {

    /**
     * Create a permission
     *
     * @param name the permission name
     * @return the permission
     */
    PermissionNode createPermission(final String name);

    /**
     * Create a permission node
     *
     * @param name the permission name
     * @param description the permission description
     * @return the permission
     */
    PermissionNode createPermission(final String name, final String description);

    /**
     * Create a permission node
     *
     * @param name the permission name
     * @param description the permission description
     * @param children the permission children
     * @return the permission
     */
    PermissionNode createPermission(final String name, final String description, final Map<String, Boolean> children);
}
