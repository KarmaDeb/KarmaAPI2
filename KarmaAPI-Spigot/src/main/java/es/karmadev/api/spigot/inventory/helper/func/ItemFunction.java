package es.karmadev.api.spigot.inventory.helper.func;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ItemFunction<T> {

    protected final T hook;
    protected final Set<Action<T>> click_actions = new LinkedHashSet<>();

    /**
     * Initialize the item function
     *
     * @param hook the inventory hook
     */
    public ItemFunction(final T hook) {
        this.hook = hook;
    }

    /**
     * On item click action
     *
     * @param action the action to perform
     * @return this instance
     */
    public ItemFunction<T> onClick(final Action<T>... action) {
        click_actions.addAll(Arrays.asList(action));
        return this;
    }

    public abstract void triggerClick(final InventoryClickEvent click);
}
