package es.karmadev.api.spigot.command;

import es.karmadev.api.strings.StringOptions;
import es.karmadev.api.strings.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An utility class for managing commands
 */
public final class CommandUtil {

    private static SimpleCommandMap commandMap;
    private final static Set<SimpleCommand> commands = ConcurrentHashMap.newKeySet();
    private final static Map<SimpleCommand, String> prefixes = new ConcurrentHashMap<>();

    static {
        try {
            Server server = Bukkit.getServer();
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandUtil.commandMap = (SimpleCommandMap) commandMapField.get(server);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Register a command
     *
     * @param prefix the command prefix
     * @param command the command to register
     */
    public static boolean register(final String prefix, final SimpleCommand command) {
        if (commandMap.register(command.name, prefix, command.toCommand())) {
            if (commands.add(command)) {
                prefixes.put(command, prefix);
                return true;
            }
        }

        return false;
    }

    /**
     * Register a command by using the class
     * instance instead. The call to this method
     * requires the command class to be annotated by
     * {@link Command}
     *
     * @param prefix the command prefix
     * @param command the command class to register
     * @throws RuntimeException if the reflection fails
     */
    public static boolean register(final String prefix, final Class<? extends SimpleCommand> command) throws RuntimeException {
        try {
            Constructor<? extends SimpleCommand> constructor = command.getConstructor();
            SimpleCommand instance = constructor.newInstance();

            return register(prefix, instance);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Register a command
     *
     * @param command the command to register
     */
    public static boolean register(final SimpleCommand command) {
        String prefix = StringUtils.generateString(5, StringOptions.NUMBERS, StringOptions.LOWERCASE);

        if (commandMap.register(command.name, prefix, command.toCommand())) {
            if (commands.add(command)) {
                prefixes.put(command, prefix);
                return true;
            }
        }

        return false;
    }

    /**
     * Register a command by using the class
     * instance instead. The call to this method
     * requires the command class to be annotated by
     * {@link Command}
     *
     * @param command the command class to register
     * @throws RuntimeException if the reflection fails
     */
    public static boolean register(final Class<? extends SimpleCommand> command) throws RuntimeException {
        String prefix = StringUtils.generateString(5, StringOptions.NUMBERS, StringOptions.LOWERCASE);

        try {
            Constructor<? extends SimpleCommand> constructor = command.getConstructor();
            SimpleCommand instance = constructor.newInstance();

            return register(prefix, instance);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Unregister a command
     *
     * @param command the command to unregister
     */
    public static void unregister(final SimpleCommand command) {
        if (commands.remove(command)) {
            Map<String, org.bukkit.command.Command> knownCommands = getKnownCommands();
            for (String alias : command.aliases) {
                knownCommands.remove(alias);
            }

            String prefix = prefixes.get(command);
            knownCommands.remove(prefix + ":" + command.name);

            if (commands.stream().noneMatch((cmd) -> cmd.name.equalsIgnoreCase(command.name))) {
                knownCommands.remove(command.name);
            }
        }
    }

    /**
     * Unregister all the instances of command
     * made by the class provided
     *
     * @param command the command class to unregister
     */
    public static void unregister(final Class<? extends SimpleCommand> command) {
        for (SimpleCommand cmd : commands) {
            if (cmd.getClass().equals(command)) {
                unregister(cmd);
            }
        }
    }

    /**
     * Get all the registered commands
     *
     * @return the registered commands
     */
    public static Set<SimpleCommand> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * Get the prefix being used by a command
     *
     * @param command the command
     * @return the prefix
     */
    public static String getPrefix(final SimpleCommand command) {
        return prefixes.get(command);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, org.bukkit.command.Command> getKnownCommands() {
        try {
            Field knownCommands = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommands.setAccessible(true);

            return (Map<String, org.bukkit.command.Command>) knownCommands.get(commandMap);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
