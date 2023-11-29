package es.karmadev.api.spigot.reflection.packet;

import es.karmadev.api.minecraft.text.component.Component;
import es.karmadev.api.minecraft.text.component.ComponentSequence;
import es.karmadev.api.minecraft.text.component.message.AnimatedComponent;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.reflection.SpigotPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Animated message packet
 */
class AnimatedMessagePacket implements SpigotPacket {

    private final static KarmaPlugin plugin = KarmaPlugin.getInstance();
    private final ComponentSequence sequence;
    private int repeat = 0;

    public AnimatedMessagePacket(final ComponentSequence sequence) {
        this.sequence = sequence;
    }

    /**
     * Send the packet to a player
     *
     * @param player the player to send
     *               the packet to
     */
    @Override
    public void send(final Player player) {
        Consumer<Void> onTaskEnd = new Consumer<Void>() {
            @Override
            public void accept(Void unused) {
                if (sequence.isFinished() || Bukkit.getPlayer(player.getUniqueId()) == null) return; //We "cancel"

                Component next = sequence.next();
                long wait = sequence.getInterval();

                if (next instanceof ComponentSequence) {
                    ComponentSequence subSequence = (ComponentSequence) next;
                    wait = Math.max(subSequence.getInterval(), (subSequence.getInterval() * subSequence.getRepeats()) * subSequence.getExtra().size());
                }

                MessagePacket packet = new MessagePacket(next);
                packet.send(player);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    accept(null);
                }, wait);
            }
        };

        onTaskEnd.accept(null);
    }
}
