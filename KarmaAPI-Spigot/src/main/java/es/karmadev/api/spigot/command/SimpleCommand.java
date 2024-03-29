package es.karmadev.api.spigot.command;

import es.karmadev.api.array.ArrayUtils;
import es.karmadev.api.minecraft.text.Colorize;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.strings.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a bukkit command, with all
 * the basic things a command have.
 */
@SuppressWarnings("unchecked")
public abstract class SimpleCommand {

    protected final String name;
    protected final String description;
    protected final String permission;
    protected final String permissionMessage;
    protected final boolean requiresOp;
    protected final String usage;
    protected final String[] aliases;
    protected final Class<? extends CommandSender>[] allowedExecutors;
    protected final List<SubCommand> subCommands = new ArrayList<>();

    private final InternalCommand cmd;

    /**
     * Create a new simple command, reading the
     * parameters of the {@link Command} annotation. If no
     * annotation present, an {@link IllegalStateException} will
     * be thrown
     */
    public SimpleCommand() {
        Class<? extends SimpleCommand> currentClass = getClass();
        if (!currentClass.isAnnotationPresent(Command.class)) {
            throw new IllegalStateException("Cannot create a SimpleCommand without a @Command annotation.");
        }

        Command settings = currentClass.getAnnotation(Command.class);
        this.name = settings.name();
        this.description = settings.description();
        this.permission = settings.permission();
        this.permissionMessage = settings.permissionMessage();
        this.requiresOp = ObjectUtils.isNullOrEmpty(settings.permission()) && settings.requiresOp();
        this.usage = settings.usage();
        this.aliases = settings.aliases();
        this.allowedExecutors = settings.executors();

        cmd = new InternalCommand(this);
    }

    /**
     * Create a new simple command out of the
     * provided parameters. Not recommended
     * unless strictly necessary, it's better and easier
     * to use the {@link Command} annotation
     *
     * @param name the command name
     * @param description the command description
     * @param permission the command permission
     * @param permissionMessage the command permission message
     * @param requiresOp whether the command requires op
     * @param usage the command usage message
     * @param aliases the command aliases
     * @param allowedExecutors the command allowed executors
     */
    public SimpleCommand(final String name, final String description, final String permission,
                         final String permissionMessage, final boolean requiresOp,
                         final String usage, final String[] aliases,
                         final Class<? extends CommandSender>[] allowedExecutors) {
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.requiresOp = ObjectUtils.isNullOrEmpty(permission) && requiresOp;
        this.usage = usage;
        this.aliases = aliases;
        this.allowedExecutors = allowedExecutors;

        this.cmd = new InternalCommand(this);
    }

    /**
     * Create a new simple command out of the
     * provided parameters. Not recommended
     * unless strictly necessary, it's better and easier
     * to use the {@link Command} annotation
     *
     * @param name the command name
     */
    public SimpleCommand(final String name) {
        this(name, "", "", "&cYou're in lack of permission&7 <permission>", false, "", new String[]{}, new Class[]{
                Player.class,
                ProxiedCommandSender.class, RemoteConsoleCommandSender.class,
                ConsoleCommandSender.class, BlockCommandSender.class
        });
    }

    /**
     * Execute the command
     *
     * @param sender the command sender
     * @param label  the command label
     * @param args   the command arguments
     */
    protected abstract void execute(final CommandSender sender, final String label, final String[] args);

    /**
     * Execute the command when the executor is
     * not an allowed executor
     *
     * @param sender the command sender
     * @param label  the command label
     * @param args   the command arguments
     */
    protected void executeInvalid(final CommandSender sender, final String label, final String[] args) {}

    /**
     * Handle a tab completion
     *
     * @param sender the command sender
     * @param alias  the alias being used
     * @param args   the command arguments
     * @return the tab completions
     */
    protected List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        return Collections.emptyList();
    }

    /**
     * Add a sub-command to the command handler
     *
     * @param subCommand the sub command
     */
    public final void addSubcommand(final SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    /**
     * Get the bukkit command instance
     * of this command
     *
     * @return the bukkit command
     */
    public final org.bukkit.command.Command toCommand() {
        return cmd;
    }

    /**
     * Register this command
     * @return if the command was able to be registered
     */
    public boolean register() {
        return CommandUtil.register(this);
    }

    /**
     * Unregister this command
     */
    public void unregister() {
        CommandUtil.unregister(this);
    }

    private final static class InternalCommand extends org.bukkit.command.Command {

        private final SimpleCommand command;

        private InternalCommand(final SimpleCommand command) {
            super(command.name, command.description, command.usage, Arrays.asList(command.aliases));
            this.command = command;
        }

        /**
         * Executes the command, returning its success
         *
         * @param sender       Source object which is executing this command
         * @param commandLabel The alias of the command used
         * @param args         All arguments passed to the command, split via ' '
         * @return true if the command was successful, otherwise false
         */
        @Override
        public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final @NotNull String[] args) {
            Class<?> senderClass = sender.getClass();
            if (Arrays.stream(command.allowedExecutors).noneMatch((allowed) ->
                    allowed.isInstance(sender) || senderClass.isAssignableFrom(allowed) || allowed.isAssignableFrom(senderClass))) {
                command.executeInvalid(sender, commandLabel, args);
                return false;
            }

            String permission = command.permission;
            if (command.requiresOp) {
                permission = StringUtils.generateString();
            }

            if (!ObjectUtils.isNullOrEmpty(permission)) {
                Permission bukkitPermission = Bukkit.getPluginManager().getPermission(permission);
                if (bukkitPermission == null) {
                    bukkitPermission = new Permission(permission, PermissionDefault.OP);
                }

                boolean hasPermission = sender.hasPermission(bukkitPermission) ||
                        (bukkitPermission.getDefault().equals(PermissionDefault.OP) && sender.isOp());

                if (hasPermission) {
                    run(sender, commandLabel, args);
                    return true;
                } else if (!ObjectUtils.isNullOrEmpty(command.permissionMessage)) {
                    sender.sendMessage(Colorize.colorize(command.permissionMessage.replace("<permission>", (command.requiresOp ?
                            "op" : permission))));
                }

                return false;
            }

            run(sender, commandLabel, args);
            return true;
        }

        /**
         * Run the command
         *
         * @param sender the command sender
         * @param label  the command label
         * @param args   the command arguments
         */
        private void run(final CommandSender sender, final String label, final String[] args) {
            if (args.length == 0) {
                command.execute(sender, label, args);
                return;
            }

            int max = args.length - 1;
            for (int ps = 0; ps < args.length; ps++) {
                int position = ps;
                int diff = max - position;
                String argument = args[position];

                SubCommand sub = command.subCommands.stream().filter((cmd) ->
                        (cmd.argument == null || cmd.argument.equalsIgnoreCase(argument)) && (cmd.position == position || cmd.position < 0)
                                && cmd.maxPosition < diff).findFirst().orElse(null);
                if (sub != null) {
                    String[] newArgs = new String[diff];
                    int vIndex = 0;
                    for (int i = position + 1; i < args.length; i++) {
                        newArgs[vIndex++] = args[i];
                    }

                    sub.run(sender, argument, newArgs.clone());
                    return;
                }
            }

            command.execute(sender, label, args);
        }

        /**
         * Executed on tab completion for this command, returning a list of
         * options the player can tab through.
         *
         * @param sender Source object which is executing this command
         * @param alias  the alias being used
         * @param args   All arguments passed to the command, split via ' '
         * @return a list of tab-completions for the specified arguments. This
         * will never be null. List may be immutable.
         * @throws IllegalArgumentException if sender, alias, or args is null
         */
        @NotNull
        @Override
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
            if (!ArrayUtils.containsAny(command.allowedExecutors, sender.getClass())) {
                return Collections.emptyList();
            }

            String permission = command.permission;
            if (!ObjectUtils.isNullOrEmpty(permission)) {
                if (sender.hasPermission(permission)) {
                    return command.tabComplete(sender, alias, args);
                }

                return Collections.emptyList();
            }

            return command.tabComplete(sender, alias, args);
        }
    }
}