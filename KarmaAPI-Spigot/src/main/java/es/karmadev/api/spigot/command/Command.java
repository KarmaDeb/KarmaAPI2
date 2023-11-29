package es.karmadev.api.spigot.command;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a bukkit command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Command {

    /**
     * The command name, always required
     *
     * @return the command name
     */
    String name();

    /**
     * The command description, shown in /help command
     *
     * @return the command description
     */
    String description() default "";

    /**
     * The command permission required to run the
     * command
     *
     * @return the permission required to run the command
     */
    String permission() default "";

    /**
     * The message to send when the
     * executor does not have the specified
     * permission
     *
     * @return the permission message
     */
    String permissionMessage() default "&cYou're in lack of permission&7 <permission>";

    /**
     * Set if the command execution requires
     * OP. THIS SETTING GETS OVERWRITE BY
     * {@link #permission()}
     *
     * @return if the command execution requires OP
     */
    boolean requiresOp() default false;

    /**
     * The command usage message
     *
     * @return the command usage message
     */
    String usage() default "";

    /**
     * The command aliases
     *
     * @return the command aliases
     */
    String[] aliases() default {};

    /**
     * The expected (allowed) executors. By default, all the
     * executors are allowed, but you can specify this to be
     * only "Player.class", so only players can run the command
     *
     * @return the allowed executors
     */
    Class<? extends CommandSender>[] executors() default {Player.class,
            ProxiedCommandSender.class, RemoteConsoleCommandSender.class,
            ConsoleCommandSender.class, BlockCommandSender.class};
}
