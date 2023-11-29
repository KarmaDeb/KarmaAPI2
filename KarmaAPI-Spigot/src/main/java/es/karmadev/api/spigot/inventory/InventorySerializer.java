package es.karmadev.api.spigot.inventory;

import com.google.gson.*;
import es.karmadev.api.file.util.PathUtilities;
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
            Gson gson = new GsonBuilder().create();
            JsonElement element = gson.fromJson(PathUtilities.read(data), JsonElement.class);

            if (element != null && element.isJsonObject()) return false; //Already exists
        }

        JsonObject object = new JsonObject();
        object.addProperty("title", title.replaceAll("§", "&"));
        object.addProperty("size", inventory.getSize());

        JsonArray slots = new JsonArray();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().equals(Material.AIR)) {
                Map<String, Object> itemData = item.serialize();

                JsonObject slotObject = new JsonObject();
                slotObject.addProperty("slot", slot);
                slotObject.addProperty("data", StringUtils.serializeUnsafe(itemData));

                slots.add(slotObject);
            }
        }

        object.add("slots", slots);

        if (!PathUtilities.createPath(data)) return false;

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
        String raw = gson.toJson(object);

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

        Gson gson = new GsonBuilder().create();
        JsonElement element = gson.fromJson(PathUtilities.read(data), JsonElement.class);
        if (element == null || !element.isJsonObject()) return null;

        JsonObject object = element.getAsJsonObject();
        if (!object.has("title") || !object.get("title").isJsonPrimitive()) return null;

        JsonPrimitive titleData = object.getAsJsonPrimitive("title");
        if (!titleData.isString()) return null;

        String title = titleData.getAsString();

        if (!object.has("size") || !object.get("size").isJsonPrimitive()) return null;

        JsonPrimitive sizeData = object.getAsJsonPrimitive("size");
        if (!sizeData.isNumber()) return null;

        int size = sizeData.getAsInt();

        Inventory inventory = Bukkit.createInventory(null, size, Colorize.colorize(title));

        if (!object.has("slots") || !object.get("slots").isJsonArray()) return null;
        JsonArray slotsData = object.getAsJsonArray("slots");


        for (JsonElement slotData : slotsData) {
            if (!slotData.isJsonObject()) continue;

            JsonObject slotObject = slotData.getAsJsonObject();
            if (!slotObject.has("slot") || !slotObject.get("slot").isJsonPrimitive()) continue;

            JsonPrimitive slotPrimitive = slotObject.get("slot").getAsJsonPrimitive();
            if (!slotPrimitive.isNumber()) continue;

            int slotIndex = slotPrimitive.getAsInt();

            if (!slotObject.has("data") || !slotObject.get("data").isJsonPrimitive()) continue;

            JsonPrimitive itemData = slotObject.get("data").getAsJsonPrimitive();
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
