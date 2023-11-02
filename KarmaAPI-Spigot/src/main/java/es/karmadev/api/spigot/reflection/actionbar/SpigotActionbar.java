package es.karmadev.api.spigot.reflection.actionbar;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.runner.signal.SignalHandler;
import es.karmadev.api.schedule.runner.signal.parameter.SignalParameter;
import es.karmadev.api.spigot.reflection.SpigotPacket;
import es.karmadev.api.spigot.server.SpigotServer;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spigot actionbar
 */
public class SpigotActionbar implements SpigotPacket {

    private UUID target;
    @Getter
    private String message;
    @Getter
    private long endTick = 0;
    private final AtomicReference<BukkitTask> persistentTask = new AtomicReference<>();
    private final static Map<UUID, SpigotActionbar> actionbars = new ConcurrentHashMap<>();

    private final SignalHandler handler = new SignalHandler() {
        @Override
        public void signal(final SignalParameter... parameters) {
            if ((endTick == -1 || endTick > 0 || endTick < SpigotServer.getCurrentTick()) && target != null) {
                Player player = Bukkit.getServer().getPlayer(target);
                if (player != null && player.isOnline()) {
                    long preEndTick = endTick;
                    send(player);
                    endTick = preEndTick; //Keep end tick the same
                }
            }
        }
    };

    /**
     * Initialize the actionbar
     *
     * @param message the actionbar message
     */
    public SpigotActionbar(final String message) {
        this.message = ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Set the actionbar message
     *
     * @param message the new message
     */
    public void setMessage(final String message) {
        this.message = ChatColor.translateAlternateColorCodes('&', message);
        handler.signal();
    }

    /**
     * Send the packet to a player
     *
     * @param player the player to send
     *               the packet to
     */
    @Override
    public void send(final Player player) {
        KarmaPlugin plugin = (KarmaPlugin) KarmaKore.INSTANCE();
        if (plugin == null || player == null || !player.isOnline()) return;

        SpigotActionbar instance = actionbars.computeIfAbsent(target, (bar) -> this);
        if (!ObjectUtils.equalsIgnoreCase(instance, this)) {
            instance.message = this.message;
            instance.send(player);
            return;
        }

        if (persistentTask.get() != null) {
            BukkitTask taskInstance = persistentTask.get();
            Bukkit.getServer().getScheduler().cancelTask(taskInstance.getTaskId());
            persistentTask.set(null);
        }

        if (SpigotServer.isBetween(SpigotServer.v1_8_X, SpigotServer.v1_10_X)) {
            SpigotServer.netMinecraftServer("PacketPlayOutChat").ifPresent((PacketPlayOutChat) -> SpigotServer
                    .netMinecraftServer("IChatBaseComponent").ifPresent((IChatBaseComponent) -> {
                        Constructor<?> chatPacket = null;
                        try {
                            chatPacket = PacketPlayOutChat.getConstructor(IChatBaseComponent, byte.class);
                        } catch (NoSuchMethodException ignored) {}
                        if (chatPacket == null) return;

                        chatPacket.setAccessible(true);
                        Class<?>[] componentClasses = IChatBaseComponent.getDeclaredClasses();
                        if (componentClasses.length == 0) return;

                        Class<?> baseComponentSub = componentClasses[0];
                        if (baseComponentSub == null) return;

                        Method a = null;
                        try {
                            a = baseComponentSub.getMethod("a", String.class);
                        } catch (NoSuchMethodException ignored) {}
                        if (a == null) return;

                        a.setAccessible(true);
                        Object chatComponent = null;

                        try {
                            chatComponent = a.invoke(null, "{\"text\":\"" + message + "\"}");
                        } catch (IllegalAccessException | InvocationTargetException ignored) {}
                        if (chatComponent == null) return;

                        Object packet = null;
                        try {
                            packet = chatPacket.newInstance(chatComponent, (byte) 2);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
                        if (packet == null) return;

                        SpigotServer.sendPacket(player, packet);
                    }));
        } else {
            if (SpigotServer.isUnder(SpigotServer.v1_8_X)) return;

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(message));
        }

        target = player.getUniqueId();
        endTick = SpigotServer.getFutureTick(2, TimeUnit.SECONDS); //When the actionbar should stop
    }

    /**
     * Send the hologram persistently
     *
     * @param player the player to show the actionbar to
     */
    public void sendPersistent(final Player player) {
        send(player);
        endTick = -1;

        KarmaPlugin plugin = (KarmaPlugin) KarmaKore.INSTANCE();
        if (plugin != null) {
            BukkitTask tsk = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (endTick > -1) {
                    int id = persistentTask.get().getTaskId();
                    Bukkit.getServer().getScheduler().cancelTask(id);
                } else {
                    send(player);
                    endTick = -1;
                }
            }, 0, 1);

            persistentTask.set(tsk);
        }
    }

    /**
     * Send the actionbar to the player
     *
     * @param player the player to send the actionbar to
     * @param ticks the ticks to show the actionbar
     */
    public void sendTimed(final Player player, final long ticks) {
        send(player);
        endTick = SpigotServer.getCurrentTick() + ticks;

        KarmaPlugin plugin = (KarmaPlugin) KarmaKore.INSTANCE();
        if (plugin != null) {
            BukkitTask tsk = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (endTick > SpigotServer.getCurrentTick()) {
                    int id = persistentTask.get().getTaskId();
                    Bukkit.getServer().getScheduler().cancelTask(id);
                } else {
                    long preEndTick = endTick;
                    send(player);
                    endTick = preEndTick;
                }
            }, 0, 1);

            persistentTask.set(tsk);
        }
    }

    /**
     * Get the player to send the packet
     * to
     *
     * @return the target player
     */
    public Player getTarget() {
        return Bukkit.getServer().getPlayer(target);
    }
}
