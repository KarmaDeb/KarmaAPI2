import es.karmadev.api.spigot.command.CommandBuilder;
import es.karmadev.api.spigot.command.impl.AbstractCommand;
import es.karmadev.api.spigot.command.impl.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Main {

    public static void main(String[] args) {
        AbstractCommand command = CommandBuilder.getBuilder()
                .name("help").build();

        command.setExecutor(new CommandExecutor() {
            @Override
            public void execute(CommandSender sender, String label, String[] args) {

            }
        });
        command.register();
        command.unregister();
    }
}
