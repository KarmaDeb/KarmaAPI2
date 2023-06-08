package es.karmadev.api.security.permission;

import java.util.Map;

/**
 * KarmaAPI permission node
 */
public interface PermissionNode<THolder> {

    /**
     * Get the permission index
     *
     * @return the permission index
     */
    int getIndex();

    /**
     * Get the permission name
     *
     * @return the permission name
     */
    String getName();

    /**
     * Get the permission description
     *
     * @return the permission description
     */
    String getDescription();

    /**
     * Get the permission children
     *
     * @return the permission children
     */
    Map<String, Boolean> getChildren();
}