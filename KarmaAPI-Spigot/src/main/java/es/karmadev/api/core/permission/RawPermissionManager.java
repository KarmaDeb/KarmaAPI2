package es.karmadev.api.core.permission;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.KarmaPlugin;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.database.DatabaseManager;
import es.karmadev.api.database.model.JsonDatabase;
import es.karmadev.api.database.model.json.JsonConnection;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.security.permission.PermissionFactory;
import es.karmadev.api.spigot.security.SpigotPermissionManager;
import es.karmadev.api.spigot.security.permission.SpigotPermissionNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.util.*;

/**
 * Raw permission manager
 */
public class RawPermissionManager extends SpigotPermissionManager {

    private final Map<UUID, BitSet> permissions = new Hashtable<>();
    private final Map<UUID, PermissionAttachment> attachments = new Hashtable<>();

    private final KarmaPlugin plugin;

    /**
     * Initialize the raw permission manager
     *
     * @param owner the permission manager owner
     */
    public RawPermissionManager(final KarmaPlugin owner) {
        this.plugin = owner;
    }

    /**
     * Get the permission factory
     *
     * @return the safe permission factory
     */
    @Override
    public PermissionFactory getFactory() {
        return null;
    }

    /**
     * Get if the holder has the specified permission
     *
     * @param player        the permission holder
     * @param node          the permission node
     * @return if the holder has the permission
     */
    @Override
    public boolean hasPermission(final OfflinePlayer player, final SpigotPermissionNode node) {
        if (player == null) return false;
        UUID id = player.getUniqueId();

        if (ObjectUtils.isNullOrEmpty(id)) return false;
        APISource source = KarmaKore.INSTANCE();
        if (source == null) return false;

        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(null);
        if (database == null) return false;

        BitSet set = getBitSet(id);
        if (set == null) return false;

        return set.get(node.getIndex());
    }

    /**
     * Grant a permission
     *
     * @param player        the permission holder
     * @param node          the permission node
     */
    @Override
    public void grantPermission(final OfflinePlayer player, final SpigotPermissionNode node) {
        if (player == null) return;
        UUID id = player.getUniqueId();

        if (ObjectUtils.isNullOrEmpty(id)) return;
        APISource source = KarmaKore.INSTANCE();
        if (source == null) return;

        BitSet set = getBitSet(id);
        if (set == null) return;

        set.set(node.getIndex());
        if (player.isOnline()) {
            Player online = player.getPlayer();
            if (online == null) return;

            PermissionAttachment attachment = attachments.computeIfAbsent(id, (a) -> online.addAttachment(plugin));
            attachment.setPermission(node.getName(), true);
            for (Map.Entry<String, Boolean> child : node.getChildren().entrySet()) {
                if (child.getValue()) {
                    attachment.setPermission(child.getKey(), true);
                }
            }
        }

        permissions.put(id, set);
        appendPermission(id, node);
    }

    /**
     * Revoke a permission
     *
     * @param player        the permission holder
     * @param node          the permission node
     */
    @Override
    public void revokePermission(final OfflinePlayer player, final SpigotPermissionNode node) {
        if (player == null) return;
        UUID id = player.getUniqueId();

        if (ObjectUtils.isNullOrEmpty(id)) return;
        APISource source = KarmaKore.INSTANCE();
        if (source == null) return;

        BitSet set = getBitSet(id);
        if (set == null) return;

        set.clear(node.getIndex());
        if (player.isOnline()) {
            Player online = player.getPlayer();
            if (online == null) return;

            PermissionAttachment attachment = attachments.computeIfAbsent(id, (a) -> online.addAttachment(plugin));
            attachment.unsetPermission(node.getName());
            for (Map.Entry<String, Boolean> child : node.getChildren().entrySet()) {
                if (child.getValue()) {
                    attachment.setPermission(child.getKey(), true);
                }
            }
        }

        permissions.put(id, set);
        deletePermission(id, node);
    }

    private BitSet getBitSet(final UUID id) {
        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(null);
        if (database == null) return null;

        String idString = id.toString().replaceAll("-", "");
        JsonConnection connection = database.grabConnection("permissions" + File.pathSeparator + idString);

        BitSet set = permissions.computeIfAbsent(id, (s) -> new BitSet());
        if (set.isEmpty()) {
            JsonConnection grantedTable = connection.createTable("granted");
            JsonConnection deniedTable = connection.createTable("denied");

            List<Number> granted = grantedTable.getNumberList("permissions");
            List<Number> denied = deniedTable.getNumberList("permissions");

            for (Number number : granted) {
                set.set(number.intValue(), true); //The number is the permission index (PermissionNode#getIndex)
            }
            for (Number number : denied) {
                set.set(number.intValue(), false);
            }
        }

        return set;
    }

    private void appendPermission(final UUID id, final SpigotPermissionNode node) {
        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(null);
        if (database == null) return;

        String idString = id.toString().replaceAll("-", "");
        JsonConnection connection = database.grabConnection("permissions" + File.pathSeparator + idString);

        JsonConnection grantedTable = connection.createTable("granted");
        JsonConnection deniedTable = connection.createTable("denied");

        List<Number> granted = grantedTable.getNumberList("permissions");
        if (!granted.contains(node.getIndex())) granted.add(node.getIndex());

        List<Number> denied = deniedTable.getNumberList("permissions");
        if (denied.contains(node.getIndex())) denied.remove(node.getIndex());

        grantedTable.setNumberList("permissions", granted);
        deniedTable.setNumberList("permissions", denied);
        connection.save();
    }

    private void deletePermission(final UUID id, final SpigotPermissionNode node) {
        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(null);
        if (database == null) return;

        String idString = id.toString().replaceAll("-", "");
        JsonConnection connection = database.grabConnection("permissions" + File.pathSeparator + idString);

        JsonConnection grantedTable = connection.createTable("granted");
        JsonConnection deniedTable = connection.createTable("denied");

        List<Number> granted = grantedTable.getNumberList("permissions");
        if (granted.contains(node.getIndex())) granted.remove(node.getIndex());

        List<Number> denied = deniedTable.getNumberList("permissions");
        if (!denied.contains(node.getIndex())) denied.add(node.getIndex());

        grantedTable.setNumberList("permissions", granted);
        deniedTable.setNumberList("permissions", denied);
        connection.save();
    }

    /**
     * Get if the module is protected
     *
     * @return if this is a protected module
     */
    @Override
    public boolean isProtected() {
        return true;
    }
}
