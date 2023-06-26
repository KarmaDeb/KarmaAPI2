package es.karmadev.api.spigot.entity.global;

import es.karmadev.api.minecraft.client.GlobalPlayer;
import es.karmadev.api.minecraft.client.factory.PlayerContext;
import es.karmadev.api.minecraft.client.factory.PlayerFactory;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.task.completable.TaskCompletor;
import es.karmadev.api.schedule.task.completable.late.LateTask;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Bukkit player factory
 */
@SuppressWarnings("unused")
public class BukkitPlayerFactory implements PlayerFactory {

    /**
     * Create a new global player
     *
     * @param context the player context
     * @return the new global player
     */
    @Override
    public GlobalPlayer create(final PlayerContext context) {
        String username = context.getUsername();
        String nickname = context.getNickname();
        UUID uuid = context.getUUID();
        String world = context.getWorld();

        if (ObjectUtils.areNullOrEmpty(false, username, nickname, uuid, world)) return null;
        double x = context.getX();
        double y = context.getY();
        double z = context.getZ();
        float yaw = context.getYaw();
        float pitch = context.getPitch();

        if (ObjectUtils.equalsMultiple(Double.MIN_VALUE, x, y, z) || ObjectUtils.equalsMultiple(Float.MIN_VALUE, yaw, pitch)) return null;
        return new BukkitPlayer(username, nickname, uuid, world, x, y, z, yaw, pitch);
    }

    /**
     * Create a new global player asynchronously
     *
     * @param uncompletedContext the uncompleted player
     *                           context task
     * @return the new global player
     */
    @Override
    public TaskCompletor<GlobalPlayer> createAsync(final TaskCompletor<PlayerContext> uncompletedContext) {
        TaskCompletor<GlobalPlayer> completor = new LateTask<>();

        uncompletedContext.onComplete((task) -> {
            Throwable error = task.error();
            if (error != null) {
                completor.complete(null, error);
                return;
            }

            GlobalPlayer instance = create(task.get());
            completor.complete(instance);
        });

        return completor;
    }

    /**
     * Create a global player from the player object
     *
     * @param player the player
     * @return the global player
     */
    public static GlobalPlayer fromPlayer(final Player player) {
        return new BukkitPlayer(player);
    }
}
