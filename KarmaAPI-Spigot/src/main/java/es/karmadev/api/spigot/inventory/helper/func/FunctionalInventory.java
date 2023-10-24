package es.karmadev.api.spigot.inventory.helper.func;

import es.karmadev.api.spigot.inventory.helper.infinity.InventoryPage;

public interface FunctionalInventory {

    /**
     * On close actions
     *
     * @param action the actions to perform
     * @return this instance
     */
    InventoryPage onClose(final Action... action);

    /**
     * On open actions
     *
     * @param actions the actions to perform
     * @return this instance
     */
    InventoryPage onOpen(final Action... actions);
}
