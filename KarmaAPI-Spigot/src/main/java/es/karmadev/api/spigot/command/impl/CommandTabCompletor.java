package es.karmadev.api.spigot.command.impl;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Represents a command tab completor
 */
public interface CommandTabCompletor {

    /**
     * Perform a tab completion
     *
     * @param sender the command sender
     * @param alias  the command alias
     * @param args   the command arguments
     * @return the tab completions
     */
    List<String> tabComplete(CommandSender sender, String alias, String[] args);
}
