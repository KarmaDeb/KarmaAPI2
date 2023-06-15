package es.karmadev.api.spigot.v1_8_R3.hologram;

import es.karmadev.api.spigot.reflection.hologram.Hologram;
import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.line.type.ItemHolderLine;
import es.karmadev.api.spigot.reflection.hologram.nms.NameableEntity;
import es.karmadev.api.spigot.reflection.hologram.nms.entity.MinecraftItem;
import es.karmadev.api.spigot.v1_8_R3.hologram.line.MinecraftHologramLine;
import es.karmadev.api.spigot.v1_8_R3.hologram.line.type.MinecraftItemLine;
import es.karmadev.api.spigot.v1_8_R3.hologram.line.type.MinecraftTextLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MinecraftHologram implements Hologram {

    private final UUID id;
    private final List<MinecraftHologramLine> lines = new ArrayList<>();
    private final List<UUID> canView = new ArrayList<>();
    private boolean globalVisibility = true;
    private boolean spawned = false;
    private World world;
    private double x,y,z;
    private int chunkX, chunkZ;

    /**
     * Create a new hologram
     *
     * @param name the hologram name
     * @param position the hologram position
     */
    public MinecraftHologram(final String name, final Location position) {
        id = UUID.nameUUIDFromBytes(("Hologram:" + name).getBytes());
        world = position.getWorld();
        x = position.getX();
        y = position.getY();
        z = position.getZ();

        int xFloor = (int) x;
        int zFloor = (int) z;
        chunkX = (xFloor == x) ? xFloor : (xFloor - (int) (Double.doubleToRawLongBits(x) >>> 63L));
        chunkZ = (zFloor == z) ? zFloor : (zFloor - (int) (Double.doubleToRawLongBits(z) >>> 63L));
    }

    /**
     * Get the hologram ID
     *
     * @return the hologram ID
     */
    @Override
    public UUID id() {
        return id;
    }

    /**
     * Spawn the hologram if it's
     * not spawned
     */
    @Override
    public void spawn() {
        if (!spawned && world.isChunkLoaded(chunkX, chunkZ)) {
            spawned = true;
            double y = this.y;
            MinecraftHologramLine previous = null;
            for (MinecraftHologramLine line : lines) {
                y -= line.height();
                if (previous != null) {
                    y -= 0.02;
                }

                previous = line;
                if (line.exists()) {
                    line.teleport(x, y, z);
                    continue;
                }

                line.spawn(world, x, y, z);
            }
        }
    }

    /**
     * Insert a text to the hologram
     *
     * @param index the index to insert at
     * @param text  the text
     * @return the created line
     */
    @Override
    public HologramLine insert(final int index, final String text) {
        if (index > lines.size()) {
            int diff = index - lines.size();
            for (int i = 0; i < diff; i++) {
                addLine("");
            }
        }

        MinecraftTextLine line = new MinecraftTextLine(this, text);
        lines.set(index, line);

        //line.spawn(world, x, y, z);

        return line;
    }

    /**
     * Insert an item to the hologram
     *
     * @param index the index to insert at
     * @param item  the item
     * @return the created line
     */
    @Override
    public ItemHolderLine insert(final int index, final ItemStack item) {
        return null;
    }

    /**
     * Insert a line
     *
     * @param index the line index
     * @param line  the line to insert
     * @return if the line was able to be inserted
     */
    @Override
    public boolean insert(final int index, final HologramLine line) {
        return false;
    }

    /**
     * Get the line at the specified
     * index
     *
     * @param index the line index
     * @return the line
     */
    @Override
    public Optional<HologramLine> get(final int index) {
        HologramLine instance = null;
        if (index < lines.size()) instance = lines.get(index);

        return Optional.ofNullable(instance);
    }

    /**
     * Get the index of a line
     *
     * @param line the line index
     * @return the line index
     */
    @Override
    public int indexOf(final HologramLine line) {
        int index = 0;

        boolean found = false;
        for (MinecraftHologramLine existing : lines) {
            if (existing.id() == line.id()) {
                found = true;
                break;
            }

            index++;
        }

        return (found ? index : -1);
    }

    /**
     * Remove a line
     *
     * @param index the line index to remove
     * @return the removed line
     */
    @Override
    public HologramLine remove(final int index) {
        if (index > lines.size()) return null;
        return lines.remove(index);
    }

    /**
     * Remove a line
     *
     * @param line the line to remove
     * @return if the line was able to be removed
     */
    @Override
    public boolean remove(final HologramLine line) {
        MinecraftHologramLine instanceToRemove = null;
        for (MinecraftHologramLine mhl : lines) {
            if (mhl.id() == line.id()) {
                instanceToRemove = mhl;
                break;
            }
        }

        if (instanceToRemove != null) return lines.remove(instanceToRemove);
        return false;
    }

    /**
     * Get the hologram size
     *
     * @return the hologram size
     */
    @Override
    public int size() {
        return lines.size();
    }

    /**
     * Clear the hologram
     */
    @Override
    public void clear() {
        lines.clear();
        refresh();
    }

    /**
     * Get the hologram line separator
     *
     * @return the line separator size
     */
    @Override
    public double lineSeparator() {
        if (lines.isEmpty()) return 0;
        double h = 0d;
        for (MinecraftHologramLine line : lines)
            h += line.height();

        h += 0.02 * (lines.size() - 1);
        return h;
    }

    /**
     * Get the hologram world
     *
     * @return the hologram world
     */
    @Override
    public World world() {
        return world;
    }

    /**
     * Set the hologram world
     *
     * @param world the new world
     */
    @Override
    public void setWorld(final World world) {
        this.world = world;
    }

    /**
     * Get the hologram X position
     *
     * @return the X position
     */
    @Override
    public double x() {
        return x;
    }

    /**
     * Set the hologram X
     *
     * @param x the new X position
     */
    @Override
    public void setX(final double x) {
        this.x = x;
        int xFloor = (int) x;
        chunkX = (xFloor == x) ? xFloor : (xFloor - (int) (Double.doubleToRawLongBits(x) >>> 63L));
    }

    /**
     * Get the hologram Y position
     *
     * @return the Y position
     */
    @Override
    public double y() {
        return y;
    }

    /**
     * Set the hologram Y
     *
     * @param y the new Y position
     */
    @Override
    public void setY(final double y) {
        this.y = y;
    }

    /**
     * Get the hologram Z position
     *
     * @return the Z position
     */
    @Override
    public double z() {
        return z;
    }

    /**
     * Set the hologram Z
     *
     * @param z the new Z position
     */
    @Override
    public void setZ(final double z) {
        this.z = z;
        int zFloor = (int) z;
        chunkZ = (zFloor == z) ? zFloor : (zFloor - (int) (Double.doubleToRawLongBits(z) >>> 63L));
    }

    /**
     * Update the hologram. For example, after
     * a line change
     */
    @Override
    public void update() {
        if (spawned && world.isChunkLoaded(chunkX, chunkZ)) {
            for (MinecraftHologramLine line : lines) {
                if (line instanceof MinecraftTextLine) {
                    MinecraftTextLine text = (MinecraftTextLine) line;
                    NameableEntity nameable = text.getEntity();

                    nameable.renameEntity(text.getText());
                }
                if (line instanceof MinecraftItemLine) {
                    MinecraftItemLine item = (MinecraftItemLine) line;
                    MinecraftItem itemEntity = item.getEntity();

                    itemEntity.setStack(item.item());
                }
            }
        }
    }

    /**
     * Refresh the hologram. For example, after
     * changing its location
     */
    @Override
    public void refresh() {
        if (spawned) {
            delete();
            spawn();
        }
    }

    /**
     * Get if the hologram is visible
     * to everyone
     *
     * @return the hologram visibility
     */
    @Override
    public boolean isVisible() {
        return globalVisibility;
    }

    /**
     * Set if the hologram is visible
     * to everyone
     *
     * @param visibleByEveryone the hologram new
     *                          visibility
     */
    @Override
    public void setVisible(final boolean visibleByEveryone) {
        globalVisibility = visibleByEveryone;
        if (visibleByEveryone) {
            show(Bukkit.getServer().getOnlinePlayers().toArray(new Player[0]));
        } else {
            hide(Bukkit.getServer().getOnlinePlayers().toArray(new Player[0]));
        }
    }

    /**
     * Show the hologram to the players
     *
     * @param players the players to show the
     *                hologram to
     */
    @Override
    public void show(final Player... players) {
        for (Player player : players) {
            if (player != null) {
                UUID id = player.getUniqueId();
                if (!canView.contains(id)) canView.add(id);
            }
        }
    }

    /**
     * Hide the hologram to the players
     *
     * @param players the players to hide the
     *                hologram to
     */
    @Override
    public void hide(final Player... players) {
        for (Player player : players) {
            if (player != null) {
                UUID id = player.getUniqueId();
                canView.remove(id);
            }
        }
    }

    /**
     * Get if the player can see the hologram
     *
     * @param player the player
     * @return if the player is able to see
     * the hologram
     */
    @Override
    public boolean canSee(final Player player) {
        return player != null && player.isOnline() && canView.contains(player.getUniqueId());
    }

    /**
     * Get the players that can see the
     * hologram
     *
     * @return the players that have view
     * access
     */
    @Override
    public Player[] viewers() {
        List<Player> players = new ArrayList<>();
        for (UUID id : canView) {
            Player instance = Bukkit.getPlayer(id);
            if (instance != null && instance.isOnline()) {
                players.add(instance);
            }
        }

        return players.toArray(new Player[0]);
    }

    /**
     * Delete the hologram
     */
    @Override
    public void delete() {
        if (spawned) {
            lines.forEach(MinecraftHologramLine::destroy);
            spawned = false;
        }
    }

    /**
     * Get if the hologram exists
     *
     * @return if the hologram exists
     */
    @Override
    public boolean exists() {
        return spawned;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<HologramLine> iterator() {
        List<HologramLine> rawType = new ArrayList<>(lines);
        return rawType.iterator();
    }
}
