package es.karmadev.api.minecraft.bossbar.component;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BossBar flag
 */
@AllArgsConstructor @Getter
public enum BarFlag {
    /**
     * Dark the player screen while the boss
     * bar is active
     */
    DARKEN_SCREEN("DARKEN_SKY"),
    /**
     * Play boss music while the boss bar is
     * active
     */
    PLAY_BOSS_MUSIC("PLAY_BOSS_MUSIC"),
    /**
     * Add fog to the world while the boss
     * bar is active
     */
    CREATE_WORLD_FOG("CREATE_FOG");

    private final String bukkitName;
}
