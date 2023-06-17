package es.karmadev.api.minecraft.bossbar.component;

/**
 * Set the boss bar progress type
 */
public enum BarProgress {
    /**
     * Static boss bar health
     */
    STATIC,
    /**
     * Static random boss bar health
     */
    STATIC_RANDOM,
    /**
     * Player-health based boss bar health
     */
    PLAYER,
    /**
     * From 0 to 100 boss bar health
     */
    HEALTH_UP,
    /**
     * From 100 to 0 boss bar health
     */
    HEALTH_DOWN,
    /**
     * Random health
     */
    RANDOM
}
