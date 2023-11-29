package es.karmadev.api.spigot.v1_8_R2.boss;

import es.karmadev.api.spigot.reflection.bossbar.nms.Boss;
import es.karmadev.api.spigot.reflection.bossbar.nms.BossProvider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class V1_8_R2BossProvider extends BossProvider {

    private final Set<Boss> bosses = ConcurrentHashMap.newKeySet();

    /**
     * Create a new boss
     *
     * @return the boss
     */
    @Override
    public Boss getBoss() {
        Boss b = new NMSBoss();
        bosses.add(b);

        return b;
    }

    /**
     * Destroy a boss
     *
     * @param boss the boss to remove
     */
    @Override
    public void destroyBoss(final Boss boss) {
        if (bosses.remove(boss)) {
            boss.destroyWither();
        }
    }

    /**
     * Get all the bosses
     *
     * @return the bosses
     */
    @Override
    public Boss[] getBosses() {
        return bosses.toArray(new Boss[0]);
    }
}
