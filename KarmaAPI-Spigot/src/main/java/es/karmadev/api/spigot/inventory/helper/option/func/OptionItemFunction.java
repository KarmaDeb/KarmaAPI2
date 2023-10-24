package es.karmadev.api.spigot.inventory.helper.option.func;

import es.karmadev.api.spigot.inventory.helper.func.Action;
import es.karmadev.api.spigot.inventory.helper.func.ItemFunction;
import es.karmadev.api.spigot.inventory.helper.option.OptionsInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OptionItemFunction<T> extends ItemFunction<OptionsInventory<T>> {

    /**
     * Initialize the item function
     *
     * @param hook the inventory hook
     */
    public OptionItemFunction(final OptionsInventory<T> hook) {
        super(hook);
    }

    @Override
    public void triggerClick(final InventoryClickEvent click) {
        HumanEntity human = click.getWhoClicked();

        if (human instanceof Player) {
            Player player = (Player) human;

            click.setCancelled(true);
            for (Action<OptionsInventory<T>> rs : click_actions) {
                rs.accept(hook, click, player);
            }
        }
    }
}
