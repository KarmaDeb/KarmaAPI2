package es.karmadev.api.spigot;

import com.github.yeetmanlord.reflection_api.ReflectionApi;
import es.karmadev.api.minecraft.color.ColorComponent;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.minecraft.text.Component;
import es.karmadev.api.minecraft.text.component.AnimationComponent;
import es.karmadev.api.spigot.reflection.title.SpigotTitle;
import es.karmadev.api.spigot.tracker.ConstantProperty;
import es.karmadev.api.spigot.tracker.stand.TrackerStand;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PluginMain extends KarmaPlugin {

    public PluginMain() throws AlreadyRegisteredException, NoSuchFieldException, IllegalAccessException {
        super(true, true);

        ConstantProperty.propagateInternals();
        TrackerStand.precacheAssignments();
    }

    /**
     * Enable the plugin
     */
    @Override
    public void enable() {
        //SpigotServer.startTickCount(this);
        ReflectionApi.init(this);

        PluginCommand titleTest = getCommand("title_test");
        if (titleTest != null) {
            titleTest.setExecutor((sender, command, label, args) -> {
                if (!sender.isOp()) return false;
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    AnimationComponent titleAnimation = Component.animation()
                            .append(ColorComponent.parse("&6<o/"))
                            .append(ColorComponent.parse("&c\\o>"))
                            .append(ColorComponent.parse("&6<o/"))
                            .append(ColorComponent.parse("&c\\o>"))
                            .build(12, 10);
                    AnimationComponent subtitleAnimation = Component.animation()
                            .append(ColorComponent.parse("&8&l[&fYou&cTuber&8&l] &b" + player.getName()))
                            .append(ColorComponent.parse("&8&l[&dStreamer&8&l] &b" + player.getName()))
                            .append(ColorComponent.parse("&8&l[&6Social&8&l] &b" + player.getName()))
                            .append(ColorComponent.parse("&8&l[&eCelebrity&8&l] &b" + player.getName()))
                            .build(6, 20);


                    SpigotTitle title = new SpigotTitle(titleAnimation, subtitleAnimation);
                    title.send(player, 0, 40, 0);
                }

                return false;
            });
        }
    }

    /**
     * Disable the plugin
     */
    @Override
    public void disable() {

    }
}
