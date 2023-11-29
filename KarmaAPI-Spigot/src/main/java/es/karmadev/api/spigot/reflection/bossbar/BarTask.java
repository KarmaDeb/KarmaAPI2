package es.karmadev.api.spigot.reflection.bossbar;

import lombok.Getter;
import lombok.Value;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Getter @Value(staticConstructor = "of")
class BarTask {

    SpigotBossBar bar;
    Consumer<Player> onTick;
}
