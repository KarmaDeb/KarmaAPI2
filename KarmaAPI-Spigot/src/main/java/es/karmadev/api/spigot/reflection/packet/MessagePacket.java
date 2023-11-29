package es.karmadev.api.spigot.reflection.packet;

import es.karmadev.api.minecraft.MinecraftVersion;
import es.karmadev.api.minecraft.text.Colorize;
import es.karmadev.api.minecraft.text.TextMessageType;
import es.karmadev.api.minecraft.text.component.Component;
import es.karmadev.api.minecraft.text.component.ComponentSequence;
import es.karmadev.api.minecraft.text.component.message.header.TimesMessage;
import es.karmadev.api.minecraft.text.component.text.ChatComponent;
import es.karmadev.api.minecraft.text.component.text.MessageComponent;
import es.karmadev.api.minecraft.text.component.text.click.ComponentClick;
import es.karmadev.api.minecraft.text.component.text.hover.ComponentHover;
import es.karmadev.api.minecraft.text.component.title.Times;
import es.karmadev.api.minecraft.text.component.title.TimesComponent;
import es.karmadev.api.spigot.reflection.ReflectionUtil;
import es.karmadev.api.spigot.reflection.SpigotPacket;
import es.karmadev.api.spigot.server.SpigotServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a message packet
 */
public class MessagePacket implements SpigotPacket {

    private final static Pattern hexPattern = Pattern.compile("§#\\((?<color>\\w*)\\)");

    private final static Class<?> chatBaseComponent = SpigotServer.netMinecraftServer("IChatBaseComponent").orElse(null);
    private final static Class<?> chatMessage = SpigotServer.netMinecraftServer("ChatMessage").orElse(null);
    private final static Class<?> packetTitle = SpigotServer.netMinecraftServer("PacketPlayOutTitle").orElse(null);
    private final static Class<?> packetChat = SpigotServer.netMinecraftServer("PacketPlayOutChat").orElse(null);
    private final static Class<?> enumTitleAction = SpigotServer.netMinecraftServer("PacketPlayOutTitle$EnumTitleAction").orElse(
            SpigotServer.netMinecraftServer("EnumTitleAction").orElse(null)
    );

    private final static Map<UUID, TimesComponent> times = new ConcurrentHashMap<>();

    private final Component[] components;

    /**
     * Initialize the message packet
     *
     * @param components the message component
     */
    public MessagePacket(final Component... components) {
        this.components = components;
    }

    /**
     * Send the packet
     *
     * @param player the entity to send
     *               the packet to
     */
    @Override
    public void send(final Player player) {
        if (components == null || components.length == 0) return;
        Arrays.sort(components, (o1, o2) -> {
            switch (o1.getType()) {
                case TIMES:
                    return (o2.getType().equals(TextMessageType.TIMES) ? 0 : -1); //Times go first
                case TITLE:
                    switch (o2.getType()) {
                        case TIMES:
                            return -1;
                        case TITLE:
                            return 0;
                        case SUBTITLE:
                        case ACTIONBAR:
                        case CHAT:
                        default:
                            return 1;
                    }
                case SUBTITLE:
                    switch (o2.getType()) {
                        case TIMES:
                        case TITLE:
                            return -1;
                        case SUBTITLE:
                            return 0;
                        case ACTIONBAR:
                        case CHAT:
                        default:
                            return 1;
                    }
                case ACTIONBAR:
                    switch (o2.getType()) {
                        case TIMES:
                        case TITLE:
                        case SUBTITLE:
                            return -1;
                        case ACTIONBAR:
                            return 0;
                        case CHAT:
                        default:
                            return 1;
                    }
                case CHAT:
                default:
                    return (o2.getType().equals(TextMessageType.CHAT) ? 0 : -1); //Chat messages go last
            }
        });

        for (Component component : components) {
            if (component instanceof ComponentSequence) {
                ComponentSequence sequence = (ComponentSequence) component;
                AnimatedMessagePacket animatedPacket = new AnimatedMessagePacket(sequence);

                animatedPacket.send(player);
                return;
            }

            if (SpigotServer.atOrUnder(MinecraftVersion.v1_10_2)) {
                if (component instanceof TimesComponent) {
                    TimesComponent times = (TimesComponent) component;
                    Constructor<?> constructor = ReflectionUtil.getConstructor(packetTitle, int.class, int.class, int.class);
                    if (constructor == null) return;

                    Object timesPacket = ReflectionUtil.instantiate(constructor, times.getFadeIn(), times.getShow(), times.getFadeOut());
                    if (timesPacket == null) return;

                    SpigotServer.sendPacket(player, timesPacket);
                    MessagePacket.times.put(player.getUniqueId(), times);
                    return;
                }

                if (component instanceof MessageComponent) {
                    MessageComponent message = (MessageComponent) component;
                    String content = message.getContent();

                    Enum<?> type;
                    switch (message.getType()) {
                        case TIMES:
                            throw new IllegalStateException("Cannot send times packet as a message packet");
                        case TITLE:
                        case SUBTITLE:
                            type = ReflectionUtil.getEnumValue(enumTitleAction, message.getType().name());

                            Constructor<?> titleConstructor = ReflectionUtil.getConstructor(packetTitle, enumTitleAction, chatBaseComponent, int.class, int.class, int.class);

                            if (titleConstructor == null) return;

                            TimesComponent times = MessagePacket.times.computeIfAbsent(player.getUniqueId(), (t) -> new TimesMessage(
                                    Times.DEFAULT_FADE_IN,
                                    Times.DEFAULT_STAY,
                                    Times.DEFAULT_FADE_OUT));

                            Object packetMessage = createChatComponent(content);
                            Object packetTitle = ReflectionUtil.instantiate(titleConstructor, type, packetMessage,
                                    times.getFadeIn(), times.getShow(), times.getFadeOut());

                            if (packetTitle == null) return;

                            SpigotServer.sendPacket(player, packetTitle);
                            break;
                        case ACTIONBAR:
                            Constructor<?> packetChatConstructor = ReflectionUtil.getConstructor(packetChat, chatBaseComponent, byte.class);
                            if (packetChatConstructor == null) return;
                            packetChatConstructor.setAccessible(true);

                            Object chatComponent = createChatComponent(content);
                            if (chatComponent == null) return;

                            Object packet = ReflectionUtil.instantiate(packetChatConstructor, chatComponent, (byte) 2);
                            if (packet == null) return;

                            SpigotServer.sendPacket(player, packet);
                            break;
                        case CHAT:
                        default:
                            player.sendMessage(Colorize.colorize(content));
                            break;
                    }
                }
            } else {
                if (component instanceof TimesComponent) {
                    TimesComponent times = (TimesComponent) component;
                    MessagePacket.times.put(player.getUniqueId(), times);

                    player.sendTitle(null, null,
                            (int) times.getFadeIn().getTicks(),
                            (int) times.getShow().getTicks(),
                            (int) times.getFadeOut().getTicks());
                    return;
                }

                if (component instanceof MessageComponent) {
                    MessageComponent message = (MessageComponent) component;
                    String content = message.getContent();

                    TimesComponent times = MessagePacket.times.computeIfAbsent(player.getUniqueId(), (t) -> new TimesMessage(
                            Times.DEFAULT_FADE_IN,
                            Times.DEFAULT_STAY,
                            Times.DEFAULT_FADE_OUT));

                    switch (message.getType()) {
                        case TIMES:
                            throw new IllegalStateException("Cannot send times packet as a message packet");
                        case TITLE:
                            player.sendTitle(Colorize.colorize(content), null,
                                    (int) times.getFadeIn().getTicks(),
                                    (int) times.getShow().getTicks(),
                                    (int) times.getFadeOut().getTicks());
                            break;
                        case SUBTITLE:
                            player.sendTitle(null, Colorize.colorize(content),
                                    (int) times.getFadeIn().getTicks(),
                                    (int) times.getShow().getTicks(),
                                    (int) times.getFadeOut().getTicks());
                            break;
                        case ACTIONBAR:
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                                    Colorize.colorize(content)
                            ));
                            break;
                        case CHAT:
                        default:
                            if (message instanceof ChatComponent) {
                                TextComponent[] components = buildComponent((ChatComponent) message);
                                player.spigot().sendMessage(components);
                            } else {
                                player.sendMessage(Colorize.colorize(content));
                            }
                            break;
                    }
                }
            }
        }
    }

    private static Object createChatComponent(final String content) {
        Constructor<?> messageConstructor = ReflectionUtil.getConstructor(chatMessage, String.class, Object[].class);
        if (messageConstructor == null) return null;

        return ReflectionUtil.instantiate(messageConstructor, content, new Object[0]);
    }

    private static TextComponent[] buildComponent(final ChatComponent component) {
        List<TextComponent> components = new ArrayList<>();
        String content = component.getContent();

        Matcher matcher = hexPattern.matcher(content);
        while (matcher.find()) {
            String hex = matcher.group("color");
            ChatColor color = ChatColor.of("#" + hex);

            content = content.replace(hex, color.toString());
        }

        ComponentClick click = component.getClickEvent();
        ComponentHover hover = component.getHoverEvent();

        ClickEvent clickEvent = null;
        HoverEvent hoverEvent = null;
        if (click != null) {
            switch (click.getAction()) {
                case COPY_TEXT:
                    clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, click.getContent());
                    break;
                case WRITE_TEXT:
                    clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click.getContent());
                    break;
                case EXECUTE:
                    clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, click.getContent());
                    break;
                case OPEN_URL:
                default:
                    clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, click.getContent());
                    break;
            }
        }
        if (hover != null) {
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Colorize.colorize(hover.getContent())));
        }

        TextComponent initial = new TextComponent(Colorize.colorize(content));
        initial.setClickEvent(clickEvent);
        initial.setHoverEvent(hoverEvent);

        components.add(initial);

        for (Component extra : component.getExtra()) {
            if (extra instanceof MessageComponent) {
                MessageComponent msg = (MessageComponent) extra;
                if (msg.getContent().equals(System.lineSeparator())) {
                    components.add(new TextComponent());
                    continue;
                }

                if (msg instanceof ChatComponent) {
                    ChatComponent chat = (ChatComponent) msg;
                    TextComponent[] parsed = buildComponent(chat);
                    for (TextComponent c : parsed) {
                        initial.addExtra(c);
                    }
                } else {
                    initial.addExtra(Colorize.colorize(msg.getContent()));
                }
            }
        }

        return components.toArray(new TextComponent[0]);
    }
}
