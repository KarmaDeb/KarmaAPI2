package es.karmadev.api.spigot.inventory.helper.option;

import es.karmadev.api.minecraft.color.ColorComponent;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.inventory.helper.func.ItemFunction;
import es.karmadev.api.spigot.inventory.helper.option.func.OptionItemFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents an inventory for a user
 * to make a choice
 */
public class OptionsInventory<T> implements InventoryHolder, Listener {

    protected final ConcurrentMap<Integer, T> choices = new ConcurrentHashMap<>();
    protected final Map<Integer, ItemFunction<OptionsInventory<T>>> functions = new ConcurrentHashMap<>();
    protected final Set<UUID> open = ConcurrentHashMap.newKeySet();
    protected final Inventory inventory;

    protected boolean canClose = false;

    public OptionsInventory(final String title, final int size) {
        inventory = Bukkit.createInventory(this, size, ColorComponent.parse(title));

        KarmaPlugin plugin = KarmaPlugin.getInstance();
        Bukkit.getPluginManager().registerEvent(InventoryClickEvent.class, this, EventPriority.HIGHEST, (listener, event) -> {
            assert event instanceof InventoryClickEvent;
            InventoryClickEvent click = (InventoryClickEvent) event;

            if (click.getClickedInventory() != null) {
                if (click.getClickedInventory().getHolder() == this) {
                    click.setCancelled(true);

                    ItemFunction<OptionsInventory<T>> function = functions.getOrDefault(click.getSlot(), null);
                    if (function != null) {
                        function.triggerClick(click);
                    }
                }
            }
        }, plugin, false);
        Bukkit.getPluginManager().registerEvent(InventoryMoveItemEvent.class, this, EventPriority.HIGHEST, (listener, event) -> {
            assert event instanceof InventoryMoveItemEvent;
            InventoryMoveItemEvent e = (InventoryMoveItemEvent) event;

            e.setCancelled(e.getDestination().getHolder() == this || e.getInitiator().getHolder() == this);
        }, plugin, true);
        Bukkit.getPluginManager().registerEvent(InventoryDragEvent.class, this, EventPriority.HIGHEST, (listener, event) -> {
            assert event instanceof InventoryDragEvent;
            InventoryDragEvent e = (InventoryDragEvent) event;

            e.setCancelled(e.getInventory().getHolder() == this);
        }, plugin, true);
        Bukkit.getPluginManager().registerEvent(InventoryCloseEvent.class, this, EventPriority.HIGHEST, (listener, event) -> {
            assert event instanceof InventoryCloseEvent;
            InventoryCloseEvent close = (InventoryCloseEvent) event;

            if (close.getInventory().getHolder() == this) {
                HumanEntity human = close.getPlayer();

                if (!canClose && open.contains(human.getUniqueId())) {
                    Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), () -> {
                        try {
                            human.openInventory(inventory);
                        } catch (Throwable ignored) {}
                    }, 10);
                }
            }
        }, plugin, false);
    }

    /**
     * Set if the inventory can be closed without
     * making a choice
     *
     * @param status the inventory close status
     * @return if the inventory can be closed
     */
    public OptionsInventory<T> setCanClose(final boolean status) {
        this.canClose = status;
        return this;
    }

    /**
     * Add a choice to the inventory
     *
     * @param element the choice
     * @param slot the choice slot
     * @param display the choice item display
     * @return the choice
     */
    public final ItemFunction<OptionsInventory<T>> addChoice(final T element, final int slot, final ItemStack display) {
        ItemFunction<OptionsInventory<T>> function = new OptionItemFunction<>(this);

        inventory.setItem(slot, display);
        choices.put(slot, element);
        functions.put(slot, function);

        return function;
    }

    /**
     * Remove a choice from the inventory
     *
     * @param slot the choice slot
     */
    public final void removeChoice(final int slot) {
        inventory.setItem(slot, null);
        choices.remove(slot);
        functions.remove(slot);
    }

    /**
     * Get the object's inventory.
     *
     * @return The inventory.
     */
    @NotNull
    @Override
    public final Inventory getInventory() {
        return inventory;
    }

    /**
     * Close the inventory for the entity
     *
     * @param entity the entity to close the inventory
     *               for
     */
    public void close(final HumanEntity entity) {
        if (open.contains(entity.getUniqueId())) {
            open.remove(entity.getUniqueId());
            entity.closeInventory();
        }
    }

    /**
     * Open the inventory to the entity
     *
     * @param entity the entity to open the
     *               inventory to
     */
    public void open(final HumanEntity entity) {
        if (!open.contains(entity.getUniqueId())) {
            open.add(entity.getUniqueId());
            entity.openInventory(inventory);
        }
    }
}
