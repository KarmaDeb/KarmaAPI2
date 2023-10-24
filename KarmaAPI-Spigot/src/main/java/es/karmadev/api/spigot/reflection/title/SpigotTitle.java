package es.karmadev.api.spigot.reflection.title;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.minecraft.color.ColorComponent;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.runner.signal.SignalHandler;
import es.karmadev.api.schedule.runner.signal.parameter.SignalParameter;
import es.karmadev.api.spigot.reflection.SpigotPacket;
import es.karmadev.api.minecraft.text.Component;
import es.karmadev.api.minecraft.text.component.AnimationComponent;
import es.karmadev.api.minecraft.text.component.TextComponent;
import es.karmadev.api.spigot.server.SpigotServer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spigot title utilities
 */
@SuppressWarnings("unused")
public class SpigotTitle implements SpigotPacket {

    private UUID target;
    @Nullable
    private TextComponent title;
    @Nullable
    private TextComponent subtitle;
    private BukkitRunnable animationSender;
    private BukkitRunnable subAnimationSender;
    private int animationSenderId;
    private int subAnimationSenderId;

    @Getter
    private int fadeIn = 0, sendTick = 0, fadeOut = 0;

    @Getter
    private boolean running;

    private final static Map<UUID, SpigotTitle> titles = new ConcurrentHashMap<>();
    private static boolean supported = true;

    private String currentTitle = "";
    private String currentSubtitle = "";

    private final static Class<?> chatBaseComponent = SpigotServer.netMinecraftServer("IChatBaseComponent").orElse(null);
    private final static Class<?> chatMessage = SpigotServer.netMinecraftServer("ChatMessage").orElse(null);
    private final static Class<?> packetTitle = SpigotServer.netMinecraftServer("PacketPlayOutTitle").orElse(null);
    private final static Class<?> enumTitleAction = SpigotServer.netMinecraftServer("PacketPlayOutTitle$EnumTitleAction").orElse(
            SpigotServer.netMinecraftServer("EnumTitleAction").orElse(null)
    );

    private final SignalHandler handler = new SignalHandler() {
        @Override
        public void signal(final SignalParameter... parameters) {
            if (sendTick > 0 && target != null) {
                Player player = Bukkit.getServer().getPlayer(target);
                if (player != null && player.isOnline()) {
                    SpigotTitle titleInstance = titles.get(player.getUniqueId());
                    if (titleInstance != null) {
                        TextComponent oldTitle = titleInstance.title;
                        TextComponent oldSubtitle = titleInstance.subtitle;

                        titleInstance.title = title;
                        titleInstance.subtitle = subtitle;
                    }

                    //send(player, fadeIn, sendTick, fadeOut);
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
    @Deprecated
    public SpigotTitle(final @Nullable String title, final @Nullable String subtitle) {
        if (title != null)
            this.title = Component.simple().text(ColorComponent.parse(title)).build();
        if (subtitle != null)
            this.subtitle = Component.simple().text(ColorComponent.parse(subtitle)).build();
    }

    /**
     * Initialize the title
     *
     * @param title the title
     * @param subtitle the subtitle
     */
    public SpigotTitle(final @Nullable TextComponent title, final @Nullable TextComponent subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    /**
     * Update the title
     *
     * @param title the new title
     */
    public void updateTitle(final String title) {
        this.title = Component.simple().text(ChatColor.translateAlternateColorCodes('&', title)).build();
        handler.signal();
    }

    /**
     * Update the subtitle
     *
     * @param subtitle the new subtitle
     */
    @Deprecated
    public void updateSubtitle(final String subtitle) {
        this.subtitle = Component.simple().text(ChatColor.translateAlternateColorCodes('&', subtitle)).build();
        handler.signal();
    }

    /**
     * Update the title
     *
     * @param title the new title
     */
    public void updateTitle(final TextComponent title) {
        this.title = title;
        handler.signal();
    }

    /**
     * Update the subtitle
     *
     * @param subtitle the new subtitle
     */
    public void updateSubtitle(final TextComponent subtitle) {
        this.subtitle = subtitle;
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
        if (!supported) return;

        KarmaPlugin plugin = (KarmaPlugin) KarmaKore.INSTANCE();
        if (plugin == null || player == null || !player.isOnline()) return;

        SpigotTitle instance = titles.computeIfAbsent(player.getUniqueId(), (title) -> this);
        this.sendTick = Math.max(1, showIn);

        int oldFadeIn = instance.fadeIn;
        int oldFadeOut = instance.fadeOut;

        if (oldFadeIn != fadeIn || oldFadeOut != fadeOut) {
            updateTimes(plugin, player);
        }

        instance.fadeIn = fadeIn;
        instance.fadeOut = fadeOut;

        //We want a single instance per player
        if (!ObjectUtils.equalsIgnoreCase(instance, this)) {
            instance.title = this.title; //Update title without emitting signal
            instance.subtitle = this.subtitle; //Update subtitle without emitting signal
            if (!instance.running) {
                instance.send(player, instance.fadeIn, showIn, instance.fadeOut);
            }

            //instance.send(player, fadeIn, showIn, fadeOut);
            return;
        }/* else {
            if (instance.running) {
                running = true;
                return; //We just updated, no need to run again
            }
        }*/

        if (instance.animationSender != null) {
            Bukkit.getScheduler().cancelTask(instance.animationSender.getTaskId());
        }
        if (instance.subAnimationSender != null) {
            Bukkit.getScheduler().cancelTask(instance.subAnimationSender.getTaskId());
        }

        instance.running = true;
        if (title instanceof AnimationComponent || subtitle instanceof AnimationComponent) {
            long interval = (title instanceof AnimationComponent ?
                    ((AnimationComponent) title).getInterval() : ((AnimationComponent) subtitle).getInterval());
            long subInterval = -1;

            if (animationSender != null) {
                Bukkit.getScheduler().cancelTask(animationSenderId);
            }
            if (subAnimationSender != null) {
                Bukkit.getScheduler().cancelTask(subAnimationSenderId);
            }

            if (title instanceof AnimationComponent && subtitle instanceof AnimationComponent) {
                AnimationComponent title = (AnimationComponent) this.title;
                AnimationComponent subtitle = (AnimationComponent) this.subtitle;

                subInterval = subtitle.getInterval();
                animationSender = new BukkitRunnable() {
                    @Override
                    public void run() {
                        String raw = title.getRaw();
                        if (raw == null) {
                            if (title.isFinished()) {
                                if (subAnimationSender != null) {
                                    subAnimationSender.cancel();
                                }

                                instance.running = false;

                                cancel();
                                return;
                            }
                        }

                        currentTitle = raw;
                        emitTitle(plugin, player, raw, showIn);
                    }
                };
                subAnimationSender = new BukkitRunnable() {
                    @Override
                    public void run() {
                        String raw = subtitle.getRaw();
                        if (raw == null) {
                            if (subtitle.isFinished()) {
                                if (animationSender != null) {
                                    animationSender.cancel();
                                }

                                instance.running = false;

                                cancel();
                                return;
                            }
                        }

                        currentSubtitle = raw;
                        emitSubtitle(plugin, player, raw, showIn);
                    }
                };
            } else {
                animationSender = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (title instanceof AnimationComponent) {
                            AnimationComponent title = (AnimationComponent) SpigotTitle.this.title;
                            String raw = title.getRaw();
                            if (raw == null) {
                                if (title.isFinished()) {
                                    instance.running = false;

                                    cancel();
                                    return;
                                }
                            }

                            emitTitle(plugin, player, raw, showIn);
                            return;
                        }

                        if (subtitle != null) {
                            AnimationComponent subtitle = (AnimationComponent) SpigotTitle.this.subtitle;
                            String raw = subtitle.getRaw();
                            if (raw == null) {
                                if (subtitle.isFinished()) {
                                    instance.running = false;

                                    cancel();
                                    return;
                                }
                            }

                            emitSubtitle(plugin, player, raw, showIn);
                        }
                    }
                };
            }

            animationSenderId = animationSender.runTaskTimer(plugin, 0, interval).getTaskId();
            if (subAnimationSender != null) {
                subAnimationSenderId = subAnimationSender.runTaskTimer(plugin, 0, subInterval).getTaskId();
            }
        } else {
            AtomicInteger ticksRun = new AtomicInteger();
            animationSender = new BukkitRunnable() {
                @Override
                public void run() {
                    if (ticksRun.getAndIncrement() <= sendTick) {
                        if (title != null) {
                            emitTitle(plugin, player, title.getRaw(), 2);
                        }
                        if (subtitle != null) {
                            emitSubtitle(plugin, player, subtitle.getRaw(), 2);
                        }

                        return;
                    }

                    instance.running = false;
                    cancel();
                }
            };

            animationSenderId = animationSender.runTaskTimer(plugin, 0, 1).getTaskId();
        }
    }

    public boolean hasFinished() {
        return !running;
    }

    private void updateTimes(final KarmaPlugin plugin, final Player player) {
        if (SpigotServer.isBetween(SpigotServer.v1_8_X, SpigotServer.v1_10_2)) {
            //Class<?> packetTitle = SpigotServer.netMinecraftServer("PacketPlayOutTitle").orElse(null);
            if (packetTitle == null) {
                plugin.logger().send(LogLevel.WARNING, "Failed to update title times, unable to find title packet");
                return;
            }

            Constructor<?> packetTitleConstructor = null;
            try {
                packetTitleConstructor = packetTitle.getConstructor(int.class, int.class, int.class);
            } catch (NoSuchMethodException ignored) {}

            if (packetTitleConstructor == null) {
                plugin.logger().send(LogLevel.WARNING, "Failed to update title times, unable to get packet constructor");
                return;
            }

            Object titlePacket = null;
            try {
                titlePacket = packetTitleConstructor.newInstance(fadeIn, 1, fadeOut);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | IllegalArgumentException ignored) {}

            if (titlePacket == null) {
                plugin.logger().send(LogLevel.WARNING, "Failed to update title times, unable to create packet instance");
                return;
            }

            SpigotServer.sendPacket(player, titlePacket);
        }
    }

    private void emitTitle(final KarmaPlugin plugin, final Player player, final String title, final int showIn) {
        if (SpigotServer.isBetween(SpigotServer.v1_8_X, SpigotServer.v1_10_2)) {
            //if (SpigotServer.isUnder(SpigotServer.v1_8_X)) return; //Minimum version

            /*Class<?> chatBaseComponent = SpigotServer.netMinecraftServer("IChatBaseComponent").orElse(null);
            Class<?> chatMessage = SpigotServer.netMinecraftServer("ChatMessage").orElse(null);
            Class<?> packetTitle = SpigotServer.netMinecraftServer("PacketPlayOutTitle").orElse(null);
            Class<?> enumTitleAction = SpigotServer.netMinecraftServer("PacketPlayOutTitle$EnumTitleAction").orElse(
                    SpigotServer.netMinecraftServer("EnumTitleAction").orElse(null)
            );*/

            if (chatBaseComponent == null || chatMessage == null || packetTitle == null || enumTitleAction == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find needed classes for creating title/subtitle, disabling titles support");
                supported = false;
                return;
            }
            if (!enumTitleAction.isEnum()) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find EnumTitleAction for title/subtitle, disabling titles support");
                supported = false;
                return;
            }

            Enum<?> TITLE = null;

            for (Object object : enumTitleAction.getEnumConstants()) {
                if (object instanceof Enum) {
                    Enum<?> objectEnum = (Enum<?>) object;
                    if (objectEnum.name().equals("TITLE")) {
                        TITLE = objectEnum;
                    }
                }

                if (TITLE != null) {
                    break;
                }
            }

            if (TITLE == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't determine title packet types, disabling titles support");
                supported = false;
                return;
            }

            Constructor<?> chatMessageConstructor = null;
            Constructor<?> packetTitleConstructor = null;
            try {
                chatMessageConstructor = chatMessage.getConstructor(String.class, Object[].class);
                packetTitleConstructor = packetTitle.getConstructor(enumTitleAction, chatBaseComponent, int.class, int.class, int.class);
            } catch (NoSuchMethodException ignored) {}

            if (chatMessageConstructor == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find chat message constructor, disabling titles support");
                supported = false;
                return;
            }
            if (packetTitleConstructor == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find title packet constructor, disabling titles support");
                supported = false;
                return;
            }

            Object chatTitle = null;
            Object titlePacket = null;
            try {
                chatTitle = chatMessageConstructor.newInstance(title, new Object[0]);
                titlePacket = packetTitleConstructor.newInstance(TITLE, chatTitle, fadeIn, showIn, fadeOut);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | IllegalArgumentException ignored) {}

            if (chatTitle == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't create title, disabling titles support");
                supported = false;
                return;
            }

            if (titlePacket == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't create title packet, disabling titles support");
                supported = false;
                return;
            }

            SpigotServer.sendPacket(player, titlePacket);
        } else {
            if (SpigotServer.isUnder(SpigotServer.v1_8_X)) return;
            player.sendTitle(title, null, fadeIn, showIn, fadeOut);
        }

        this.target = player.getUniqueId();
    }

    private void emitSubtitle(final KarmaPlugin plugin, final Player player, final String subtitle, final int showIn) {
        if (SpigotServer.isBetween(SpigotServer.v1_8_X, SpigotServer.v1_10_2)) {
            //if (SpigotServer.isUnder(SpigotServer.v1_8_X)) return; //Minimum version

            /*Class<?> chatBaseComponent = SpigotServer.netMinecraftServer("IChatBaseComponent").orElse(null);
            Class<?> chatMessage = SpigotServer.netMinecraftServer("ChatMessage").orElse(null);
            Class<?> packetTitle = SpigotServer.netMinecraftServer("PacketPlayOutTitle").orElse(null);
            Class<?> enumTitleAction = SpigotServer.netMinecraftServer("PacketPlayOutTitle$EnumTitleAction").orElse(
                    SpigotServer.netMinecraftServer("EnumTitleAction").orElse(null)
            );*/

            if (chatBaseComponent == null || chatMessage == null || packetTitle == null || enumTitleAction == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find needed classes for creating title/subtitle, disabling titles support");
                supported = false;
                return;
            }
            if (!enumTitleAction.isEnum()) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find EnumTitleAction for title/subtitle, disabling titles support");
                supported = false;
                return;
            }

            Enum<?> SUBTITLE = null;

            for (Object object : enumTitleAction.getEnumConstants()) {
                if (object instanceof Enum) {
                    Enum<?> objectEnum = (Enum<?>) object;
                    if (objectEnum.name().equals("SUBTITLE")) {
                        SUBTITLE = objectEnum;
                    }
                }
            }

            if (SUBTITLE == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't determine subtitle packet types, disabling titles support");
                supported = false;
                return;
            }

            Constructor<?> chatMessageConstructor = null;
            Constructor<?> packetTitleConstructor = null;
            try {
                chatMessageConstructor = chatMessage.getConstructor(String.class, Object[].class);
                packetTitleConstructor = packetTitle.getConstructor(enumTitleAction, chatBaseComponent, int.class, int.class, int.class);
            } catch (NoSuchMethodException ignored) {}

            if (chatMessageConstructor == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find chat message constructor, disabling titles support");
                supported = false;
                return;
            }
            if (packetTitleConstructor == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't find title packet constructor, disabling titles support");
                supported = false;
                return;
            }

            Object chatSubtitle = null;
            Object subtitlePacket = null;
            try {
                chatSubtitle = chatMessageConstructor.newInstance(subtitle, new Object[0]);
                subtitlePacket = packetTitleConstructor.newInstance(SUBTITLE, chatSubtitle, fadeIn, showIn, fadeOut);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | IllegalArgumentException ignored) {}

            if (chatSubtitle == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't create subtitle, disabling titles support");
                supported = false;
                return;
            }

            if (subtitlePacket == null) {
                plugin.logger().send(LogLevel.WARNING, "Couldn't create subtitle packet, disabling titles support");
                supported = false;
                return;
            }

            SpigotServer.sendPacket(player, subtitlePacket);
        } else {
            if (SpigotServer.isUnder(SpigotServer.v1_8_X)) return;
            player.sendTitle(null, subtitle, fadeIn, showIn, fadeOut);
        }

        this.target = player.getUniqueId();
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
            sendTick = 0;

            title = Component.simple().text("").build();
            subtitle = Component.simple().text("").build();
            Player player = Bukkit.getServer().getPlayer(target);
            if (player != null && player.isOnline()) {
                send(player, 0, 0, 0);
            }
        }
    }
}
