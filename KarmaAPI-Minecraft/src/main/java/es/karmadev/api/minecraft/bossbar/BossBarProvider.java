package es.karmadev.api.minecraft.bossbar;

import es.karmadev.api.minecraft.bossbar.component.BarColor;
import es.karmadev.api.minecraft.bossbar.component.BarFlag;
import es.karmadev.api.minecraft.bossbar.component.BarProgress;
import es.karmadev.api.minecraft.bossbar.component.BarType;

import java.util.BitSet;
import java.util.Collection;

/**
 * BossBar message provider
 * @param <Player> the player type
 */
@SuppressWarnings("unused")
public abstract class BossBarProvider<Player> {

    protected final BitSet flagSet = new BitSet(3);

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
    public abstract BossBarProvider<Player> color(final BarColor color) throws UnsupportedOperationException;

    /**
     * Set the boss bar type
     *
     * @param type the type
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     * type change
     */
    public abstract BossBarProvider<Player> type(final BarType type) throws UnsupportedOperationException;

    /**
     * Set the boss bar progress type
     *
     * @param progress the progress type
     * @return the modified boss bar
     * @throws UnsupportedOperationException if the boss bar doesn't support
     * progress change
     */
    public abstract BossBarProvider<Player> progress(final BarProgress progress) throws UnsupportedOperationException;

    /**
     * Set the boss bar flags
     *
     * @param flags the flags to add
     * @return the modified boss bar
     */
    public final BossBarProvider<Player> setFlags(final BarFlag... flags) {
        for (BarFlag flag : flags) {
            flagSet.set(flag.ordinal(), true);
        }

        return this;
    }

    /**
     * Remove the boss bar flags
     *
     * @param flags the flags to remove
     * @return the modified boss bar
     */
    public final BossBarProvider<Player> removeFlags(final BarFlag... flags) {
        for (BarFlag flag : flags) {
            flagSet.set(flag.ordinal(), false);
        }

        return this;
    }

    /**
     * Set the boss bar display time
     *
     * @param time the new display time
     * @return the modified boss bar
     */
    public abstract BossBarProvider<Player> displayTime(final double time);

    /**
     * Set the boss bar progress manually
     *
     * @param progress the boss bar progress
     */
    public abstract void setProgress(final double progress);

    /**
     * Cancel the boss bar message
     */
    public abstract void cancel();

    /**
     * Send the boss bar to the players
     *
     * @param players the players to send the boss bar
     *                to
     */
    public void send(final Collection<Player> players) {
        for (Player player : players) send(player);
    }

    /**
     * Send the boss bar to the players
     *
     * @param players the players to send the boss bar
     *                to
     */
    public void send(final Player[] players) {
        for (Player player : players) send(player);
    }

    /**
     * Send the boss bar to the player
     *
     * @param player the player to send the actionbar to
     */
    public abstract void send(final Player player);

    /**
     * Update the boss bar text
     *
     * @param newMessage the new boss bar message
     * @param resetProgress if reset the boss bar progress
     * @return if the boss bar was able to be updated
     */
    public abstract boolean update(final String newMessage, final boolean resetProgress);

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
    public abstract BarFlag[] getFlags();

    /**
     * Check if the boss bar has the specified
     * flag
     *
     * @param flag the flag
     * @return if the boss bar has the flag
     */
    public boolean hasFlag(final BarFlag flag) {
        return flagSet.get(flag.ordinal());
    }
}
