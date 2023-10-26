package es.karmadev.api.spigot.command.impl;

import org.bukkit.command.CommandSender;

/**
 * Represents a command executor
 */
public interface CommandExecutor {

    /**
     * Execute the command
     *
     * @param sender the command sender
     * @param label  the used label
     * @param args   the command arguments
     */
    void execute(CommandSender sender, String label, String[] args);
}
