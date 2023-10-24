package es.karmadev.api.spigot.inventory.helper.infinity.func;

import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.inventory.helper.func.Action;
import es.karmadev.api.spigot.inventory.helper.infinity.InventoryBook;
import es.karmadev.api.spigot.inventory.helper.infinity.InventoryPage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface BookAction extends Action<InventoryBook> {

    /**
     * Run a custom action
     *
     * @param action the action to perform
     * @return the action
     */
    static BookAction run(final Runnable action) {
        return (book, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), action, 10);
    }

    /**
     * Replace the item
     *
     * @param new_item the new item
     * @return the action
     */
    static BookAction replaceItem(final ItemStack new_item) {
        return (book, e, player) -> {
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
    static BookAction allow() {
        return (book, e, player) -> {
            if (e instanceof Cancellable) {
                ((Cancellable) e).setCancelled(false);
            }
        };
    }

    /**
     * Close the inventory
     *
     * @return the action
     */
    static BookAction close() {
        return close(10);
    }

    /**
     * Change the page
     *
     * @param page the new page
     * @return the action
     */
    static BookAction changePage(final int page) {
        return changePage(page, 10);
    }

    /**
     * Go to the next page
     *
     * @return the action
     */
    static BookAction nextPage() {
        return nextPage(10);
    }

    /**
     * Go to the previous page
     *
     * @return the action
     */
    static BookAction previousPage() {
        return previousPage(10);
    }

    /**
     * Run a custom action
     *
     * @param action the action to perform
     * @param delay the ticks to wait before executing this action
     * @return the action
     */
    static BookAction run(final Runnable action, final long delay) {
        return (book, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), action, delay);
    }

    /**
     * Close the inventory
     *
     * @param delay the ticks to wait before executing this action
     * @return the action
     */
    static BookAction close(final long delay) {
        return (book, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), () -> {
            book.allowClose(player);
            player.closeInventory();
            book.denyClose(player);
        }, delay);
    }

    /**
     * Change the page
     *
     * @param page the new page
     * @param delay the ticks to wait before executing this action
     * @return the action
     */
    static BookAction changePage(final int page, final long delay) {
        return (book, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), () -> {
            if (book.getPageIndex(player) != page) {
                InventoryPage new_page = book.getPage(page);
                if (new_page != null) {
                    try {
                        book.allowClose(player);
                        book.open(player, page);
                        book.denyClose(player);
                    } catch (Throwable ignored) {}
                }
            }
        }, delay);
    }

    /**
     * Go to the next page
     *
     * @param delay the ticks to wait before executing this action
     * @return the action
     */
    static BookAction nextPage(long delay) {
        return (book, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), () -> {
            int next = book.getNextPageIndex(player);
            if (next != -1) {
                try {
                    book.allowClose(player);
                    book.open(player, next);
                    book.denyClose(player);
                } catch (Throwable ignored) {}
            }
        }, delay);
    }

    /**
     * Go to the previous page
     *
     * @param delay the ticks to wait before executing this action
     * @return the action
     */
    static BookAction previousPage(long delay) {
        return (book, e, player) -> Bukkit.getScheduler().runTaskLater(KarmaPlugin.getInstance(), () -> {
            int previous = book.getPreviousPageIndex(player);
            if (previous != -1) {
                try {
                    book.allowClose(player);
                    book.open(player, previous);
                    book.denyClose(player);
                } catch (Throwable ignored) {}
            }
        }, delay);
    }

    /**
     * Run an action on the click event
     *
     * @param click the click consumer
     * @return the action
     */
    static BookAction handle(final BiConsumer<Integer, InventoryView> click) {
        return handle(click, () -> true);
    }

    /**
     * Run an action on the click event
     *
     * @param click the click consumer
     * @param cancel the cancel function
     * @return the action
     */
    static BookAction handle(final BiConsumer<Integer, InventoryView> click, Supplier<Boolean> cancel) {
        return (book, e, player) -> {
            if (e instanceof InventoryClickEvent) {
                InventoryClickEvent ce = (InventoryClickEvent) e;
                int slot = ce.getSlot();
                InventoryView view = ce.getView();

                click.accept(ce.getSlot(), view);
                ce.setCancelled(cancel.get());
            }
        };
    }
}
