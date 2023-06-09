package es.karmadev.api.spigot.reflection;

import es.karmadev.api.core.KarmaPlugin;
import es.karmadev.api.database.DatabaseEngine;
import es.karmadev.api.database.DatabaseManager;
import es.karmadev.api.database.model.JsonDatabase;
import es.karmadev.api.database.model.json.JsonConnection;
import es.karmadev.api.spigot.reflection.hologram.Hologram;
import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.line.type.ItemHolderLine;
import es.karmadev.api.spigot.reflection.hologram.line.type.TextHolderLine;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

/**
 * KarmaAPI hologram manager
 */
@SuppressWarnings("unused")
public abstract class HologramManager {

    private static HologramManager instance = HologramManager.newInstance();
    protected final KarmaPlugin plugin;

    private HologramManager() { plugin = null; }

    /**
     * Initialize the hologram manager
     *
     * @param plugin the plugin owning the hologram
     *               manager
     */
    public HologramManager(final KarmaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Create a hologram
     *
     * @param name the hologram name
     * @param location the hologram location
     * @return the created hologram
     */
    public abstract Hologram createHologram(final String name, final Location location);

    /**
     * Save a hologram
     *
     * @param hologram the hologram to save
     * @return the hologram storage UUID
     */
    public UUID saveHologram(final Hologram hologram) {
        UUID rId = UUID.randomUUID();

        if (plugin == null || hologram == null) return rId;
        UUID id = hologram.id();

        DatabaseEngine engine = DatabaseManager.getEngine("json").orElse(null);
        if (!(engine instanceof JsonDatabase)) return rId;

        JsonDatabase database = (JsonDatabase) engine;
        JsonConnection connection = database.grabConnection("holograms" + File.pathSeparator + id.toString().replace("-", ""));

        JsonConnection textLines = connection.createTable("text");
        JsonConnection itemLines = connection.createTable("items");

        int latestIndex = 0;
        for (HologramLine line : hologram) {
            int index = hologram.indexOf(line);
            String indexName = String.valueOf(index);
            latestIndex = Math.max(latestIndex, index);

            if (line instanceof ItemHolderLine) {
                ItemHolderLine item = (ItemHolderLine) line;

                if (textLines.hasTable(indexName)) textLines.removeTable(indexName);
                JsonConnection itemTable = itemLines.createTable(indexName);

                ItemStack stack = item.item();
                itemTable.set("item", stack.serialize());
                itemTable.set("world", item.world().getUID().toString());
                itemTable.set("x", item.x());
                itemTable.set("y", item.y());
                itemTable.set("z", item.z());
                itemTable.set("height", item.height());
                itemTable.set("touchable", item.isTouchable());
            }

            if (line instanceof TextHolderLine) {
                TextHolderLine text = (TextHolderLine) line;

                if (itemLines.hasTable(indexName)) itemLines.removeTable(indexName);
                JsonConnection textTable = textLines.createTable(indexName);

                textTable.set("text", text.getText());
                textTable.set("world", text.world().getUID().toString());
                textTable.set("x", text.x());
                textTable.set("y", text.y());
                textTable.set("z", text.z());
                textTable.set("height", text.height());
                textTable.set("touchable", text.isTouchable());
            }
        }

        for (String key : itemLines.getKeys()) {
            try {
                int index = Integer.parseInt(key);
                if (index > latestIndex && itemLines.getType(key).equals("table")) itemLines.removeTable(key);
            } catch (NumberFormatException ignored) {}
        }
        for (String key : textLines.getKeys()) {
            try {
                int index = Integer.parseInt(key);
                if (index > latestIndex && textLines.getType(key).equals("table")) textLines.removeTable(key);
            } catch (NumberFormatException ignored) {}
        }

        connection.save();
        return id;
    }

    /**
     * Get the plugin owning the hologram
     * manager
     *
     * @return the plugin that owns this
     * hologram manager
     */
    public final KarmaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Register this instance as the
     * hologram manager
     */
    public final void register() {
        instance = this;
    }

    /**
     * Get the hologram manager instance
     *
     * @return the hologram manager
     * instance
     */
    public static HologramManager getInstance() {
        return instance;
    }

    /**
     * Create a new hologram manager instance
     *
     * @return the new instance
     */
    private static HologramManager newInstance() {
        return new HologramManager() {
            @Override
            public Hologram createHologram(final String name, final Location location) {
                return null;
            }
        };
    }
}
