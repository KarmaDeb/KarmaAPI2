package es.karmadev.api.spigot.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;

/**
 * Represents a command that is a subcommand
 * of a parent command. In order for this to
 * fully work, a call to {@link SimpleCommand#addSubcommand(SubCommand)} must
 * be done
 */
@Getter
public abstract class SubCommand {

    protected final String argument;
    protected final int position;
    protected final int maxPosition;

    /**
     * Initialize the sub command
     *
     * @param argument the command argument name
     * @param position the command argument position
     */
    public SubCommand(final String argument, final int position) {
        this.argument = argument;
        this.position = position;
        this.maxPosition = Integer.MAX_VALUE;
    }

    /**
     * Initialize the sub command
     *
     * @param argument    the command argument name
     * @param position    the command argument position
     * @param maxPosition the command max argument position, this means that
     *                    the sub-command will be considered a sub-command of the
     *                    parent command ONLY when the number of arguments starting from
     *                    the sub-command position is less than or equal to this
     */
    public SubCommand(final String argument, final int position, final int maxPosition) {
        this.argument = argument;
        this.position = position;
        this.maxPosition = maxPosition;
    }

    /**
     * Run the command
     *run
     * @param sender   the command sender
     * @param argument the command argument
     * @param args     the arguments
     */
    protected abstract void run(final CommandSender sender, final String argument, final String[] args);
}
