package es.karmadev.api.spigot.inventory.helper.func;

import es.karmadev.api.function.TriConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Action to perform
 */
@SuppressWarnings("unused")
public interface Action<T> extends TriConsumer<T, Event, Player> {

}
