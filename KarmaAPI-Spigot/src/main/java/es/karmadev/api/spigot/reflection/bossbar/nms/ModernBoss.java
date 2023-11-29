package es.karmadev.api.spigot.reflection.bossbar.nms;

import es.karmadev.api.minecraft.text.Colorize;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

@Getter
public class ModernBoss implements Boss {

    private final BossBar wither;

    ModernBoss() {
        wither = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
    }

    /**
     * Add a player to the wither view
     *
     * @param player the player to add
     */
    @Override
    public void add(final Player player) {
        wither.addPlayer(player);
    }

    /**
     * Remove a player from the
     * wither view
     *
     * @param player the player
     */
    @Override
    public void remove(final Player player) {
        wither.removePlayer(player);
    }

    /**
     * Create the wither object
     *
     * @param position the wither position
     * @param name     the wither name
     */
    @Override
    public void createWither(final Location position, final String name) {
        wither.setTitle(Colorize.colorize(name));
        wither.setVisible(true);
    }

    /**
     * Set the wither name
     *
     * @param name the wither name
     */
    @Override
    public void setName(final String name) {
        wither.setTitle(Colorize.colorize(name));
    }

    /**
     * Destroy the wither
     */
    @Override
    public void destroyWither() {
        wither.removeAll();
        wither.setVisible(false);
    }

    /**
     * Teleport the wither
     *
     * @param position the new wither position
     */
    @Override
    public void teleport(final Location position) {

    }

    /**
     * Set the wither health
     *
     * @param healthScale the health
     */
    @Override
    public void setHealth(final double healthScale) {
        wither.setProgress(healthScale);
    }

    /**
     * Get if the wither has containers
     * or not
     *
     * @return if the wither has containers
     */
    @Override
    public boolean isEmpty() {
        return wither.getPlayers().isEmpty();
    }
}
