package es.karmadev.api.spigot.command.impl;

import es.karmadev.api.spigot.command.Command;
import es.karmadev.api.spigot.command.SimpleCommand;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Represents a command that has no default
 * execution implementation, but instead can be
 * set on the go
 */
@Getter @Setter
public final class AbstractCommand extends SimpleCommand {

    private CommandExecutor executor;
    private CommandExecutor invalidExecutor;
    private CommandTabCompletor tabCompletor;

    /**
     * Create a new simple command, reading the
     * parameters of the {@link Command} annotation. If no
     * annotation present, an {@link IllegalStateException} will
     * be thrown
     */
    private AbstractCommand() {
        super(); //Not compatible with abstract command
    }

    /**
     * Create a new simple command out of the
     * provided parameters. Not recommended
     * unless strictly necessary, it's better and easier
     * to use the {@link Command} annotation
     *
     * @param name             the command name
     * @param description      the command description
     * @param permission       the command permission
     * @param usage            the command usage message
     * @param aliases          the command aliases
     * @param allowedExecutors the command allowed executors
     */
    public AbstractCommand(final String name, final String description, final String permission,
                           final String usage, final String[] aliases, final Class<? extends CommandSender>[] allowedExecutors) {
        super(name, description, permission, usage, aliases, allowedExecutors);
    }

    /**
     * Create a new simple command out of the
     * provided parameters. Not recommended
     * unless strictly necessary, it's better and easier
     * to use the {@link Command} annotation
     *
     * @param name the command name
     */
    public AbstractCommand(final String name) {
        super(name);
    }

    /**
     * Execute the command
     *
     * @param sender the command sender
     * @param label  the command label
     * @param args   the command arguments
     */
    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (executor != null) executor.execute(sender, label, args);
    }

    /**
     * Execute the command when the executor is
     * not an allowed executor
     *
     * @param sender the command sender
     * @param label  the command label
     * @param args   the command arguments
     */
    @Override
    protected void executeInvalid(final CommandSender sender, final String label, final String[] args) {
        if (invalidExecutor != null) invalidExecutor.execute(sender, label, args);
    }

    /**
     * Handle a tab completion
     *
     * @param sender the command sender
     * @param alias  the alias being used
     * @param args   the command arguments
     * @return the tab completions
     */
    @Override
    protected List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        if (tabCompletor != null) return tabCompletor.tabComplete(sender, alias, args);
        return Collections.emptyList();
    }
}
