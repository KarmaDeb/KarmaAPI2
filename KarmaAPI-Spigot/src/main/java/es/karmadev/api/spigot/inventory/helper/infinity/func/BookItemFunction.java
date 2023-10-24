package es.karmadev.api.spigot.inventory.helper.infinity.func;

import es.karmadev.api.spigot.inventory.helper.func.Action;
import es.karmadev.api.spigot.inventory.helper.func.ItemFunction;
import es.karmadev.api.spigot.inventory.helper.infinity.InventoryBook;
import es.karmadev.api.spigot.inventory.helper.infinity.InventoryPage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Represents an item book function
 */
public class BookItemFunction extends ItemFunction<InventoryBook> {

    /**
     * Initialize the item function
     *
     * @param hook the inventory hook
     */
    public BookItemFunction(final InventoryBook hook) {
        super(hook);
    }

    @Override
    public void triggerClick(final InventoryClickEvent click) {
        HumanEntity human = click.getWhoClicked();

        if (human instanceof Player) {
            Player player = (Player) human;

            Inventory inv = click.getClickedInventory();
            InventoryPage page = hook.getPage(player);
            if (page != null) {
                if (page.isInventory(inv)) {
                    click.setCancelled(true);

                    for (Action<InventoryBook> rs : click_actions) {
                        rs.accept(hook, click, player);
                    }
                }
            }
        }
    }
}
