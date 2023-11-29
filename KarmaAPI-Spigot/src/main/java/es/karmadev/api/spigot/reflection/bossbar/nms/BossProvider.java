package es.karmadev.api.spigot.reflection.bossbar.nms;

import lombok.Getter;
import lombok.Setter;

public abstract class BossProvider {

    @Getter @Setter
    private static BossProvider provider;

    /**
     * Create a new boss
     *
     * @return the boss
     */
    public abstract Boss getBoss();

    /**
     * Destroy a boss
     *
     * @param boss the boss to remove
     */
    public abstract void destroyBoss(final Boss boss);

    /**
     * Get all the bosses
     *
     * @return the bosses
     */
    public abstract Boss[] getBosses();
}
