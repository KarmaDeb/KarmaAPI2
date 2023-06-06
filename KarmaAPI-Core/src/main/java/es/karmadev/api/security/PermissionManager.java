package es.karmadev.api.security;

import es.karmadev.api.core.CoreModule;
import es.karmadev.api.security.permission.PermissionNode;

/**
 * KarmaAPI permission manager
 */
public abstract class PermissionManager implements CoreModule {

    /**
     * Get the module name
     *
     * @return the module name
     */
    @Override
    public final String getName() {
        return "permissions";
    }

    /**
     * Tries to register a permission
     *
     * @param permission the permission to register
     * @return if the permission was able to be registered
     */
    public abstract boolean register(final PermissionNode permission);

    /**
     * Get a permission by name
     *
     * @param name the permission name
     * @return the permissions matching the specified name
     */
    public abstract PermissionNode[] getByName(final String name);

    /**
     * Get a permission by index
     *
     * @param index the permission index
     * @return the permission matching the specified index
     */
    public abstract PermissionNode getByIndex(final int index);
}
