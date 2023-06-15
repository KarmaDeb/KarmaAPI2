package es.karmadev.api.spigot.reflection.hologram;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.object.RuntimeModifier;
import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.line.type.ItemHolderLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * KarmaAPI simple hologram
 */
@SuppressWarnings("unused")
public interface Hologram extends Iterable<HologramLine> {

    /**
     * Get the hologram ID
     *
     * @return the hologram ID
     */
    UUID id();

    /**
     * Spawn the hologram if it's
     * not spawned
     */
    void spawn();

    /**
     * Append a text to the hologram
     *
     * @param text the text
     * @return the created line
     */
    @SuppressWarnings("UnusedReturnValue")
    default HologramLine addLine(final String text) {
        return insert(size() + 1, text);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @return the created line
     */
    default ItemHolderLine addLine(final Material item) {
        return insert(size() + 1, item);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @param amount the item amount
     * @return the created line
     */
    default ItemHolderLine addLine(final Material item, final int amount) {
        return insert(size() + 1, item, amount);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @param amount the item amount
     * @param data the item data
     * @return the created line
     */
    default ItemHolderLine addLine(final Material item, final int amount, final byte data) {
        return insert(size() + 1, item, amount, data);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @param amount the item amount
     * @param data the item data
     * @param metaModifier the item meta modifier
     * @return the created line
     */
    default ItemHolderLine addLine(final Material item, final int amount, final byte data, final RuntimeModifier<ItemMeta> metaModifier) {
        return insert(size() + 1, item, amount, data, metaModifier);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @param amount the item amount
     * @param data the item data
     * @param meta the item meta
     * @return the created line
     */
    default ItemHolderLine addLine(final Material item, final int amount, final byte data, final ItemMeta meta) {
        return insert(size() + 1, item, amount, data, meta);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @param amount the item amount
     * @param metaModifier the item meta modifier
     * @return the created line
     */
    default ItemHolderLine addLine(final Material item, final int amount, final RuntimeModifier<ItemMeta> metaModifier) {
        return insert(size() + 1, item, amount, metaModifier);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @param amount the item amount
     * @param meta the item meta
     * @return the created line
     */
    default ItemHolderLine addLine(final Material item, final int amount, final ItemMeta meta) {
        return insert(size() + 1, item, amount, meta);
    }

    /**
     * Append an item to the hologram
     *
     * @param item the item
     * @return the created line
     */
    default ItemHolderLine addLine(final ItemStack item) {
        return insert(size() + 1, item);
    }

    /**
     * Append a line
     *
     * @param line the line to append
     * @return if the line was able to be appended
     */
    default boolean append(final ItemHolderLine line) {
        return insert(size() + 1, line);
    }

    /**
     * Insert a text to the hologram
     *
     * @param index the index to insert at
     * @param text the text
     * @return the created line
     */
    HologramLine insert(final int index, final String text);

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @return the created line
     */
    default ItemHolderLine insert(final int index, final Material item) {
        return insert(index, item, 1, (byte) 0, (meta) -> meta);
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @param amount the item amount
     * @return the created line
     */
    default ItemHolderLine insert(final int index, final Material item, final int amount) {
        return insert(index, item, amount, (byte) 0, (meta) -> meta);
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @param amount the item amount
     * @param data the item data
     * @return the created line
     */
    default ItemHolderLine insert(final int index, final Material item, final int amount, final byte data) {
        return insert(index, item, amount, data, (meta) -> meta);
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @param amount the item amount
     * @param data the item data
     * @param metaModifier the item meta modifier
     * @return the created line
     */
    @SuppressWarnings("deprecation")
    default ItemHolderLine insert(final int index, final Material item, final int amount, final byte data, final RuntimeModifier<ItemMeta> metaModifier) {
        ItemStack stack = new ItemStack(item, Math.max(1, amount));
        if (data > 0) {
            try {
                stack = new ItemStack(item, Math.max(1, amount), data);
            } catch (Exception ex) {
                ExceptionCollector.catchException(Hologram.class, ex);
            }
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            ItemMeta tmpMeta = metaModifier.modify(meta);
            if (tmpMeta != null) meta = tmpMeta;
        }

        stack.setItemMeta(meta);
        //That's what the developer should do if we don't prove the method...
        return insert(index, stack);
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @param amount the item amount
     * @param data the item data
     * @param meta the item meta
     * @return the created line
     */
    default ItemHolderLine insert(final int index, final Material item, final int amount, final byte data, final ItemMeta meta) {
        return insert(index, item, amount, data, (createdMeta) -> meta);
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @param amount the item amount
     * @param metaModifier the item meta modifier
     * @return the created line
     */
    default ItemHolderLine insert(final int index, final Material item, final int amount, final RuntimeModifier<ItemMeta> metaModifier) {
        return insert(index, item, amount, (byte) 0, metaModifier);
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @param amount the item amount
     * @param meta the item meta
     * @return the created line
     */
    default ItemHolderLine insert(final int index, final Material item, final int amount, final ItemMeta meta) {
        return insert(index, item, amount, (createdMeta) -> meta);
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item the item
     * @return the created line
     */
    ItemHolderLine insert(final int index, final ItemStack item);

    /**
     * Insert a line
     *
     * @param index the line index
     * @param line the line to insert
     * @return if the line was able to be inserted
     */
    boolean insert(final int index, final HologramLine line);

    /**
     * Get the line at the specified
     * index
     *
     * @param index the line index
     * @return the line
     */
    Optional<HologramLine> get(final int index);

    /**
     * Get the index of a line
     *
     * @param line the line index
     * @return the line index
     */
    int indexOf(final HologramLine line);

    /**
     * Remove a line
     *
     * @param index the line index to remove
     * @return the removed line
     */
    HologramLine remove(final int index);

    /**
     * Remove a line
     *
     * @param line the line to remove
     * @return if the line was able to be removed
     */
    boolean remove(final HologramLine line);

    /**
     * Get the hologram size
     *
     * @return the hologram size
     */
    int size();

    /**
     * Clear the hologram
     */
    void clear();

    /**
     * Get the hologram line separator
     *
     * @return the line separator size
     */
    double lineSeparator();

    /**
     * Teleport the hologram
     *
     * @param location the location to teleport
     *                 the hologram at
     */
    default void teleport(final Location location) {
        if (location == null) return;

        World locationWorld = location.getWorld();
        if (locationWorld == null) return;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        teleport(locationWorld, x, y, z);
    }

    /**
     * Teleport the hologram
     *
     * @param x the hologram new X position
     * @param y the hologram new Y position
     * @param z the hologram new Z position
     */
    default void teleport(final double x, final double y, final double z) {
        teleport(world(), x, y, z);
    }

    /**
     * Teleport the hologram
     *
     * @param world the hologram new world
     * @param x the hologram new X position
     * @param y the hologram new Y position
     * @param z the hologram new Z position
     */
    default void teleport(final World world, final double x, final double y, final double z) {
        if (world == null) return;
        setWorld(world);
        setX(x);
        setY(y);
        setZ(z);
    }

    /**
     * Get the hologram world
     *
     * @return the hologram world
     */
    World world();

    /**
     * Set the hologram world
     *
     * @param world the new world
     */
    void setWorld(final World world);

    /**
     * Get the hologram X position
     *
     * @return the X position
     */
    double x();

    /**
     * Set the hologram X
     *
     * @param x the new X position
     */
    void setX(final double x);

    /**
     * Get the hologram Y position
     *
     * @return the Y position
     */
    double y();

    /**
     * Set the hologram Y
     *
     * @param y the new Y position
     */
    void setY(final double y);

    /**
     * Get the hologram Z position
     *
     * @return the Z position
     */
    double z();

    /**
     * Set the hologram Z
     *
     * @param z the new Z position
     */
    void setZ(final double z);

    /**
     * Update the hologram. For example, after
     * a line change
     */
    void update();

    /**
     * Refresh the hologram. For example, after
     * changing its location
     */
    void refresh();

    /**
     * Get if the hologram is visible
     * to everyone
     *
     * @return the hologram visibility
     */
    boolean isVisible();

    /**
     * Set if the hologram is visible
     * to everyone
     *
     * @param visibleByEveryone the hologram new
     *                          visibility
     */
    void setVisible(final boolean visibleByEveryone);

    /**
     * Show the hologram to the players
     *
     * @param players the players to show the
     *                hologram to
     */
    default void show(final Collection<Player> players) {
        show(players.toArray(new Player[0]));
    }

    /**
     * Show the hologram to the players
     *
     * @param players the players to show the
     *                hologram to
     */
    void show(final Player... players);

    /**
     * Hide the hologram to the players
     *
     * @param players the players to hide the
     *                hologram to
     */
    default void hide(final Collection<Player> players) {
        hide(players.toArray(new Player[0]));
    }

    /**
     * Hide the hologram to the players
     *
     * @param players the players to hide the
     *                hologram to
     */
    void hide(final Player... players);

    /**
     * Get if the player can see the hologram
     *
     * @param player the player
     * @return if the player is able to see
     * the hologram
     */
    boolean canSee(final Player player);

    /**
     * Get the players that can see the
     * hologram
     *
     * @return the players that have view
     * access
     */
    Player[] viewers();

    /**
     * Reset the visibility for everyone
     */
    default void resetVisibility() {
        resetVisibility(Bukkit.getServer().getOnlinePlayers().toArray(new Player[0]));
    }

    /**
     * Reset the visibility for the specified players
     *
     * @param players the players to reset
     *                visibility for
     */
    default void resetVisibility(final Collection<Player> players) {
        resetVisibility(players.toArray(new Player[0]));
    }

    /**
     * Reset the visibility for the specified players
     *
     * @param players the players to reset
     *                visibility for
     */
    default void resetVisibility(final Player... players) {
        boolean isVisible = isVisible();
        if (isVisible)
            show(players);
        else
            hide(players);
    }

    /**
     * Delete the hologram
     */
    void delete();

    /**
     * Get if the hologram exists
     *
     * @return if the hologram exists
     */
    boolean exists();
}
