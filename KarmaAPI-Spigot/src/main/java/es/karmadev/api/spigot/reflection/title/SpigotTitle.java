package es.karmadev.api.spigot.reflection.title;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.KarmaPlugin;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.runner.signal.SignalHandler;
import es.karmadev.api.schedule.runner.signal.parameter.SignalParameter;
import es.karmadev.api.spigot.reflection.SpigotPacket;
import es.karmadev.api.spigot.server.SpigotServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spigot title utilities
 */
@SuppressWarnings("unused")
public class SpigotTitle implements SpigotPacket {

    private UUID target;
    private String title;
    private String subtitle;
    private int fadeIn, sendTick, fadeOut = 0;

    private final static Map<UUID, SpigotTitle> titles = new ConcurrentHashMap<>();

    private final SignalHandler handler = new SignalHandler() {
        @Override
        public void signal(final SignalParameter... parameters) {
            if (sendTick > 0 && target != null) {
                Player player = Bukkit.getServer().getPlayer(target);
                if (player != null && player.isOnline()) {
                    send(player, fadeIn / 20, sendTick / 20, fadeOut / 20);
                }
            }
        }
    };

    /**
     * Initialize the title
     *
     * @param title the title
     * @param subtitle the subtitle
     */
    public SpigotTitle(final String title, final String subtitle) {
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
    }

    /**
     * Update the title
     *
     * @param title the new title
     */
    public void updateTitle(final String title) {
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        handler.signal();
    }

    /**
     * Update the subtitle
     *
     * @param subtitle the new subtitle
     */
    public void updateSubtitle(final String subtitle) {
        this.subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
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
        send(player, 2, 5, 2);
    }

    /**
     * Send the packet to a player
     *
     * @param player the player to send
     *               the packet to
     * @param fadeIn the time to animate title
     *               show
     * @param showIn the time to display title
     *               in screen
     * @param fadeOut the time to animate title
     *                hide
     */
    public void send(final Player player, final int fadeIn, final int showIn, final int fadeOut) {
        KarmaPlugin plugin = (KarmaPlugin) KarmaKore.INSTANCE();
        if (plugin == null || player == null || !player.isOnline()) return;

        SpigotTitle instance = titles.computeIfAbsent(player.getUniqueId(), (title) -> this);
        //We want a single instance per player
        if (!ObjectUtils.equalsIgnoreCase(instance, this)) {
            instance.title = this.title; //Update title without emitting signal
            instance.subtitle = this.subtitle; //Update subtitle without emitting signal
            instance.send(player, fadeIn, showIn, fadeOut);
            return;
        }

        if (SpigotServer.isBetween(SpigotServer.v1_8_X, SpigotServer.v1_10_2)) {
            //if (SpigotServer.isUnder(SpigotServer.v1_8_X)) return; //Minimum version

            SpigotServer.netMinecraftServer("IChatBaseComponent").ifPresent((IChatBaseComponent) -> {
                Class<?>[] componentSubClasses = IChatBaseComponent.getDeclaredClasses();
                if (componentSubClasses.length == 0) return;
                Class<?> componentClass = IChatBaseComponent.getDeclaredClasses()[0];
                if (componentClass == null) return;

                Method tmpA = null;
                try {
                    tmpA = componentClass.getDeclaredMethod("a", String.class);
                } catch (NoSuchMethodException ignored) {}

                if (tmpA == null) return;
                Method a = tmpA;

                a.setAccessible(true);

                Object tmpChatTitle = null;
                try {
                    tmpChatTitle = a.invoke(null, "{\"text\": \"" + title + "\"}");
                } catch (IllegalAccessException | InvocationTargetException ignored) {}

                if (tmpChatTitle == null) return;

                Object chatTitle = tmpChatTitle;
                SpigotServer.netMinecraftServer("PacketPlayOutTitle").ifPresent((PacketPlayOutTitle) -> {
                    Class<?>[] titleSubClasses = IChatBaseComponent.getDeclaredClasses();
                    if (titleSubClasses.length == 0) return;

                    Class<?> titleSubclass = titleSubClasses[0];
                    Constructor<?> titleConstructor = null;
                    try {
                        titleConstructor = PacketPlayOutTitle.getConstructor(
                                titleSubclass,
                                IChatBaseComponent,
                                int.class,
                                int.class,
                                int.class);
                    } catch (NoSuchMethodException ignored) {}

                    if (titleConstructor == null) return;
                    Field TITLE = null;
                    Field SUBTITLE = null;
                    try {
                        TITLE = titleSubclass.getField("TITLE");
                        SUBTITLE = titleSubclass.getField("SUBTITLE");
                    } catch (NoSuchFieldException ignored) {}
                    if (TITLE == null || SUBTITLE == null) return;
                    TITLE.setAccessible(true);
                    SUBTITLE.setAccessible(true);

                    Object titleObject = null;
                    Object subtitleObject = null;
                    try {
                        titleObject = TITLE.get(null);
                        subtitleObject = SUBTITLE.get(null);
                    } catch (IllegalAccessException ignored) {}

                    Object titlePacket = null;
                    try {
                        titlePacket = titleConstructor.newInstance(titleObject, chatTitle, 20 * fadeIn, 20 * showIn, 20 * fadeOut);
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException ignored) {}
                    if (titlePacket == null) return;

                    Object tmpChatSubtitle = null;
                    try {
                        tmpChatSubtitle = a.invoke(null, "{\"text\": \"" + title + "\"}");
                    } catch (IllegalAccessException | InvocationTargetException ignored) {}
                    if (tmpChatSubtitle == null) return;

                    Object chatSubtitle = tmpChatSubtitle;
                    Object subtitlePacket = null;
                    try {
                        subtitlePacket = titleConstructor.newInstance(subtitleObject, chatSubtitle, 20 * fadeIn, 20 * showIn, 20 * fadeOut);
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException ignored) {}
                    if (subtitlePacket == null) return;

                    SpigotServer.sendPacket(player, titlePacket);
                    SpigotServer.sendPacket(player, subtitlePacket);
                });
            });
        } else {
            if (SpigotServer.isUnder(SpigotServer.v1_8_X)) return;

            player.sendTitle(title, subtitle, 20 * fadeIn, 20 * showIn, 20 * fadeOut);
        }

        this.target = player.getUniqueId();
        this.fadeIn = fadeIn * 20;
        this.sendTick = showIn * 20;
        this.fadeOut = fadeOut * 20;

        AtomicReference<BukkitTask> task = new AtomicReference<>();
        AtomicInteger lastTick = new AtomicInteger(sendTick);

        task.set(plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            //if the last tick doesn't match the current tick, it means that we've sent the title again
            if (sendTick == 0 || lastTick.get() != sendTick) {
                Bukkit.getServer().getScheduler().cancelTask(task.get().getTaskId());
                return;
            }

            if (sendTick > 0) sendTick--;
            lastTick.set(sendTick);
        }, 0, 1) /*Run task each tick*/);

    }

    /**
     * Get the title fade in ticks
     *
     * @return the title fade in ticks
     */
    public int getFadeIn() {
        return fadeIn;
    }

    /**
     * Get the title current tick
     *
     * @return the title tick
     */
    public int getSendTick() {
        return sendTick;
    }

    /**
     * Get the title fade out ticks
     *
     * @return the title fade out ticks
     */
    public int getFadeOut() {
        return fadeOut;
    }

    /**
     * Get the title target
     *
     * @return the title target
     */
    public Player getTarget() {
        return Bukkit.getServer().getPlayer(target);
    }

    /**
     * Cancel the title
     */
    public void cancel() {
        if (sendTick > 0 && target != null) {
            this.title = "";
            this.subtitle = "";
            Player player = Bukkit.getServer().getPlayer(target);
            if (player != null && player.isOnline()) {
                send(player, 0, 0, 0);
            }
        }
    }
}
