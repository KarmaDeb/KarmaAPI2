package es.karmadev.api.spigot.reflection.bossbar;

import es.karmadev.api.minecraft.MinecraftVersion;
import es.karmadev.api.minecraft.bossbar.BossBarProvider;
import es.karmadev.api.minecraft.bossbar.component.BarColor;
import es.karmadev.api.minecraft.bossbar.component.BarFlag;
import es.karmadev.api.minecraft.bossbar.component.BarProgress;
import es.karmadev.api.minecraft.bossbar.component.BarType;
import es.karmadev.api.minecraft.text.Colorize;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.reflection.bossbar.nms.Boss;
import es.karmadev.api.spigot.reflection.bossbar.nms.ModernBoss;
import es.karmadev.api.spigot.server.SpigotServer;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class SpigotBossBar extends BossBarProvider<Player> {

    private static int globalId = 0;
    private final int id = globalId++;

    private final static Map<UUID, Set<BarTask>> bars = new ConcurrentHashMap<>();
    private final static Map<UUID, Queue<BarTask>> barsQue = new ConcurrentHashMap<>();

    @NonNull
    private String contentCache;
    @NonNull
    private String content;

    private BarColor barColorCache = BarColor.PURPLE;
    private BarColor barColor = BarColor.PURPLE;
    private BarType barTypeCache = BarType.SEGMENTED_6;
    private BarType barType = BarType.SEGMENTED_6;
    private BarProgress barProgressCache = BarProgress.STATIC;
    private BarProgress barProgress = BarProgress.STATIC;

    private long displayTime = 10;
    private long remainingTime = displayTime;
    private double progressCache = 1.0;
    private double progress = 1.0;
    private boolean cancelled = false;
    private final EnumSet<BarFlag> flagSetCache = EnumSet.noneOf(BarFlag.class);

    /*
    The global wither might not be supported
    by all versions, specially 1.8. As in those
    versions BossBar API was not a thing, each
    client must have a different boss bar
     */
    private Boss globalWither = null;

    private final Map<UUID, Boss> bosses = new ConcurrentHashMap<>();

    /**
     * Create a new boss bar
     */
    @SuppressWarnings("unused")
    public SpigotBossBar() {
        this("");
    }

    /**
     * Create a new boss bar
     *
     * @param content the boss bar content
     */
    public SpigotBossBar(final @NonNull String content) {
        this.content = content;
        this.contentCache = content;
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
    public BossBarProvider<Player> color(final BarColor color) throws UnsupportedOperationException {
        this.barColorCache = color;

        if (bosses.isEmpty()) this.barColor = color;
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
    public BossBarProvider<Player> type(final BarType type) throws UnsupportedOperationException {
        this.barTypeCache = type;

        if (bosses.isEmpty()) this.barType = type;
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
    public BossBarProvider<Player> progress(final BarProgress progress) throws UnsupportedOperationException {
        this.barProgressCache = progress;

        if (bosses.isEmpty()) this.barProgress = progress;
        return this;
    }

    /**
     * Set the boss bar display time
     *
     * @param time the new display time
     * @return the modified boss bar
     */
    @Override
    public BossBarProvider<Player> displayTime(final long time) {
        if (time < 0) {
            this.displayTime = -1;
            this.remainingTime = -1; //Infinite
            return this;
        }

        this.displayTime = Math.max(1, time);
        this.remainingTime = displayTime;
        return this;
    }

    /**
     * Set the boss bar progress manually
     *
     * @param progress the boss bar progress
     */
    @Override
    public void setProgress(final double progress) {
        double value = progress;

        if (value < 0) {
            value = 0;
        } else if (value > 1.0) {
            if (value >= 100) {
                if (value <= 300) {
                    //We perform the conversion from 0-300 health scale
                    value = value / 300.0d;
                } else {
                    value = 1.0d;
                }
            } else {
                //We perform the conversion from 0-100 % scale
                value = value / 100.0d;
            }
        }

        this.progressCache = value;
        if (bosses.isEmpty()) this.progress = value;
    }

    /**
     * Set the boss bar message
     *
     * @param content the message
     */
    @Override
    public void setContent(final String content) {
        this.contentCache = content;
        if (bosses.isEmpty()) this.content = content;
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
     * @param onTick  the action to perform on each tick
     * @param players the players to send the boss bar
     *                to
     */
    @Override
    public void send(final Consumer<Player> onTick, final Collection<Player> players) {
        if (cancelled) return;
        for(Player player : players) {
            sendTo(player, onTick);
        }
    }

    /**
     * Send the boss bar to the player
     *
     * @param player the player to send the
     *               boss bar to
     */
    private void sendTo(final Player player, final Consumer<Player> onTick) {
        Set<BarTask> activeBars = bars.computeIfAbsent(player.getUniqueId(), (s) -> ConcurrentHashMap.newKeySet());
        Queue<BarTask> barQueue = barsQue.computeIfAbsent(player.getUniqueId(), (q) -> new ConcurrentLinkedDeque<>());

        BarTask task = BarTask.of(this, onTick);
        int maxBars = 4;
        if (SpigotServer.atOrUnder(MinecraftVersion.v1_8_9)) {
            maxBars = 1;
        }
        if (activeBars.size() >= maxBars) {
            barQueue.add(task);
            barsQue.put(player.getUniqueId(), barQueue);
            return;
        }
        activeBars.add(task);
        bars.put(player.getUniqueId(), activeBars);

        Boss boss = Boss.getBoss();
        boolean schedule = !(boss instanceof ModernBoss);

        if (boss instanceof ModernBoss && globalWither == null) {
            globalWither = boss;
            schedule = true;

            ModernBoss modern = (ModernBoss) boss;
            BossBar bar = modern.getWither();

            for (BarFlag bFlag : flagSet) {
                org.bukkit.boss.BarFlag flag = org.bukkit.boss.BarFlag.valueOf(bFlag.getBukkitName());
                bar.addFlag(flag);
            }

            org.bukkit.boss.BarColor bukkitColor = org.bukkit.boss.BarColor.valueOf(barColor.name());
            bar.setColor(bukkitColor);

            BarStyle bukkitStyle = BarStyle.valueOf(barType.name());
            bar.setStyle(bukkitStyle);
        }

        if (globalWither == null) {
            boss.add(player);
            bosses.put(player.getUniqueId(), boss);
        } else {
            if (!boss.equals(globalWither)) {
                boss.destroyWither();
            }

            globalWither.add(player);
            bosses.put(player.getUniqueId(), globalWither);
        }

        if (!schedule) return;

        boss.setName(Colorize.colorize(content));
        applyProgress(player, boss);
        if (barProgress.equals(BarProgress.STATIC_RANDOM)) {
            progress = Math.random();
        }
        if (onTick != null) {
            onTick.accept(player);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                UUID playerId = player.getUniqueId();
                Player online = Bukkit.getPlayer(playerId);
                if (online == null || !online.isOnline()) {
                    boss.remove(player);
                    bosses.remove(playerId);
                }

                long rm = remainingTime--;
                if (boss.isEmpty() || rm == 0 || cancelled) {
                    activeBars.remove(task);

                    activeBars.remove(task);
                    boss.destroyWither();
                    cancel();

                    if (online != null) {
                        BarTask next = barQueue.poll();
                        if (next != null) {
                            next.getBar().sendTo(online, next.getOnTick());
                        }
                    }
                    return;
                }

                if (onTick != null) {
                    onTick.accept(player);
                }

                boss.setName(Colorize.colorize(content));
                applyProgress(player, boss);
            }
        }.runTaskTimer(KarmaPlugin.getInstance(), 20, 20);
    }

    /**
     * Update the boss bar text
     *
     * @param resetProgress if reset the boss bar progress
     * @return if the boss bar was able to be updated
     */
    @Override
    public boolean update(final boolean resetProgress) {
        if (cancelled) return false;

        boolean updated = false;

        if (this.barColor.equals(this.barColorCache) && this.barType.equals(barTypeCache) && this.barProgress.equals(this.barProgressCache) &&
                this.content.equals(this.contentCache) && this.progress == this.progressCache && contentEquals(flagSet, flagSetCache)) {
            return false; //Nothing to update
        }

        this.barColor = barColorCache;
        this.barType = barTypeCache;
        this.barProgress = barProgressCache;
        this.content = contentCache;
        this.progress = progressCache;
        this.flagSet.clear();
        this.flagSet.addAll(flagSetCache);

        for (UUID id : bosses.keySet()) {
            Player player = Bukkit.getPlayer(id);
            Boss boss = bosses.get(id);

            if (player == null || !player.isOnline()) {
                boss.destroyWither();
                bosses.remove(id);

                continue;
            }

            updated = true;

            boss.setName(Colorize.colorize(content));
            if (resetProgress) {
                remainingTime = displayTime;

                /*if (barProgress == BarProgress.STATIC_RANDOM || barProgress == BarProgress.RANDOM) {
                    progress = Math.random();
                }

                applyProgress(player, boss);*/
                boss.setHealth(progress);
            }

            if (boss instanceof ModernBoss) {
                ModernBoss modern = (ModernBoss) boss;
                BossBar bar = modern.getWither();

                for (BarFlag bFlag : flagSet) {
                    org.bukkit.boss.BarFlag flag = org.bukkit.boss.BarFlag.valueOf(bFlag.getBukkitName());
                    bar.addFlag(flag);
                }

                org.bukkit.boss.BarColor bukkitColor = org.bukkit.boss.BarColor.valueOf(barColor.name());
                bar.setColor(bukkitColor);

                BarStyle bukkitStyle = BarStyle.valueOf(barType.name());
                bar.setStyle(bukkitStyle);
            }
        }

        return updated;
    }

    private <T> boolean contentEquals(final Collection<T> collection1, final Collection<T> collection2) {
        if (collection1 == null && collection2 == null) return true;
        if (collection1 == null || collection2 == null) return false;

        for (T element : collection1) {
            if (!collection2.contains(element)) return false;
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private void applyProgress(final Player player, final Boss boss) {
        switch (barProgress) {
            case HEALTH_UP:
                double t = remainingTime;
                double diff = displayTime - t;

                progress = diff / displayTime;
                break;
            case HEALTH_DOWN:
                progress = (double) (remainingTime) / displayTime;
                break;
            case RANDOM:
                progress = Math.random() + 0.1;
                break;
            case PLAYER:
                double maxHealth = player.getMaxHealth();
                double health = player.getHealth();

                progress = health / maxHealth;
                break;
            case STATIC_RANDOM:
            case STATIC:
            default:
                break;
        }

        boss.setHealth(Math.max(0, Math.min(1.0, progress)));
    }

    /**
     * Check if the boss bar is valid
     *
     * @return if the boss bar entity is alive or is still running
     */
    @Override
    public boolean isValid() {
        return !bosses.isEmpty();
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
    public @NotNull String getContent() {
        return content;
    }

    /**
     * Get the boss bar color
     *
     * @return the color
     */
    @Override
    public BarColor getColor() {
        return barColor;
    }

    /**
     * Get the boss bar type
     *
     * @return the type
     */
    @Override
    public BarType getType() {
        return barType;
    }

    /**
     * Get the boss bar progress type
     *
     * @return the progress type
     */
    @Override
    public BarProgress getProgressType() {
        return barProgress;
    }

    /**
     * Get the boss bar progress
     *
     * @return the bar progress
     */
    @Override
    public double getProgress() {
        return progress;
    }

    /**
     * Set the boss bar flags
     *
     * @param flags the flags to add
     * @return the modified boss bar
     */
    @Override
    public BossBarProvider<Player> setFlags(final BarFlag... flags) {
        flagSetCache.addAll(Arrays.asList(flags));

        if (bosses.isEmpty()) super.setFlags(flags);
        return this;
    }

    /**
     * Remove the boss bar flags
     *
     * @param flags the flags to remove
     * @return the modified boss bar
     */
    @Override
    public BossBarProvider<Player> removeFlags(final BarFlag... flags) {
        Arrays.asList(flags).forEach(flagSetCache::remove);

        if (bosses.isEmpty()) super.removeFlags(flags);
        return this;
    }
}
