package es.karmadev.api.spigot.inventory.helper.option.func;

import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.inventory.helper.func.Action;
import es.karmadev.api.spigot.inventory.helper.option.OptionsInventory;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface OptionAction<T> extends Action<OptionsInventory<T>> {

    /**
     * Run a custom action
     *
     * @param action the action to perform
     * @return the action
     */
    static OptionAction<?> run(final Runnable action) {
        return (inventory, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), action, 10);
    }

    /**
     * Replace the item
     *
     * @param new_item the new item
     * @return the action
     */
    static OptionAction<?> replaceItem(final ItemStack new_item) {
        return (inventory, e, player) -> {
            if (e instanceof InventoryClickEvent) {
                InventoryClickEvent interact = (InventoryClickEvent) e;

                int slot = interact.getSlot();
                interact.getInventory().setItem(slot, new_item);
            }
            if (e instanceof InventoryDragEvent) {
                InventoryDragEvent drag = (InventoryDragEvent) e;
                drag.setCursor(new_item);
            }
            if (e instanceof InventoryMoveItemEvent) {
                InventoryMoveItemEvent move = (InventoryMoveItemEvent) e;
                move.setItem(new_item);
            }
        };
    }

    /**
     * Allow event interaction, please note this will allow player
     * to take items
     *
     * @return the action
     */
    static OptionAction<?> allow() {
        return (inventory, e, player) -> {
            if (e instanceof Cancellable) {
                ((Cancellable) e).setCancelled(false);
            }
        };
    }

    /**
     * Run a custom action
     *
     * @param action the action to perform
     * @param delay the ticks to wait before executing this action
     * @return the action
     */
    static OptionAction<?> run(final Runnable action, final long delay) {
        return (inventory, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), action, delay);
    }

    /**
     * Run an action on the click event
     *
     * @param click the click consumer
     * @return the action
     */
    static OptionAction<?> handle(final BiConsumer<Integer, InventoryView> click) {
        return handle(click, () -> true);
    }

    /**
     * Run an action on the click event
     *
     * @param click the click consumer
     * @param cancel the cancel function
     * @return the action
     */
    static OptionAction<?> handle(final BiConsumer<Integer, InventoryView> click, Supplier<Boolean> cancel) {
        return (inventory, e, player) -> {
            if (e instanceof InventoryClickEvent) {
                InventoryClickEvent ce = (InventoryClickEvent) e;
                InventoryView view = ce.getView();

                click.accept(ce.getSlot(), view);
                ce.setCancelled(cancel.get());
            }
        };
    }
}
