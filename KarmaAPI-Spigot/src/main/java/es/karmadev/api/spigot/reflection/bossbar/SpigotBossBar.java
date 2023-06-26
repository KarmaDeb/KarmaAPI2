package es.karmadev.api.spigot.reflection.bossbar;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.KarmaPlugin;
import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.minecraft.bossbar.BossBarProvider;
import es.karmadev.api.minecraft.bossbar.component.BarColor;
import es.karmadev.api.minecraft.bossbar.component.BarFlag;
import es.karmadev.api.minecraft.bossbar.component.BarProgress;
import es.karmadev.api.minecraft.bossbar.component.BarType;
import es.karmadev.api.minecraft.client.GlobalPlayer;
import es.karmadev.api.minecraft.client.exception.NonAvailableException;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.runner.TaskRunner;
import es.karmadev.api.schedule.runner.async.AsyncTaskExecutor;
import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.spigot.server.SpigotServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class SpigotBossBar extends BossBarProvider {

    private final static Map<UUID, Queue<SpigotBossBar>> ques = new ConcurrentHashMap<>();
    private final static Map<UUID, TaskRunner> runners = new ConcurrentHashMap<>();
    private final static Map<UUID, Integer> displayBars = new ConcurrentHashMap<>();

    private static int globalId = 0;
    private final int id = globalId++;
    private BarColor color = BarColor.PURPLE;
    private BarType type = BarType.SOLID;
    private BarProgress progress = BarProgress.STATIC;
    private double time = 10d;
    private double health = 1.0d;
    private double livedTime = 0d;
    private boolean cancelled = false;
    private String content;
    private TaskRunner barTimer;

    private static boolean packetCompatible = false;
    private static boolean useHealth = false;
    private static Class<?> craftWorldClass;
    private static Constructor<?> packetPlayOutEntityDestroy;
    private static Constructor<?> witherConstructor;
    private static Constructor<?> entityLivingConstructor;
    private static Constructor<?> packetTeleportConstructor;
    private static Constructor<?> packetPlayOutMetadata;

    private static Method craftWorldHandle;
    private static Method witherSetLocation;
    private static Method witherSetProgress;
    private static Method witherSetInvisible;
    private static Method witherSetCustomNameVisible;
    private static Method witherGetId;

    static {
        buildReflection();
    }

    /**
     * Get the boss bar ID
     *
     * @return the boss bar ID
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Set the boss bar color
     *
     * @param color the color
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     *                                       color change
     */
    @Override
    public BossBarProvider color(final BarColor color) throws UnsupportedOperationException {
        this.color = color;
        return this;
    }

    /**
     * Set the boss bar type
     *
     * @param type the type
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     *                                       type change
     */
    @Override
    public BossBarProvider type(final BarType type) throws UnsupportedOperationException {
        this.type = type;
        return this;
    }

    /**
     * Set the boss bar progress type
     *
     * @param progress the progress type
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     *                                       progress change
     */
    @Override
    public BossBarProvider progress(final BarProgress progress) throws UnsupportedOperationException {
        this.progress = progress;
        return this;
    }

    /**
     * Set the boss bar display time
     *
     * @param time the new display time
     * @return the modified boss bar
     */
    @Override
    public BossBarProvider displayTime(final double time) {
        this.time = time;
        return this;
    }

    /**
     * Set the boss bar progress manually
     *
     * @param health the boss bar progress
     */
    @Override
    public void setProgress(final double health) {
        this.health = health;
    }

    /**
     * Cancel the boss bar message
     */
    @Override
    public void cancel() {
        if (cancelled) return;
        cancelled = true;
    }

    /**
     * Send the boss bar to the players
     *
     * @param players the players to send the boss bar
     *                to
     */
    @Override
    public void send(final Collection<GlobalPlayer> players) {
        players.forEach(this::send);
    }

    /**
     * Send the boss bar to the players
     *
     * @param players the players to send the boss bar
     *                to
     */
    @Override
    public void send(final GlobalPlayer[] players) {
        Arrays.asList(players).forEach(this::send);
    }

    /**
     * Send the boss bar to the player
     *
     * @param player the player to send the actionbar to
     */
    @Override
    public void send(final GlobalPlayer player) {
        KarmaPlugin plugin = (KarmaPlugin) KarmaKore.INSTANCE();
        if (plugin == null) return;

        Queue<SpigotBossBar> que = ques.computeIfAbsent(player.getUUID(), (barQue) -> new ConcurrentLinkedQueue<>());
        int initialMaxBars = 4;
        if (SpigotServer.isUnder(SpigotServer.v1_13_X)) {
            initialMaxBars = 1;
            if (!packetCompatible) return; //We don't support boss bar
        }

        final int maxBars = initialMaxBars;
        que.add(this);
        ques.put(player.getUUID(), que);

        TaskRunner runner = runners.getOrDefault(player.getUUID(), null);
        if (runner == null) {
            runner = new AsyncTaskExecutor(1, TimeUnit.SECONDS);
            runner.setRepeating(true);

            runner.on(TaskEvent.RESTART, () -> {
                if (player.isOnline()) {
                    int displayBars = SpigotBossBar.displayBars.getOrDefault(player.getUUID(), 0);
                    if (displayBars < maxBars) {
                        Queue<SpigotBossBar> updatedQue = ques.computeIfAbsent(player.getUUID(), (barQue) -> new ConcurrentLinkedQueue<>());
                        SpigotBossBar next = updatedQue.poll();

                        if (next != null && !next.cancelled) {
                            next.display(plugin, player);
                        }
                    }
                }
            });
        }

        runners.put(player.getUUID(), runner);
    }

    /**
     * Display the boss bar
     *
     * @param plugin the plugin
     * @param player the player to display to
     */
    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    private void display(final KarmaPlugin plugin, final GlobalPlayer player) {
        switch (progress) {
            case PLAYER:
                //NOT YET IMPLEMENTED
            case STATIC:
                //NOT YET IMPLEMENTED
            case STATIC_RANDOM:
                //NOT YET IMPLEMENTED
            case RANDOM:
                //NOT YET IMPLEMENTED
            case HEALTH_UP:
                livedTime = 0;
                break;
            case HEALTH_DOWN:
                livedTime = time;
                break;
        }

        if (SpigotServer.isUnder(SpigotServer.v1_13_X)) {
            try {
                int initialBars = displayBars.getOrDefault(player.getUUID(), 0);
                initialBars++;
                displayBars.put(player.getUUID(), initialBars);

                World world = plugin.getServer().getWorld(player.getWorld());
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                float yaw = player.getYaw();
                float pitch = player.getPitch();

                Location location = new Location(world, x, y, z);
                location.setYaw(yaw);
                location.setPitch(pitch);

                Object craftWorld = craftWorldClass.cast(world);
                Object worldServer = craftWorldHandle.invoke(craftWorld);

                Object playerHandle = player.getHandler();
                Player initialPlayerInstance = null;
                if (playerHandle instanceof Player) {
                    initialPlayerInstance = (Player) playerHandle;
                }
                if (playerHandle instanceof OfflinePlayer) {
                    if (player.isOnline()) {
                        initialPlayerInstance = ((OfflinePlayer) playerHandle).getPlayer();
                    }
                }
                if (initialPlayerInstance == null) return;
                Player playerInstance = initialPlayerInstance;

                Object wither = witherConstructor.newInstance(worldServer);

                witherSetCustomNameVisible.invoke(wither, true);
                witherSetInvisible.invoke(wither, false);
                witherSetLocation.invoke(wither, x, y - 10, z, yaw, pitch);
                setCustomName(wither, content);

                Object packetPlayOutEntityLiving = entityLivingConstructor.newInstance(wither);
                SpigotServer.sendPacket(playerInstance, packetPlayOutEntityLiving);
                updateMetadata(playerInstance, wither);

                barTimer = new AsyncTaskExecutor((long) time, TimeUnit.SECONDS);
                barTimer.on(TaskEvent.TICK, (time) -> {
                    if (time >= 2) {
                        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                            try {
                                Location newLocation = playerInstance.getLocation().clone()
                                        .add(playerInstance.getLocation().getDirection().multiply(20));

                                witherSetLocation.invoke(wither,
                                        newLocation.getX(),
                                        newLocation.getY() - 10,
                                        newLocation.getZ(),
                                        newLocation.getYaw(),
                                        newLocation.getPitch());

                                Object packetPlayOutEntityTeleport = packetTeleportConstructor.newInstance(wither);
                                SpigotServer.sendPacket(playerInstance, packetPlayOutEntityTeleport);
                            } catch (InvocationTargetException | IllegalAccessException | InstantiationException ex) {
                                ExceptionCollector.catchException(SpigotBossBar.class, ex);
                            }
                        });
                    }
                });
                barTimer.on(TaskEvent.END, () -> Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    cancelled = true;
                    try {
                        int id = (int) witherGetId.invoke(wither);
                        Object packetPLayOutEntityDestroy = packetPlayOutEntityDestroy.newInstance(new int[]{id});

                        SpigotServer.sendPacket(playerInstance, packetPLayOutEntityDestroy);
                        int bars = displayBars.getOrDefault(player.getUUID(), 0);
                        bars--;
                        displayBars.put(player.getUUID(), bars);
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException ex) {
                        ExceptionCollector.catchException(SpigotBossBar.class, ex);
                    }
                }));
                barTimer.on(TaskEvent.STOP, () -> Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    cancelled = true;
                    try {
                        int id = (int) witherGetId.invoke(wither);
                        Object packetPLayOutEntityDestroy = packetPlayOutEntityDestroy.newInstance(new int[]{id});

                        SpigotServer.sendPacket(playerInstance, packetPLayOutEntityDestroy);
                        int bars = displayBars.getOrDefault(player.getUUID(), 0);
                        bars--;
                        displayBars.put(player.getUUID(), bars);
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException ex) {
                        ExceptionCollector.catchException(SpigotBossBar.class, ex);
                    }
                }));

                TaskRunner hpTimer = new AsyncTaskExecutor(1, TimeUnit.SECONDS);
                hpTimer.setRepeating(true);

                hpTimer.on(TaskEvent.RESTART, () -> Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    if (cancelled || !playerInstance.isOnline()) {
                        barTimer.stop();
                        hpTimer.stop();
                        return;
                    }

                    double percentage = 1000;
                    switch (progress) {
                        case PLAYER:
                            //NOT YET IMPLEMENTED
                        case STATIC:
                            //NOT YET IMPLEMENTED
                        case STATIC_RANDOM:
                            //NOT YET IMPLEMENTED
                        case RANDOM:
                            //NOT YET IMPLEMENTED
                        case HEALTH_UP:
                            percentage = livedTime / time;
                            livedTime++;
                            break;
                        case HEALTH_DOWN:
                            percentage = livedTime / time;
                            livedTime--;
                            break;
                    }
                    percentage = Math.max(0, percentage);
                    if (useHealth) {
                        percentage *= 300;
                    }

                    try {
                        witherSetProgress.invoke(wither, percentage);
                    } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ex) {
                        try {
                            witherSetProgress.invoke(wither, (float) percentage);
                        } catch (IllegalAccessException | InvocationTargetException ex2) {
                            ExceptionCollector.catchException(SpigotBossBar.class, ex2);
                        }
                    }

                    updateMetadata(playerInstance, wither);
                }));

                barTimer.start();
                hpTimer.start();
            } catch (NonAvailableException | InvocationTargetException |
                    IllegalAccessException | InstantiationException ex) {
                ExceptionCollector.catchException(SpigotBossBar.class, ex);
            }
        }
    }

    /**
     * Update the boss bar text
     *
     * @param newMessage    the new boss bar message
     * @param resetProgress if reset the boss bar progress
     * @return if the boss bar was able to be updated
     */
    @Override
    public boolean update(final String newMessage, final boolean resetProgress) {
        return false;
    }

    /**
     * Check if the boss bar is valid
     *
     * @return if the boss bar entity is alive or is still running
     */
    @Override
    public boolean isValid() {
        return false;
    }

    /**
     * Check if the boss bar has been cancelled
     *
     * @return if the boss bar entity is alive and/or running but is
     * cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Get the boss bar content
     *
     * @return the content
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * Get the boss bar color
     *
     * @return the color
     */
    @Override
    public BarColor getColor() {
        return color;
    }

    /**
     * Get the boss bar type
     *
     * @return the type
     */
    @Override
    public BarType getType() {
        return type;
    }

    /**
     * Get the boss bar progress type
     *
     * @return the progress type
     */
    @Override
    public BarProgress getProgressType() {
        return progress;
    }

    /**
     * Get the boss bar progress
     *
     * @return the bar progress
     */
    @Override
    public double getProgress() {
        return health;
    }

    /**
     * Get the boss bar flags
     *
     * @return the bar flags
     */
    @Override
    public BarFlag[] getFlags() {
        return flagSet.toArray(new BarFlag[0]).clone();
    }

    /**
     * Build the reflection methods
     */
    @SuppressWarnings("DataFlowIssue")
    private static void buildReflection() {
        packetCompatible = false; //Reset
        craftWorldClass = SpigotServer.orgBukkitCraftbukkit("CraftWorld").orElse(null);
        if (craftWorldClass == null) return;

        Class<?> entityWither = SpigotServer.netMinecraftServer("EntityWither").orElse(null);
        Class<?> world = SpigotServer.netMinecraftServer("World").orElse(null);
        Class<?> spawnEntityLiving = SpigotServer.netMinecraftServer("PacketPlayOutSpawnEntityLiving").orElse(null);
        Class<?> entityLiving = SpigotServer.netMinecraftServer("EntityLiving").orElse(null);
        Class<?> packet = SpigotServer.netMinecraftServer("Packet").orElse(null);
        Class<?> packetDestroy = SpigotServer.netMinecraftServer("PacketPlayOutEntityDestroy").orElse(null);
        Class<?> dataWatcher = SpigotServer.netMinecraftServer("DataWatcher").orElse(null);
        Class<?> packetMetadata = SpigotServer.netMinecraftServer("PacketPlayOutEntityMetadata").orElse(null);
        Class<?> packetTeleport = SpigotServer.netMinecraftServer("PacketPlayOutEntityTeleport").orElse(null);
        Class<?> entity = SpigotServer.netMinecraftServer("Entity").orElse(null);

        if (ObjectUtils.areNullOrEmpty(false, entityWither, world, spawnEntityLiving,
                entityLiving, packet, packetDestroy, dataWatcher, packetMetadata, packetTeleport,
                entity)) return;

        Constructor<?> witherConstructor = null;
        try {
            witherConstructor = entityWither.getConstructor(world);
        } catch (NoSuchMethodException ignored) {}
        if (witherConstructor == null) return;
        SpigotBossBar.witherConstructor = witherConstructor;

        Constructor<?> entityLivingConstructor = null;
        try {
            entityLivingConstructor = spawnEntityLiving.getConstructor(entityLiving);
        } catch (NoSuchMethodException ignored) {}
        if (entityLivingConstructor == null) return;
        SpigotBossBar.entityLivingConstructor = entityLivingConstructor;

        Constructor<?> destroyConstructor = null;
        try {
            destroyConstructor = packetDestroy.getConstructor(int[].class);
        } catch (NoSuchMethodException ignored) {}
        if (destroyConstructor == null) return;
        packetPlayOutEntityDestroy = destroyConstructor;

        Constructor<?> packetPlayMetadataConstructor = null;
        try {
            packetPlayMetadataConstructor = packetMetadata.getConstructor(int.class, dataWatcher, boolean.class);
        } catch (NoSuchMethodException ignored) {}
        if (packetPlayMetadataConstructor == null) return;
        packetPlayOutMetadata = packetPlayMetadataConstructor;

        Constructor<?> teleportConstructor = null;
        try {
            teleportConstructor = packetTeleport.getConstructor(entity);
        } catch (NoSuchMethodException ignored) {}
        if (teleportConstructor == null) return;
        packetTeleportConstructor = teleportConstructor;

        Method getWorldHandle = null;
        try {
            getWorldHandle = craftWorldClass.getMethod("getHandle");
        } catch (NoSuchMethodException ignored) {}
        if (getWorldHandle == null) return;
        craftWorldHandle = getWorldHandle;

        Method setLocationMethod = null;
        try {
            setLocationMethod = entityWither.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
        } catch (NoSuchMethodException ignored) {}
        if (setLocationMethod == null) return;
        witherSetLocation = setLocationMethod;

        Method setInvisibleMethod = null;
        try {
            setInvisibleMethod = entityWither.getMethod("setInvisible", boolean.class);
        } catch (NoSuchMethodException ignored) {}
        if (setInvisibleMethod == null) return;
        witherSetInvisible = setInvisibleMethod;

        Method setCustomNameMethod = null;
        try {
            setCustomNameMethod = entityWither.getMethod("setCustomNameVisible", boolean.class);
        } catch (NoSuchMethodException ignored) {}
        if (setCustomNameMethod == null) return;
        witherSetCustomNameVisible = setCustomNameMethod;

        Method getIdMethod = null;
        try {
            getIdMethod = entityWither.getMethod("getId");
        } catch (NoSuchMethodException ignored) {}
        if (getIdMethod == null) return;
        witherGetId = getIdMethod;

        Method setProgress = null;
        try {
            setProgress = entityWither.getMethod("setProgress", double.class);
        } catch (Throwable ex) {
            try {
                setProgress = entityWither.getMethod("setProgress", float.class);
            } catch (Throwable ex2) {
                useHealth = true;
                try {
                    setProgress = entityWither.getMethod("setHealth", double.class);
                } catch (Throwable ex3) {
                    try {
                        setProgress = entityWither.getMethod("setHealth", float.class);
                    } catch (NoSuchMethodException ignored) {}
                }
            }
        }
        if (setProgress == null) return;

        witherSetProgress = setProgress;
        packetCompatible = true;
    }

    /**
     * Set the custom name of the wither
     *
     * @param wither the wither object
     * @param message the name
     * @throws InvocationTargetException as part of the method
     * @throws IllegalAccessException as part of the method
     */
    private void setCustomName(final Object wither, final String message) throws InvocationTargetException, IllegalAccessException {
        AtomicReference<Method> customName = new AtomicReference<>();
        AtomicBoolean component = new AtomicBoolean(false);
        try {
            customName.set(wither.getClass().getMethod("setCustomName", String.class));
        } catch (NoSuchMethodException e) {
            SpigotServer.netMinecraftServer("IChatBaseComponent").ifPresent((IChatBaseComponent) -> {
                try {
                    customName.set(wither.getClass().getMethod("setCustomName", IChatBaseComponent));
                    component.set(true);
                } catch (NoSuchMethodException ex) {
                    ExceptionCollector.catchException(SpigotBossBar.class, ex);
                }
            });
        }

        if (customName.get() != null) {
            if (component.get()) {
                SpigotServer.netMinecraftServer("ChatMessage").ifPresent((ChatMessage) -> {
                    try {
                        Constructor<?> constructor = ChatMessage.getConstructor(String.class, Object[].class);
                        Object chatMessageComponent = constructor.newInstance(ConsoleColor.parse(message), new Object[0]);
                        customName.get().invoke(wither, chatMessageComponent);
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                        ExceptionCollector.catchException(SpigotBossBar.class, ex);
                    }
                });
            } else {
                customName.get().invoke(wither, ConsoleColor.parse(message));
            }
        }
    }

    /**
     * Update the boss bar metadata
     *
     * @param player the player to send the packet to
     * @param wither the wither entity
     */
    private void updateMetadata(final Player player, final Object wither) {
        try {
            Method getId = wither.getClass().getMethod("getId");
            Method getDataWatcher = wither.getClass().getMethod("getDataWatcher");
            int id = (int) getId.invoke(wither);
            Object dataWatcher = getDataWatcher.invoke(wither);

            Object packetPlayOutEntityMetadata = packetPlayOutMetadata.newInstance(id, dataWatcher, true);
            SpigotServer.sendPacket(player, packetPlayOutEntityMetadata);
        } catch (Throwable ex) {
            ExceptionCollector.catchException(SpigotBossBar.class, ex);
        }
    }
}
