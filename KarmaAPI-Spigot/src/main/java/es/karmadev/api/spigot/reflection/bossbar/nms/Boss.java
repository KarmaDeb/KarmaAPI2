package es.karmadev.api.spigot.reflection.bossbar.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * BossBar class wrapper
 */
public interface Boss {

    static Boss getBoss() {
        BossProvider provider = BossProvider.getProvider();
        if (provider == null) {
            provider = new ModernBarProvider();
            BossProvider.setProvider(provider);
        }

        return provider.getBoss();
    }

    /**
     * Add a player to the wither view
     *
     * @param player the player to add
     */
    void add(final Player player);

    /**
     * Remove a player from the
     * wither view
     *
     * @param player the player
     */
    void remove(final Player player);

    /**
     * Create the wither object
     *
     * @param position the wither position
     * @param name the wither name
     */
    void createWither(final Location position, final String name);

    /**
     * Set the wither name
     *
     * @param name the wither name
     */
    void setName(final String name);

    /**
     * Destroy the wither
     */
    void destroyWither();

    /**
     * Teleport the wither
     *
     * @param position the new wither position
     */
    void teleport(final Location position);

    /**
     * Set the wither health
     *
     * @param healthScale the health
     */
    void setHealth(final double healthScale);

    /**
     * Get if the wither has containers
     * or not
     *
     * @return if the wither has containers
     */
    boolean isEmpty();
}