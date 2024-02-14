package es.karmadev.api.spigot.inventory;

import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.minecraft.text.Colorize;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.strings.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * Inventory serializer
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE) @Getter
public class InventorySerializer {

    private final static KarmaPlugin plugin = KarmaPlugin.getInstance();

    private final UUID id;
    private final String title;
    private transient Inventory inventory;

    /**
     * Get the inventory unique ID
     *
     * @return the inventory unique ID
     */
    public UUID getUniqueId() {
        return id;
    }

    /**
     * Get if the data exists
     *
     * @return if the data exists
     */
    public boolean exists() {
        String simple = id.toString().replaceAll("-", "");
        Path data = plugin.workingDirectory().resolve(simple).resolve("data.json");

        return Files.exists(data);
    }

    /**
     * Saves the inventory data
     * @return if the data could be saved
     */
    public boolean save() {
        String simple = id.toString().replaceAll("-", "");
        Path data = plugin.workingDirectory().resolve(simple).resolve("data.json");

        if (Files.exists(data)) {
            JsonInstance element = JsonReader.read(PathUtilities.read(data));

            if (element.isObjectType()) return false; //Already exists
        }

        JsonObject object = JsonObject.newObject("", "");
        object.put("title", title.replaceAll("§", "&"));
        object.put("size", inventory.getSize());

        JsonArray slots = JsonArray.newArray("", "slots");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().equals(Material.AIR)) {
                Map<String, Object> itemData = item.serialize();

                JsonObject slotObject = JsonObject.newObject("", "");
                slotObject.put("slot", slot);
                slotObject.put("data", StringUtils.serializeUnsafe(itemData));

                slots.add(slotObject);
            }
        }

        object.put("slots", slots);

        if (!PathUtilities.createPath(data)) return false;

        String raw = object.toString();
        return PathUtilities.write(data, raw);
    }

    /**
     * Destroy the stored data if any
     */
    public boolean destroy() {
        String simple = id.toString().replaceAll("-", "");
        Path data = plugin.workingDirectory().resolve(simple).resolve("data.json");

        return PathUtilities.destroy(data);
    }

    /**
     * Create an inventory serializer for the
     * inventory
     *
     * @param inventory the inventory view
     * @return the inventory serializer
     */
    public static InventorySerializer forInventory(final InventoryView inventory) {
        return new InventorySerializer(UUID.randomUUID(), inventory.getTitle(), inventory.getTopInventory());
    }

    /**
     * Load an inventory
     *
     * @param inventoryId the inventory ID
     * @return the loaded inventory
     */
    @Nullable
    public static InventorySerializer load(final UUID inventoryId) {
        String simple = inventoryId.toString().replaceAll("-", "");

        Path data = plugin.workingDirectory().resolve(simple).resolve("data.json");
        if (!Files.exists(data)) return null;

        JsonInstance element = JsonReader.read(PathUtilities.read(data));
        if (!element.isObjectType()) return null;

        JsonObject object = element.asObject();
        if (!object.hasChild("title") || !object.getChild("title").isNativeType()) return null;

        JsonNative titleData = object.getChild("title").asNative();
        if (!titleData.isString()) return null;

        String title = titleData.getAsString();

        if (!object.hasChild("size") || !object.getChild("size").isNativeType()) return null;

        JsonNative sizeData = object.getChild("size").asNative();
        if (!sizeData.isNumber()) return null;

        int size = sizeData.asInteger();

        Inventory inventory = Bukkit.createInventory(null, size, Colorize.colorize(title));

        if (!object.hasChild("slots") || !object.getChild("slots").isArrayType()) return null;
        JsonArray slotsData = object.getChild("slots").asArray();


        for (JsonInstance slotData : slotsData) {
            if (!slotData.isObjectType()) continue;

            JsonObject slotObject = slotData.asObject();
            if (!slotObject.hasChild("slot") || !slotObject.getChild("slot").isNativeType()) continue;

            JsonNative slotPrimitive = slotObject.getChild("slot").asNative();
            if (!slotPrimitive.isNumber()) continue;

            int slotIndex = slotPrimitive.asInteger();

            if (!slotObject.hasChild("data") || !slotObject.getChild("data").isNativeType()) continue;

            JsonNative itemData = slotObject.getChild("data").asNative();
            if (!itemData.isString()) continue;

            Map<String, Object> itemMap = StringUtils.loadAndCast(itemData.getAsString());
            if (itemMap != null) {
                ItemStack stack = ItemStack.deserialize(itemMap);
                ItemStack existing = inventory.getItem(slotIndex);

                if (existing != null && !existing.getType().equals(Material.AIR)) {
                    if (existing.isSimilar(stack)) {
                        existing.setAmount(existing.getAmount() + 1);
                    }

                    inventory.setItem(slotIndex, existing);
                    continue;
                }

                inventory.setItem(slotIndex, stack);
            }
        }

        return new InventorySerializer(inventoryId, title, inventory);
    }
}
