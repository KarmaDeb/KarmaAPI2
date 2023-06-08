package es.karmadev.api.security;

import es.karmadev.api.core.CoreModule;
import es.karmadev.api.security.permission.PermissionFactory;
import es.karmadev.api.security.permission.PermissionNode;

/**
 * KarmaAPI permission manager
 */
public abstract class PermissionManager<TNode extends PermissionNode<THolder>, THolder> implements CoreModule {

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
     * Get the permission factory
     *
     * @return the safe permission factory
     */
    public abstract PermissionFactory getFactory();

    /**
     * Tries to register a permission
     *
     * @param permission the permission to register
     * @return if the permission was able to be registered
     */
    public abstract boolean register(final TNode permission);

    /**
     * Get a permission by name
     *
     * @param name the permission name
     * @return the permissions matching the specified name
     */
    public abstract TNode[] getByName(final String name);

    /**
     * Get a permission by index
     *
     * @param index the permission index
     * @return the permission matching the specified index
     */
    public abstract TNode getByIndex(final int index);

    /**
     * Get if the holder has the specified permission
     *
     * @param holder the permission holder
     * @param node the permission node
     * @return if the holder has the permission
     */
    public abstract boolean hasPermission(final THolder holder, final TNode node);

    /**
     * Grant a permission
     *
     * @param holder the permission holder
     * @param node the permission node
     */
    public abstract void grantPermission(final THolder holder, final TNode node);

    /**
     * Revoke a permission
     *
     * @param holder the permission holder
     * @param node the permission node
     */
    public abstract void revokePermission(final THolder holder, final TNode node);
}
