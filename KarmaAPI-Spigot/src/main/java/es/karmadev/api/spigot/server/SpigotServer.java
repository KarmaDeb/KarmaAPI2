package es.karmadev.api.spigot.server;

import es.karmadev.api.minecraft.BukkitVersion;
import es.karmadev.api.minecraft.MinecraftVersion;
import es.karmadev.api.version.Version;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Spigot server utilities
 */
@SuppressWarnings("unused")
public class SpigotServer {

    private final static Server server = Bukkit.getServer();

    /**
     * Get the spigot server version
     *
     * @return the server version
     */
    public static MinecraftVersion getVersion() {
        String bukkitVersion = server.getBukkitVersion(); //For example, 1.19.4-R0.1-SNAPSHOT
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

        return MinecraftVersion.of(mayor, minor, patch, info);
    }

    /**
     * Get the spigot server version release
     *
     * @return the release version
     */
    public static String getRelease() {
        String bukkitVersion = server.getBukkitVersion(); //For example, 1.19.4-R0.1-SNAPSHOT
        String[] data = bukkitVersion.split("-");

        return data[1];
    }

    /**
     * Get the spigot server release type
     *
     * @return the release type
     */
    public static ReleaseType getVersionType() {
        String bukkitVersion = server.getBukkitVersion(); //For example, 1.19.4-R0.1-SNAPSHOT
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
     * Get if the server version is at or
     * over the provided version
     *
     * @param other the version to check with
     * @return if the server version is over
     * the specified version
     */
    public static boolean atOrOver(final Version other) {
        Version current = getVersion();
        int comparator = current.compareTo(other);

        return comparator >= 0;
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

        return comparator > 0;
    }

    /**
     * Get if the server version is at or
     * under the provided version
     *
     * @param other the version to check with
     * @return if the server version is under
     * the specified version
     */
    public static boolean atOrUnder(final Version other) {
        Version current = getVersion();
        int comparator = current.compareTo(other);

        return comparator <= 0;
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

        return comparator < 0;
    }

    /**
     * Get if the server version is over the
     * provided version
     *
     * @param other the version to check with
     * @return if the server version is over
     * the specified version
     */
    public static boolean isSameAs(final Version other) {
        /*if (BukkitVersion.v1_8_R1.contains(other)) {
            try {
                Class.forName("org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer");
                return true;
            } catch (NoClassDefFoundError | ClassNotFoundException ex) {
                return false;
            }
        }
        if (BukkitVersion.v1_8_R2.contains(other)) {
            try {
                Class.forName("org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer");
                return true;
            } catch (NoClassDefFoundError | ClassNotFoundException ex) {
                return false;
            }
        }
        if (BukkitVersion.v1_8_R3.contains(other)) {
            try {
                Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer");
                return true;
            } catch (NoClassDefFoundError | ClassNotFoundException ex) {
                return false;
            }
        }*/

        Version current = getVersion();
        int comparator = current.compareTo(other);

        return comparator == 0;
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
        return isSameAs(start) || isSameAs(end) || (isOver(start) && isUnder(end));
    }

    /**
     * Get the server package version
     *
     * @return the package version
     */
    public static BukkitVersion packageVersion() {
        String version = server.getClass().getPackage().getName().replace(".", ",").split(",")[3];
        if (isUnder(MinecraftVersion.v1_7_2)) return BukkitVersion.LEGACY;
        if (isOver(MinecraftVersion.LATEST)) return BukkitVersion.FUTURE;

        return BukkitVersion.valueOf(version);
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
        String classLocation = "net.minecraft";
        if (isUnder(MinecraftVersion.v1_19_X)) { //When mojang stopped using net.minecraft.server.<version>
            classLocation += "." + version;
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
