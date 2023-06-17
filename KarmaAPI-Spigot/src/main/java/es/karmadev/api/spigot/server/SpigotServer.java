package es.karmadev.api.spigot.server;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.KarmaPlugin;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.version.Version;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Spigot server utilities
 */
@SuppressWarnings("unused")
public class SpigotServer {

    public final static Version v1_7_X = Version.of(1, 7, 0);
    public final static Version v1_7_2 = Version.of(1, 7, 2);
    public final static Version v1_7_4 = Version.of(1, 7, 4);
    public final static Version v1_7_5 = Version.of(1, 7, 5);
    public final static Version v1_7_6 = Version.of(1, 7, 6);
    public final static Version v1_7_7 = Version.of(1, 7, 7);
    public final static Version v1_7_8 = Version.of(1, 7, 8);
    public final static Version v1_7_9 = Version.of(1, 7, 9);
    public final static Version v1_7_10 = Version.of(1, 7, 10);
    public final static Version v1_8_X = Version.of(1, 8, 0);
    public final static Version v1_8_1 = Version.of(1, 8, 1);
    public final static Version v1_8_2 = Version.of(1, 8, 2);
    public final static Version v1_8_3 = Version.of(1, 8, 3);
    public final static Version v1_8_4 = Version.of(1, 8, 4);
    public final static Version v1_8_5 = Version.of(1, 8, 5);
    public final static Version v1_8_6 = Version.of(1, 8, 6);
    public final static Version v1_8_7 = Version.of(1, 8, 7);
    public final static Version v1_8_8 = Version.of(1, 8, 8);
    public final static Version v1_8_9 = Version.of(1, 8, 9);
    public final static Version v1_9_X = Version.of(1, 9, 0);
    public final static Version v1_9_1 = Version.of(1, 9, 1);
    public final static Version v1_9_2 = Version.of(1, 9, 2);
    public final static Version v1_9_3 = Version.of(1, 9, 3);
    public final static Version v1_9_4 = Version.of(1, 9, 4);
    public final static Version v1_10_X = Version.of(1, 10, 0);
    public final static Version v1_10_1 = Version.of(1, 10, 1);
    public final static Version v1_10_2 = Version.of(1, 10, 2);
    public final static Version v1_11_X = Version.of(1, 11, 0);
    public final static Version v1_11_1 = Version.of(1, 11, 1);
    public final static Version v1_11_2 = Version.of(1, 11, 2);
    public final static Version v1_12_X = Version.of(1, 12, 0);
    public final static Version v1_12_1 = Version.of(1, 12, 1);
    public final static Version v1_12_2 = Version.of(1, 12, 2);
    public final static Version v1_13_X = Version.of(1, 13, 0);
    public final static Version v1_13_1 = Version.of(1, 13, 1);
    public final static Version v1_13_2 = Version.of(1, 13, 2);
    public final static Version v1_14_X = Version.of(1, 14, 0);
    public final static Version v1_14_1 = Version.of(1, 14, 1);
    public final static Version v1_14_2 = Version.of(1, 14, 2);
    public final static Version v1_14_3 = Version.of(1, 14, 3);
    public final static Version v1_14_4 = Version.of(1, 14, 4);
    public final static Version v1_15_X = Version.of(1, 15, 0);
    public final static Version v1_15_1 = Version.of(1, 15, 1);
    public final static Version v1_15_2 = Version.of(1, 15, 2);
    public final static Version v1_16_X = Version.of(1, 16, 0);
    public final static Version v1_16_1 = Version.of(1, 16, 1);
    public final static Version v1_16_2 = Version.of(1, 16, 2);
    public final static Version v1_16_3 = Version.of(1, 16, 3);
    public final static Version v1_16_4 = Version.of(1, 16, 4);
    public final static Version v1_16_5 = Version.of(1, 16, 5);
    public final static Version v1_17_X = Version.of(1, 17, 0);
    public final static Version v1_17_1 = Version.of(1, 17, 1);
    public final static Version v1_18_X = Version.of(1, 18, 0);
    public final static Version v1_18_1 = Version.of(1, 18, 1);
    public final static Version v1_18_2 = Version.of(1, 18, 2);
    public final static Version v1_19_X = Version.of(1, 19, 0);
    public final static Version v1_19_1 = Version.of(1, 19, 1);
    public final static Version v1_19_2 = Version.of(1, 19, 2);
    public final static Version v1_19_3 = Version.of(1, 19, 3);
    public final static Version v1_19_4 = Version.of(1, 19, 4);
    public final static Version v1_20_X = Version.of(1, 20, 0);
    public final static Version v1_20_1 = Version.of(1, 20, 1);

    private final static Server server = Bukkit.getServer();

    private static long currentTick = 0;

    /**
     * Start counting the tick
     *
     * @param owner the plugin owning the tick counter
     * @return if the task could be started
     */
    public static boolean startTickCount(final Plugin owner) {
        if (currentTick == 0) {
            BukkitScheduler scheduler = Bukkit.getScheduler();

            scheduler.runTaskTimer(owner, () -> {
                currentTick++;
            }, 0, 1);

            return true;
        }

        return false;
    }

    /**
     * Get the spigot server version
     *
     * @return the server version
     */
    public static Version getVersion() {
        String bukkitVersion = server.getBukkitVersion(); //For example: 1.19.4-R0.1-SNAPSHOT
        String[] data = bukkitVersion.split("-");

        String version = data[0];
        String info = bukkitVersion.replace(version + "-", "");

        String[] versionData = version.split("\\.");
        int mayor, minor, patch = 0;

        if (versionData.length == 2) {
            mayor = Integer.parseInt(versionData[0]);
            minor = Integer.parseInt(versionData[1]);
        } else {
            mayor = Integer.parseInt(versionData[0]);
            minor = Integer.parseInt(versionData[1]);
            patch = Integer.parseInt(versionData[2]);
        }

        return Version.of(mayor, minor, patch, info);
    }

    /**
     * Get the spigot server version release
     *
     * @return the release version
     */
    public static String getRelease() {
        String bukkitVersion = server.getBukkitVersion(); //For example: 1.19.4-R0.1-SNAPSHOT
        String[] data = bukkitVersion.split("-");

        return data[1];
    }

    /**
     * Get the spigot server release type
     *
     * @return the release type
     */
    public static ReleaseType getVersionType() {
        String bukkitVersion = server.getBukkitVersion(); //For example: 1.19.4-R0.1-SNAPSHOT
        String[] data = bukkitVersion.split("-");

        String type = data[2];
        return ReleaseType.valueOf(type);
    }

    /**
     * Get the version update. For example, if the
     * server version is 1.19.2, this will return 19
     *
     * @return the version update
     */
    public static int getUpdate() {
        Version version = getVersion();
        return version.getMinor();
    }

    /**
     * Get the version build. For example, if the
     * server version is 1.19.2, this will return 1.19
     *
     * @return the version build
     */
    public static float getBuild() {
        Version version = getVersion();
        return Float.parseFloat(version.getMayor() + "." + version.getMinor());
    }

    /**
     * Get if the server version is over the
     * provided version
     *
     * @param other the version to check with
     * @return if the server version is over
     * the specified version
     */
    public static boolean isOver(final Version other) {
        Version current = getVersion();
        int comparator = current.compareTo(other);

        return comparator >= 1;
    }

    /**
     * Get if the server version is under the
     * provided version
     *
     * @param other the version to check with
     * @return if the server version is under
     * the specified version
     */
    public static boolean isUnder(final Version other) {
        Version current = getVersion();
        int comparator = current.compareTo(other);

        return comparator <= -1;
    }

    /**
     * Check if the current version is between the
     * specified versions
     *
     * @param start the version start range
     * @param end the version end range
     * @return if the version is between
     */
    public static boolean isBetween(final Version start, final Version end) {
        return isOver(start) && isUnder(end);
    }

    /**
     * Get the server package version
     *
     * @return the package version
     */
    public static VersionType packageVersion() {
        String version = server.getClass().getPackage().getName().replace(".", ",").split(",")[3];
        if (isUnder(v1_7_2)) return VersionType.LEGACY;
        if (isOver(v1_20_1)) return VersionType.FUTURE;

        return VersionType.valueOf(version);
    }

    /**
     * Get the current tick
     *
     * @return the current tick
     */
    public static long getCurrentTick() {
        return currentTick;
    }

    /**
     * Get a future tick
     *
     * @param plusTime the time to add
     * @param unit the time unit to add as
     * @return the future tick
     */
    public static long getFutureTick(final long plusTime, final TimeUnit unit) {
        long current = currentTick;
        long seconds = TimeUnit.SECONDS.convert(plusTime, unit);

        return current + (seconds * 20); //Each 20 ticks is 1 second
    }

    /**
     * Get if the spigot server version is the
     * provided version
     *
     * @param other the other server version
     * @return if the version matches
     */
    public static boolean equals(final Version other) {
        Version current = getVersion();
        int comparator = current.compareTo(other);

        return comparator == 0;
    }

    /**
     * Get if the server is MCP based
     *
     * @return if the server is MCP
     */
    public static boolean isMCP() {
        return Bukkit.getName().toLowerCase().contains("mcp");
    }

    /**
     * Get a bukkit class
     *
     * @param name the class name
     * @param route the class route
     * @return the bukkit class
     */
    public static Optional<Class<?>> orgBukkitCraftbukkit(final String name, final String... route) {
        String version = server.getClass().getPackage().getName().replace(".", ",").split(",")[3];
        String classLocation = "org.bukkit.craftbukkit." + version;

        Class<?> instance = null;
        try {
            StringBuilder classPath = new StringBuilder(classLocation);
            for (String path : route) {
                classPath.append(".").append(path);
            }

            instance = Class.forName(classPath.append(".").append(name).toString());
        } catch (ClassNotFoundException ignored) {}

        return Optional.ofNullable(instance);
    }

    /**
     * Get a minecraft class
     *
     * @param name the class name
     * @param route the class route
     * @return the minecraft class
     */
    public static Optional<Class<?>> netMinecraftServer(final String name, final String... route) {
        String version = server.getClass().getPackage().getName().replace(".", ",").split(",")[3];
        String classLocation = "net.minecraft.server";
        if (getUpdate() < 19) { //When mojang stopped using net.minecraft.server.<version>
            classLocation += "net.minecraft.server." + version;
        }

        Class<?> instance = null;
        try {
            StringBuilder classPath = new StringBuilder(classLocation);
            for (String path : route) {
                classPath.append(".").append(path);
            }

            instance = Class.forName(classPath.append(".").append(name).toString());
        } catch (ClassNotFoundException ignored) {}

        return Optional.ofNullable(instance);
    }

    /**
     * Send a packet to the player
     *
     * @param player the player
     * @param packet the packet to send
     * @return if the packet was able to be sent
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean sendPacket(final Player player, final Object packet) {
        Class<?> packetClass = netMinecraftServer("Packet").orElse(null);

        if (packetClass == null) return false;
        if (packet == null) return false;
        if (player == null) {
            Bukkit.getServer().getOnlinePlayers().forEach((online) -> SpigotServer.sendPacket(online, packet));
            return true;
        }

        Class<? extends Player> playerClass = player.getClass();
        Method getHandle = null;
        try {
            getHandle = playerClass.getMethod("getHandle");
        } catch (NoSuchMethodException ignored) {}
        if (getHandle == null) return false;

        Object entityPlayer = null;
        try {
            entityPlayer = getHandle.invoke(player);
        } catch (IllegalAccessException | InvocationTargetException ignored) {}
        if (entityPlayer == null) return false;

        Class<?> entityPlayerClass = entityPlayer.getClass();
        Field PLAYER_CONNECTION = null;
        try {
            PLAYER_CONNECTION = entityPlayerClass.getField("playerConnection");
        } catch (NoSuchFieldException ignored) {}
        if (PLAYER_CONNECTION == null) return false;

        Object playerConnection = null;
        try {
            playerConnection = PLAYER_CONNECTION.get(entityPlayer);
        } catch (IllegalAccessException ignored) {}
        if (playerConnection == null) return false;

        Class<?> playerConnectionClass = playerConnection.getClass();
        Method sendPacket = null;
        try {
            sendPacket = playerConnectionClass.getMethod("sendPacket", packetClass);
        } catch (NoSuchMethodException ignored) {}
        if (sendPacket == null) return false;

        try {
            sendPacket.invoke(playerConnection, packet);
            return true;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            return false;
        }
    }
}
