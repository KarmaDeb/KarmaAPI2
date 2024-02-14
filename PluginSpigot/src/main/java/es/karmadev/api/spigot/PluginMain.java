package es.karmadev.api.spigot;

import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.minecraft.BukkitVersion;
import es.karmadev.api.minecraft.bossbar.component.BarProgress;
import es.karmadev.api.minecraft.bossbar.component.BarType;
import es.karmadev.api.minecraft.component.Color;
import es.karmadev.api.minecraft.text.TextMessageType;
import es.karmadev.api.minecraft.text.component.Component;
import es.karmadev.api.minecraft.text.component.title.Times;
import es.karmadev.api.reflection.ReflectionApi;
import es.karmadev.api.minecraft.text.Colorize;
import es.karmadev.api.spigot.command.CommandBuilder;
import es.karmadev.api.spigot.command.impl.AbstractCommand;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.reflection.HologramManager;
import es.karmadev.api.spigot.reflection.bossbar.SpigotBossBar;
import es.karmadev.api.spigot.reflection.bossbar.nms.BossProvider;
import es.karmadev.api.spigot.reflection.packet.MessagePacket;
import es.karmadev.api.spigot.tracker.ConstantProperty;
import es.karmadev.api.spigot.tracker.stand.TrackerStand;
import es.karmadev.api.spigot.v1_8_R1.boss.V1_8_R1BossProvider;
import es.karmadev.api.spigot.v1_8_R1.hologram.V1_8_R1HologramManager;
import es.karmadev.api.spigot.v1_8_R2.boss.V1_8_R2BossProvider;
import es.karmadev.api.spigot.v1_8_R2.hologram.V1_8_R2HologramManager;
import es.karmadev.api.spigot.v1_8_R3.boss.V1_8_R3BossProvider;
import es.karmadev.api.spigot.v1_8_R3.hologram.V1_8_R3HologramManager;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.concurrent.atomic.AtomicInteger;

public class PluginMain extends KarmaPlugin {

    public PluginMain() throws IllegalAccessException {
        super(true, true);

        ConstantProperty.propagateInternals();
        TrackerStand.precacheAssignments();
    }

    /**
     * Enable the plugin
     */
    @Override
    public void enable() {
        ReflectionApi.init(this);

        BukkitVersion version = BukkitVersion.getCurrent();
        if (version != null) {
            HologramManager manager = null;

            switch (version.toEnum()) {
                case v1_8_R1:
                    BossProvider.setProvider(new V1_8_R1BossProvider());
                    manager = new V1_8_R1HologramManager(this);
                    break;
                case v1_8_R2:
                    BossProvider.setProvider(new V1_8_R2BossProvider());
                    manager = new V1_8_R2HologramManager(this);
                    break;
                case v1_8_R3:
                    BossProvider.setProvider(new V1_8_R3BossProvider());
                    manager = new V1_8_R3HologramManager(this);
                    break;
            }

            if (manager != null) {
                manager.register();
                logger().send(LogLevel.WARNING, "Initialized minecraft 1.8 {0} reflection", version.getReleaseVersion());
            }
        }

        Permission titlePermission = new Permission("karmaapi.title.test", PermissionDefault.OP);
        Permission actionbarPermission = new Permission("karmaapi.actionbar.test", PermissionDefault.OP);
        Permission bossPermission = new Permission("karmaapi.boss.test", PermissionDefault.OP);

        getServer().getPluginManager().addPermission(titlePermission);
        getServer().getPluginManager().addPermission(actionbarPermission);
        getServer().getPluginManager().addPermission(bossPermission);

        AbstractCommand testTitle = CommandBuilder.getBuilder()
                .name("test_title")
                .permission(titlePermission)
                .executor(Player.class).build();
        AbstractCommand testActionbar = CommandBuilder.getBuilder()
                .name("test_actionbar")
                .permission(actionbarPermission)
                .executor(Player.class).build();
        AbstractCommand testBossBar = CommandBuilder.getBuilder()
                .name("test_boss")
                .permission(bossPermission)
                .executor(Player.class).build();

        testTitle.setExecutor((sender, label, args) -> {
            assert sender instanceof Player;
            Player player = (Player) sender;

            MessagePacket titlePacket;
            if (args.length == 1 && args[0].equalsIgnoreCase("simple")) {
                Component[] title = Component.builder()
                        .color(Color.GREEN).title("This is a title")
                        .color(Color.DARK_GREEN).subtitle("This is a subtitle")
                        .build(TextMessageType.TIMES);

                titlePacket = new MessagePacket(title);
            } else {
                /*TimesMessage times = new TimesMessage(Times.exact(0), Times.exact(20), Times.exact(0));
                MessagePacket timesPacket = new MessagePacket(times);
                timesPacket.send(player);

                ComponentSequence titleAnimation = new AnimatedComponent(4, Times.exact(10), TextMessageType.TITLE);
                titleAnimation.addSequence(Colorize.colorize("&6<o/"));
                titleAnimation.addSequence(Colorize.colorize("&c\\o>"));
                titleAnimation.addSequence(Colorize.colorize("&6<o/"));
                titleAnimation.addSequence(Colorize.colorize("&c\\o>"));

                ComponentSequence subtitleAnimation = new AnimatedComponent(2, Times.exact(20), TextMessageType.SUBTITLE);
                subtitleAnimation.addSequence(Colorize.colorize("&8&l[&fYou&cTuber&8&l] &b" + player.getName()));
                subtitleAnimation.addSequence(Colorize.colorize("&8&l[&dStreamer&8&l] &b" + player.getName()));
                subtitleAnimation.addSequence(Colorize.colorize("&8&l[&6Social&8&l] &b" + player.getName()));
                subtitleAnimation.addSequence(Colorize.colorize("&8&l[&eCelebrity&8&l] &b" + player.getName()));

                titlePacket = new MessagePacket(titleAnimation);
                subtitlePacket = new MessagePacket(subtitleAnimation);*/

                Component[] components = Component.builder()
                        .fadeIn(Times.exact(0))
                        .show(Times.exact(20))
                        .fadeOut(Times.exact(0))
                        .sequenceStart(TextMessageType.TITLE)
                        .repeats(4)
                        .interval(Times.exact(10))
                        .component("&6<o/")
                        .component("&c\\o>")
                        .component("&6<o/")
                        .component("&c\\o>")
                        .sequenceEnd()
                        .sequenceStart(TextMessageType.SUBTITLE)
                        .repeats(2)
                        .interval(Times.exact(20))
                        .component("&8&l[&fYou&cTuber&8&l] &b" + player.getName())
                        .component("&8&l[&dStreamer&8&l] &b" + player.getName())
                        .component("&8&l[&6Social&8&l] &b" + player.getName())
                        .component("&8&l[&eCelebrity&8&l] &b" + player.getName())
                        .sequenceEnd()
                        .build();

                titlePacket = new MessagePacket(components);
            }

            titlePacket.send(player);
            player.sendMessage(Colorize.colorize("&aSending title and subtitle"));
        });
        testActionbar.setExecutor((sender, label, args) -> {
            assert sender instanceof Player;
            Player player = (Player) sender;

            MessagePacket packet;

            if (args.length == 1 && args[0].equalsIgnoreCase("simple")) {
                Component[] actionbar = Component.builder().actionbar("&aThis is an actionbar").build();
                packet = new MessagePacket(actionbar);
            } else {
                Component[] components = Component.builder()
                        .sequenceStart(TextMessageType.ACTIONBAR)
                        .repeats(1)
                        .interval(Times.exact(5))
                        .component("&0████", 4)
                        .component("&a█&0███", 4)
                        .component("&a██&0██", 4)
                        .component("&a███&0█", 4)
                        .component("&a████", 8)
                        .component("&a███&c█")
                        .component("&a██&c██")
                        .component("&a█&c███")
                        .component("&c████", 8)
                        .sequenceEnd().build();

                        /*
                ComponentSequence barSequence = new AnimatedComponent(1, Times.exact(10), TextMessageType.ACTIONBAR);
                barSequence.addSequence("&0████", 2);
                barSequence.addSequence("&a█&0███", 2);
                barSequence.addSequence("&a██&0██", 2);
                barSequence.addSequence("&a███&0█", 2);
                barSequence.addSequence("&a████", 4);
                barSequence.addSequence("&a███&c█");
                barSequence.addSequence("&a██&c██");
                barSequence.addSequence("&a█&c███");
                barSequence.addSequence("&c████", 4);*/

                packet = new MessagePacket(components);
            }

            packet.send(player);
            player.sendMessage(Colorize.colorize("&aSending actionbar"));
        });
        testBossBar.setExecutor((sender, label, args) -> {
            assert sender instanceof Player;
            Player player = (Player) sender;

            for (int i = 0; i < 10; i++) {
                int barId = i + 1;
                SpigotBossBar bossBar = new SpigotBossBar(String.format("&bBoss bar #%d (5 seconds left)", barId));
                bossBar.progress(BarProgress.HEALTH_DOWN);
                bossBar.type(BarType.SOLID);
                AtomicInteger time = new AtomicInteger(5);
                bossBar.displayTime(5);

                bossBar.send((client) -> {
                    bossBar.setContent(String.format("&bBoss bar #%d (%d seconds left)", barId, time.getAndDecrement()));
                    bossBar.update(false);
                }, player);
            }

            player.sendMessage(Colorize.colorize("&aSending boss bar"));
        });

        if (testTitle.register()) {
            logger().send(LogLevel.SUCCESS, "Successfully registered test_title command");
        } else {
            logger().send(LogLevel.WARNING, "Failed to register test_title command");
        }
        if (testActionbar.register()) {
            logger().send(LogLevel.SUCCESS, "Successfully registered test_actionbar command");
        } else {
            logger().send(LogLevel.WARNING, "Failed to register test_actionbar command");
        }
        if (testBossBar.register()) {
            logger().send(LogLevel.SUCCESS, "Successfully registered test_boss command");
        } else {
            logger().send(LogLevel.WARNING, "Failed to register test_boss command");
        }
    }

    /**
     * Disable the plugin
     */
    @Override
    public void disable() {

    }
}
