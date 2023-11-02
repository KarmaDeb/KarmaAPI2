package es.karmadev.api.spigot.reflection.title;

import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.server.SpigotServer;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Title reflection utility
 * class
 */
@SuppressWarnings("unused")
public class TitleReflection {

    private final static Class<?> chatBaseComponent = SpigotServer.netMinecraftServer("IChatBaseComponent").orElse(null);
    private final static Class<?> chatMessage = SpigotServer.netMinecraftServer("ChatMessage").orElse(null);
    private final static Class<?> packetTitle = SpigotServer.netMinecraftServer("PacketPlayOutTitle").orElse(null);
    private final static Class<?> enumTitleAction = SpigotServer.netMinecraftServer("PacketPlayOutTitle$EnumTitleAction").orElse(
            SpigotServer.netMinecraftServer("EnumTitleAction").orElse(null)
    );

    @Getter
    private static boolean supported = true;
    private final static Map<UUID, int[]> times = new ConcurrentHashMap<>();

    /**
     * Set the client title times
     *
     * @param plugin the plugin instance doing the change
     * @param player the player to set times for
     * @param fadeIn the time of fade in
     * @param show   the time of showing in
     * @param fadeOut the time to hide in
     */
    public static void setTimes(final KarmaPlugin plugin, final Player player, int fadeIn, int show, int fadeOut) {
        int[] times = TitleReflection.times.computeIfAbsent(player.getUniqueId(), (t) -> new int[]{10, 70, 20});
        if (fadeIn < 0) {
            fadeIn = times[0];
        }
        if (show < 0) {
            show = times[1];
        }
        if (fadeOut < 0) {
            fadeOut = times[2];
        }

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
                titlePacket = packetTitleConstructor.newInstance(fadeIn, show, fadeOut);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | IllegalArgumentException ignored) {}

            if (titlePacket == null) {
                plugin.logger().send(LogLevel.WARNING, "Failed to update title times, unable to create packet instance");
                return;
            }

            SpigotServer.sendPacket(player, titlePacket);
        }

        times[0] = fadeIn;
        times[1] = show;
        times[2] = fadeOut;
        TitleReflection.times.put(player.getUniqueId(), times);
    }

    /**
     * Send a title to a player
     *
     * @param plugin the plugin sending the title
     * @param player the player to send the title to
     * @param title  the title
     * @param fadeIn the time to show in
     * @param show   the time to show in screen
     * @param fadeOut the time to hide in
     */
    public static void sendTitle(final KarmaPlugin plugin, final Player player, final String title, int fadeIn, int show, int fadeOut) {
        int[] times = TitleReflection.times.computeIfAbsent(player.getUniqueId(), (t) -> new int[]{10, 70, 20});
        if (fadeIn < 0) {
            fadeIn = times[0];
        }
        if (show < 0) {
            show = times[1];
        }
        if (fadeOut < 0) {
            fadeOut = times[2];
        }

        if (SpigotServer.isBetween(SpigotServer.v1_8_X, SpigotServer.v1_10_2)) {
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
                titlePacket = packetTitleConstructor.newInstance(TITLE, chatTitle, fadeIn, show, fadeOut);
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
            player.sendTitle(title, null, fadeIn, show, fadeOut);
        }
    }

    /**
     * Send a title to a player
     *
     * @param plugin the plugin sending the title
     * @param player the player to send the title to
     * @param title  the title
     */
    public static void sendTitle(final KarmaPlugin plugin, final Player player, final String title) {
        sendTitle(plugin, player, title, -1, -1, -1);
    }

    /**
     * Send a subtitle to a player
     *
     * @param plugin the plugin sending the title
     * @param player the player to send the title to
     * @param subtitle the subtitle
     * @param fadeOut the time to show in
     * @param show the time to show in screen
     * @param fadeIn the time to hide in
     */
    public static void sendSubtitle(final KarmaPlugin plugin, final Player player, final String subtitle, int fadeIn, int show, int fadeOut) {
        int[] times = TitleReflection.times.computeIfAbsent(player.getUniqueId(), (t) -> new int[]{10, 70, 20});
        if (fadeIn < 0) {
            fadeIn = times[0];
        }
        if (show < 0) {
            show = times[1];
        }
        if (fadeOut < 0) {
            fadeOut = times[2];
        }

        if (SpigotServer.isBetween(SpigotServer.v1_8_X, SpigotServer.v1_10_2)) {
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
                subtitlePacket = packetTitleConstructor.newInstance(SUBTITLE, chatSubtitle, fadeIn, show, fadeOut);
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
            player.sendTitle(null, subtitle, fadeIn, show, fadeOut);
        }
    }

    /**
     * Send a subtitle to a player
     *
     * @param plugin the plugin sending the subtitle
     * @param player the player to send the subtitle to
     * @param subtitle  the subtitle
     */
    public static void sendSubtitle(final KarmaPlugin plugin, final Player player, final String subtitle) {
        sendSubtitle(plugin, player, subtitle, -1, -1, -1);
    }
}
