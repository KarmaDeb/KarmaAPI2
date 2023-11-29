package es.karmadev.api.spigot.reflection.bossbar.nms;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class ModernBarProvider extends BossProvider {

    private final Set<Boss> bosses = ConcurrentHashMap.newKeySet();

    /**
     * Create a new boss
     *
     * @return the boss
     */
    @Override
    public Boss getBoss() {
        ModernBoss boss = new ModernBoss();
        bosses.add(boss);

        return boss;
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
