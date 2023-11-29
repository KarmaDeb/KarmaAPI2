package es.karmadev.api.spigot.command;

import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.spigot.command.impl.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

/**
 * A utility crass to create
 * commands on the go. The operations performed
 * on {@link CommandBuilder#aliases} and {@link CommandBuilder#executors}
 * makes this class non-thread safe
 */
@NotThreadSafe @SuppressWarnings({"unused", "unchecked"})
public final class CommandBuilder {

    private String name;
    private String description;
    private String permission;
    @SuppressWarnings("FieldMayBeFinal")
    private String permissionMessage = "&cYou're in lack of permission&7 <permission>";
    private boolean requiresOp;
    private String usage;
    private final Set<CharSequence> aliases = new HashSet<>();
    private final Set<Class<? extends CommandSender>> executors = new HashSet<>();

    /**
     * Create a new command builder
     */
    private CommandBuilder() {}

    /**
     * Set the command name
     *
     * @param name the command name
     * @return the command builder
     */
    public CommandBuilder name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the command description
     *
     * @param description the command description
     * @return the command builder
     */
    public CommandBuilder description(final String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the command permission
     *
     * @param permission the command permission
     * @return the command builder
     */
    public CommandBuilder permission(final Permission permission) {
        if (permission == null) {
            this.permission = null;
        } else {
            this.permission = permission.getName();
        }

        return this;
    }

    /**
     * Set the command permission
     *
     * @param permission the command permission
     * @return the command builder
     */
    public CommandBuilder permission(final String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Set the command permission message
     *
     * @param message the message
     * @return the command builder
     */
    public CommandBuilder permissionMessage(final String message) {
        this.permissionMessage = message;
        return this;
    }

    /**
     * Set if the command requires OP
     *
     * @param status the op status
     * @return the command builder
     */
    public CommandBuilder requiresOp(final boolean status) {
        this.requiresOp = status;
        return this;
    }

    /**
     * Set the command usage message
     *
     * @param usage the usage message
     * @return the command builder
     */
    public CommandBuilder usage(final String usage) {
        this.usage = usage;
        return this;
    }

    /**
     * Add an alias for the command
     *
     * @param alias the alias to add
     * @return the command builder
     */
    public <T extends CharSequence> CommandBuilder alias(final T alias) {
        if (alias != null) {
            aliases.add(alias);
        }

        return this;
    }

    /**
     * Add the aliases to the command
     *
     * @param aliases the aliases to add
     * @return the command builder
     */
    public <T extends CharSequence> CommandBuilder aliases(final Collection<T> aliases) {
        if (aliases.isEmpty()) return this;

        Collection<T> nonNullAliases = new ArrayList<>();
        for (T sequence : aliases) {
            if (ObjectUtils.isNullOrEmpty(sequence)) continue;
            nonNullAliases.add(sequence);
        }

        this.aliases.addAll(nonNullAliases);
        return this;
    }

    /**
     * Add the aliases to the command aliases
     *
     * @param aliases the aliases to add
     * @return the command builder
     * @param <T> the alias string type
     */
    @SafeVarargs
    public final <T extends CharSequence> CommandBuilder aliases(final T... aliases) {
        return aliases(Arrays.asList(aliases));
    }

    /**
     * Add an allowed executor to the command
     *
     * @param executor the executor to add
     * @return the command builder
     */
    public CommandBuilder executor(final Class<? extends CommandSender> executor) {
        if (executor == null) return this;

        executors.add(executor);
        return this;
    }

    /**
     * Add the allowed executors to the command
     *
     * @param executors the executors to add
     * @return the command builder
     */
    public CommandBuilder executors(final Collection<Class<? extends CommandSender>> executors) {
        if (executors.isEmpty()) return this;

        Collection<Class<? extends CommandSender>> nonNullExecutors = new ArrayList<>();
        for (Class<? extends CommandSender> executor : executors) {
            if (ObjectUtils.isNullOrEmpty(executor)) continue;
            nonNullExecutors.add(executor);
        }

        this.executors.addAll(nonNullExecutors);
        return this;
    }

    /**
     * Add the allowed executors to the command
     *
     * @param executors the executors to add
     * @return the command builder
     */
    @SafeVarargs
    public final CommandBuilder executors(final Class<? extends CommandSender>... executors) {
        return executors(Arrays.asList(executors));
    }

    /**
     * Build a command out of the
     * provided arguments
     *
     * @return the command
     * @throws IllegalStateException if the command name is not valid or the provided
     * executor is not valid
     */
    public AbstractCommand build() throws IllegalStateException {
        if (ObjectUtils.isNullOrEmpty(name))
            throw new IllegalStateException("Cannot create a simple command with an empty command name");

        String description = (this.description == null ? "": this.description);
        String permission = (this.permission == null ? "": this.permission);
        String usage = (this.usage == null ? "": this.usage);

        CharSequence[] tmpAliases = this.aliases.toArray(new CharSequence[0]);
        String[] aliases = new String[tmpAliases.length];
        for (int i = 0; i < tmpAliases.length; i++) aliases[i] = tmpAliases[i].toString();

        Class<? extends CommandSender>[] executors = this.executors.toArray(new Class[0]);
        return new AbstractCommand(name, description, permission, permissionMessage, requiresOp, usage, aliases, executors);
    }

    /**
     * Get a new command builder
     *
     * @return a command builder
     */
    public static CommandBuilder getBuilder() {
        return new CommandBuilder();
    }
}
