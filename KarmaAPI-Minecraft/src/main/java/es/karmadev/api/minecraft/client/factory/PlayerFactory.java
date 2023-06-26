package es.karmadev.api.minecraft.client.factory;

import es.karmadev.api.minecraft.client.GlobalPlayer;
import es.karmadev.api.schedule.task.completable.TaskCompletor;

/**
 * Player factory
 */
@SuppressWarnings("unused")
public interface PlayerFactory {

    /**
     * Create a new global player
     *
     * @param context the player context
     * @return the new global player
     */
    GlobalPlayer create(final PlayerContext context);

    /**
     * Create a new global player asynchronously
     *
     * @param uncompletedContext the uncompleted player
     *                           context task
     * @return the new global player
     */
    TaskCompletor<GlobalPlayer> createAsync(final TaskCompletor<PlayerContext> uncompletedContext);
}
