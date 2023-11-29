package es.karmadev.api.minecraft.bossbar;

import es.karmadev.api.minecraft.bossbar.component.BarColor;
import es.karmadev.api.minecraft.bossbar.component.BarFlag;
import es.karmadev.api.minecraft.bossbar.component.BarProgress;
import es.karmadev.api.minecraft.bossbar.component.BarType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

/**
 * BossBar message provider
 */
@SuppressWarnings("unused")
public abstract class BossBarProvider<BarHolder> {

    protected final EnumSet<BarFlag> flagSet = EnumSet.noneOf(BarFlag.class);

    /**
     * Get the boss bar ID
     *
     * @return the boss bar ID
     */
    public abstract int getId();

    /**
     * Set the boss bar color
     *
     * @param color the color
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     * color change
     */
    public abstract BossBarProvider<BarHolder> color(final BarColor color) throws UnsupportedOperationException;

    /**
     * Set the boss bar type
     *
     * @param type the type
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     * type change
     */
    public abstract BossBarProvider<BarHolder> type(final BarType type) throws UnsupportedOperationException;

    /**
     * Set the boss bar progress type
     *
     * @param progress the progress type
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     * progress change
     */
    public abstract BossBarProvider<BarHolder> progress(final BarProgress progress) throws UnsupportedOperationException;

    /**
     * Set the boss bar flags
     *
     * @param flags the flags to add
     * @return the modified boss bar
     */
    public BossBarProvider<BarHolder> setFlags(final BarFlag... flags) {
        Collections.addAll(flagSet, flags);
        return this;
    }

    /**
     * Remove the boss bar flags
     *
     * @param flags the flags to remove
     * @return the modified boss bar
     */
    public BossBarProvider<BarHolder> removeFlags(final BarFlag... flags) {
        Arrays.asList(flags).forEach(flagSet::remove);
        return this;
    }

    /**
     * Set the boss bar display time
     *
     * @param time the new display time
     * @return the modified boss bar
     */
    public abstract BossBarProvider<BarHolder> displayTime(final long time);

    /**
     * Set the boss bar progress manually
     *
     * @param progress the boss bar progress
     */
    public abstract void setProgress(final double progress);

    /**
     * Set the boss bar message
     *
     * @param content the message
     */
    public abstract void setContent(final String content);

    /**
     * Cancel the boss bar message
     */
    public abstract void cancel();

    /**
     * Send the boss bar to the players
     *
     * @param onTick the action to perform on each tick
     * @param players the players to send the boss bar
     *                to
     */
    public abstract void send(final Consumer<BarHolder> onTick, final Collection<BarHolder> players);

    /**
     * Send the boss bar to the player
     *
     * @param players the player to send the actionbar to
     * @param onTick the action to perform on each tick
     */
    public void send(final Consumer<BarHolder> onTick, final BarHolder... players) {
        send(onTick, Arrays.asList(players));
    }

    /**
     * Send the boss bar to the players
     *
     * @param players the players to send the boss bar
     *                to
     */
    public void send(final Collection<BarHolder> players) {
        send((player) -> {}, players);
    }

    /**
     * Send the boss bar to the player
     *
     * @param players the player to send the actionbar to
     */
    public void send(final BarHolder... players) {
        send((player) -> {}, Arrays.asList(players));
    }

    /**
     * Update the boss bar
     *
     * @param resetProgress if reset the boss bar progress
     * @return if the boss bar was able to be updated
     */
    public abstract boolean update(final boolean resetProgress);

    /**
     * Check if the boss bar is valid
     *
     * @return if the boss bar entity is alive or is still running
     */
    public abstract boolean isValid();

    /**
     * Check if the boss bar has been cancelled
     *
     * @return if the boss bar entity is alive and/or running but is
     * cancelled
     */
    public abstract boolean isCancelled();

    /**
     * Get the boss bar content
     *
     * @return the content
     */
    public abstract String getContent();

    /**
     * Get the boss bar color
     *
     * @return the color
     */
    public abstract BarColor getColor();

    /**
     * Get the boss bar type
     *
     * @return the type
     */
    public abstract BarType getType();

    /**
     * Get the boss bar progress type
     *
     * @return the progress type
     */
    public abstract BarProgress getProgressType();

    /**
     * Get the boss bar progress
     *
     * @return the bar progress
     */
    public abstract double getProgress();

    /**
     * Get the boss bar flags
     *
     * @return the bar flags
     */
    public BarFlag[] getFlags() {
        return flagSet.toArray(new BarFlag[0]).clone();
    }

    /**
     * Check if the boss bar has the specified
     * flag
     *
     * @param flag the flag
     * @return if the boss bar has the flag
     */
    public boolean hasFlag(final BarFlag flag) {
        return flagSet.contains(flag);
    }
}
