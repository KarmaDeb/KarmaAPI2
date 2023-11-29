package es.karmadev.api.minecraft.text.component;

import com.google.gson.*;
import es.karmadev.api.minecraft.text.Color;
import es.karmadev.api.minecraft.text.TextMessageType;
import es.karmadev.api.minecraft.text.component.exception.NotComponentException;
import es.karmadev.api.minecraft.text.component.message.AnimatedComponent;
import es.karmadev.api.minecraft.text.component.message.SimpleMessageComponent;
import es.karmadev.api.minecraft.text.component.message.chat.ChatMessageComponent;
import es.karmadev.api.minecraft.text.component.message.chat.event.ClickEvent;
import es.karmadev.api.minecraft.text.component.message.chat.event.HoverEvent;
import es.karmadev.api.minecraft.text.component.message.header.TimesMessage;
import es.karmadev.api.minecraft.text.component.text.ChatComponent;
import es.karmadev.api.minecraft.text.component.text.MessageComponent;
import es.karmadev.api.minecraft.text.component.text.click.ClickAction;
import es.karmadev.api.minecraft.text.component.text.click.ComponentClick;
import es.karmadev.api.minecraft.text.component.text.hover.ComponentHover;
import es.karmadev.api.minecraft.text.component.text.hover.HoverAction;
import es.karmadev.api.minecraft.text.component.title.Times;
import es.karmadev.api.minecraft.text.component.title.TimesComponent;
import es.karmadev.api.strings.StringUtils;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a component builder
 */
public class ComponentBuilder {

    private final static Pattern TIMES_PATTERN = Pattern.compile("<times\\s*(?<fadeIn>fadeIn=\"(?<fiTime>[0-9]+(ms|s|m|h|d)?|reset|default|none)\")\\s*(?<show>show=\"(?<shTime>[0-9]+(ms|s|m|h|d)?|reset|default|none)\")\\s*(?<fadeOut>fadeOut=\"(?<foTime>[0-9]+(ms|s|m|h|d)?|reset|default|none)\")>", Pattern.CASE_INSENSITIVE);
    private final static Pattern ACTIONBAR_PATTERN = Pattern.compile("<actionbar>(?<text>[a-zA-Z0-9~@#$^*()_+=\\[\\]{}|\\\\,.?: -&§]*)</actionbar>", Pattern.CASE_INSENSITIVE);
    private final static Pattern HEADER_PATTERN = Pattern.compile("<header\\s*(?<type>title|subtitle)>(?<text>[a-zA-Z0-9~@#$^*()_+=\\[\\]{}|\\\\,.?: -&§]*)</header>", Pattern.CASE_INSENSITIVE);
    private final static Pattern CHAT_PATTERN = Pattern.compile("(?<container><chat\\s*(?<click>click=\"(?<clickAction>OPEN_URL|COPY_TEXT|WRITE_TEXT|EXECUTE)\"\\s*content=\"(?<clickContent>\\bhttps?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]|[\\w\\s&§]*)\")?\\s*(?<hover>hover=\"(?<hoverAction>SHOW_TEXT)\"\\s*content=\"(?<hoverText>[a-zA-Z0-9~@#$^*()_+=\\[\\]{}|\\\\,.?: -&§]*)\")?>(?<message>[a-zA-Z0-9~@#$^*()_+=\\[\\]{}|\\\\,.?: -&§]*)</chat>)", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    private final static Pattern COLOR_PATTERN = Pattern.compile("<(?<color>black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|purple|yellow|white|reset)>", Pattern.CASE_INSENSITIVE);
    private final static Pattern STYLE_PATTERN = Pattern.compile("<(?<style>bold|italic|underline|strikethrough|magic)>", Pattern.CASE_INSENSITIVE);

    private final List<ChatComponent> messageComponents = new ArrayList<>();
    private final List<MessageComponent> actionbarComponents = new ArrayList<>();
    private final List<MessageComponent> titleMessageComponents = new ArrayList<>();
    private final List<MessageComponent> subtitleMessageComponents = new ArrayList<>();
    final List<ComponentSequence> sequenceComponents = new ArrayList<>();

    private String text = null;

    @Setter
    @Accessors(fluent = true)
    @NonNull
    private Color color = Color.RESET;

    @Setter @Accessors(fluent = true)
    private boolean bold = false;
    @Setter @Accessors(fluent = true)
    private boolean italic = false;
    @Setter @Accessors(fluent = true)
    private boolean strikethrough = false;
    @Setter @Accessors(fluent = true)
    private boolean underline = false;
    @Setter @Accessors(fluent = true)
    private boolean magic = false;


    private String title = null;
    private String subtitle = null;

    private boolean addTimes = false;

    @Accessors(fluent = true) @NonNull
    private Times fadeIn = Times.DEFAULT_FADE_IN;
    @Accessors(fluent = true) @NonNull
    private Times show = Times.DEFAULT_STAY;
    @Accessors(fluent = true) @NonNull
    private Times fadeOut = Times.DEFAULT_FADE_OUT;

    private String actionbar = null;

    private ComponentClick clickAction;
    private ComponentHover hoverAction;

    /**
     * Get an empty component builder
     *
     * @return an empty component
     * builder
     */
    public static ComponentBuilder builder() {
        return new ComponentBuilder();
    }

    /**
     * Creates a component builder
     *
     * @param text component
     * @return the component builder
     */
    public static ComponentBuilder chat(final String text) {
        return new ComponentBuilder().text(text);
    }

    /**
     * Creates a component builder
     *
     * @param title component
     * @param subtitle component
     * @return the component builder
     */
    public static ComponentBuilder header(final String title, final String subtitle) {
        return new ComponentBuilder().title(title).subtitle(subtitle);
    }

    /**
     * Creates a component builder
     *
     * @param title component
     * @param subtitle component
     * @param fadeIn fade in time
     * @param show show time
     * @param fadeOut fade out time
     * @return the component builder
     */
    public static ComponentBuilder header(final String title, final String subtitle, final Times fadeIn, final Times show, final Times fadeOut) {
        return new ComponentBuilder().title(title).subtitle(subtitle).fadeIn(fadeIn).show(show).fadeOut(fadeOut);
    }

    /**
     * Creates a component builder
     *
     * @param actionbar component
     * @param persistent if the actionbar should be persistent
     * @return the component builder
     */
    public static ComponentBuilder actionbar(final String actionbar, final boolean persistent) {
        return new ComponentBuilder().actionbar(actionbar);
    }

    /**
     * Get a component from a json
     * string
     *
     * @param json the json string
     * @return the component
     * @throws NotComponentException if the json string does not
     * represent a component
     */
    public static Component fromJson(final String json) throws NotComponentException {
        Gson gson = new GsonBuilder().create();
        JsonElement element = gson.fromJson(json, JsonElement.class);
        if (element == null || !element.isJsonObject()) throw new NotComponentException(json);

        JsonObject object = element.getAsJsonObject();
        return parse(object);
    }

    public static Component[] parse(final String content) {
        try {
            Component component = fromJson(content);
            return new Component[]{component};
        } catch (NotComponentException ignored) {}

        List<Component> components = new ArrayList<>();
        if (content.contains("\n")) {
            for (String line : content.split("\n")) {
                Component[] parsed = parse(line, true);
                components.addAll(Arrays.asList(parsed));
            }

            return components.toArray(new Component[0]);
        }

        return parse(content, false);
    }

    private static Component[] parse(final String content, final boolean newLine) {
        List<Component> components = new ArrayList<>();
        StringBuilder messageMod = new StringBuilder(content);

        Matcher colorMatcher = COLOR_PATTERN.matcher(new StringBuilder(messageMod));
        int colorOffset = 0;
        while (colorMatcher.find()) {
            int start = colorMatcher.start();
            int end = colorMatcher.end();

            String cName = colorMatcher.group("color");
            Color color = Color.getByName(cName);
            if (color == null) continue;

            messageMod.replace(start - colorOffset, end - colorOffset, "§" + color.getCode());
            colorOffset += (end - start) - 2;
        }

        Matcher styleMatcher = STYLE_PATTERN.matcher(new StringBuilder(messageMod));
        int styleOffset = 0;
        while (styleMatcher.find()) {
            int start = styleMatcher.start();
            int end = styleMatcher.end();

            String style = styleMatcher.group("style");

            char code = '\0';
            switch (style.toLowerCase()) {
                case "bold":
                    code = 'l';
                    break;
                case "italic":
                    code = 'o';
                    break;
                case "underline":
                    code = 'n';
                    break;
                case "strikethrough":
                    code = 'm';
                    break;
                case "magic":
                    code = 'k';
                    break;
                default:
                    break;
            }

            if (code != '\0') {
                messageMod.replace(start - styleOffset, end - styleOffset, "§" + code);
                styleOffset += (end - start) - 2;
            }
        }

        Matcher actionbarMatcher = ACTIONBAR_PATTERN.matcher(new StringBuilder(messageMod));
        int barOffset = 0;
        while (actionbarMatcher.find()) {
            int start = actionbarMatcher.start();
            int end = actionbarMatcher.end();

            SimpleMessageComponent barMessage = new SimpleMessageComponent(actionbarMatcher.group("text"), TextMessageType.ACTIONBAR);

            messageMod.replace(start - barOffset, end - barOffset, "");
            barOffset += (end - start);
            components.add(barMessage);
        }

        Matcher headerPattern = HEADER_PATTERN.matcher(new StringBuilder(messageMod));
        int titleOffset = 0;
        while (headerPattern.find()) {
            int start = headerPattern.start();
            int end = headerPattern.end();

            String type = headerPattern.group("type");
            TextMessageType tmt;
            switch (type.toLowerCase()) {
                case "title":
                    tmt = TextMessageType.TITLE;
                    break;
                case "subtitle":
                    tmt = TextMessageType.SUBTITLE;
                    break;
                default:
                    continue;
            }

            Component component = new SimpleMessageComponent(headerPattern.group("text"), tmt);
            components.add(component);

            messageMod.replace(start - titleOffset, end - titleOffset, "");
            titleOffset += (end - start);
            //return components.toArray(new Component[0]);
        }

        Matcher timesPattern = TIMES_PATTERN.matcher(new StringBuilder(messageMod));
        int timesOffset = 0;
        while (timesPattern.find()) {
            int start = timesPattern.start();
            int end = timesPattern.end();

            String fiTime = timesPattern.group("fiTime");
            String shTime = timesPattern.group("shTime");
            String foTime = timesPattern.group("foTime");

            Times fadeIn = Times.parse(fiTime, Times.TimesType.FADE_IN);
            Times show = Times.parse(shTime, Times.TimesType.STAY);
            Times fadeOut = Times.parse(foTime, Times.TimesType.FADE_OUT);

            if (fadeIn == null || show == null || fadeOut == null) continue;

            TimesComponent component = new TimesMessage(fadeIn, show, fadeOut);
            components.add(component);

            messageMod.replace(start - timesOffset, end - timesOffset, "");
            timesOffset += (end - start);
        }

        Matcher clickMatcher = CHAT_PATTERN.matcher(messageMod);
        MessageComponent parent = new ChatMessageComponent("");

        int lastIndex = 0;
        boolean prepared = false;
        while (clickMatcher.find()) {
            prepared = true;
            int being = clickMatcher.start("container");
            String nonFormatted = messageMod.substring(lastIndex, being);
            parent.addExtra(new SimpleMessageComponent(nonFormatted, TextMessageType.CHAT));

            lastIndex = clickMatcher.end("container");

            ClickEvent click = null;
            HoverEvent hover = null;
            try {
                String clickAction = clickMatcher.group("clickAction");
                String clickContent = clickMatcher.group("clickContent");

                click = new ClickEvent(ClickAction.valueOf(clickAction.toUpperCase()), clickContent);
            } catch (IllegalArgumentException | NullPointerException ignored) {}
            try {
                String hoverAction = clickMatcher.group("hoverAction");
                String hoverText = clickMatcher.group("hoverText");

                hover = new HoverEvent(HoverAction.valueOf(hoverAction.toUpperCase()), hoverText);
            } catch (IllegalArgumentException | NullPointerException ignored) {}

            String displayText = clickMatcher.group("message");
            ChatComponent component = new ChatMessageComponent(displayText.replace("&", "§"));
            component.onClick(click);
            component.onHover(hover);

            parent.addExtra(component);
        }

        if (!prepared) {
            return new Component[]{new SimpleMessageComponent(messageMod.toString().replace("&", "§"), TextMessageType.CHAT)};
        }
        if (lastIndex < messageMod.length()) {
            parent.addExtra(new SimpleMessageComponent(messageMod.substring(lastIndex).replace("&", "§"), TextMessageType.CHAT));
        }

        components.add(parent);
        if (newLine) {
            components.add(new SimpleMessageComponent(System.lineSeparator(), TextMessageType.CHAT));
        }

        return components.toArray(new Component[0]);
    }

    private String getFormat() {
        String format = "§r";
        if (!this.color.equals(Color.RESET)) {
            if (this.color.isCustom()) {
                format = "§#(" + Integer.toHexString(this.color.getHex()) + ")";
            } else {
                format = "§" + this.color.getCode();
            }
        }

        if (bold) {
            format += "§l";
        }
        if (italic) {
            format += "§o";
        }
        if (strikethrough) {
            format += "§m";
        }
        if (underline) {
            format += "§n";
        }
        if (magic) {
            format += "§k";
        }

        return format;
    }

    /**
     * Start building a new sequence
     *
     * @param sequenceType the sequence type
     * @return the sequence
     */
    public SeqBuilder sequenceStart(final TextMessageType sequenceType) {
        if (sequenceType.equals(TextMessageType.TIMES)) throw new IllegalStateException("Cannot create a sequence for times");
        return new SeqBuilder(sequenceType, this);
    }

    /**
     * Set the component fade in
     *
     * @param times the fade in time
     * @return the component builder
     */
    public ComponentBuilder fadeIn(final Times times) {
        this.fadeIn = times;
        this.addTimes = true;

        return this;
    }

    /**
     * Set the component show
     *
     * @param times the show time
     * @return the component builder
     */
    public ComponentBuilder show(final Times times) {
        this.show = times;
        this.addTimes = true;

        return this;
    }

    /**
     * Set the component fade out
     *
     * @param times the fade out time
     * @return the component builder
     */
    public ComponentBuilder fadeOut(final Times times) {
        this.fadeOut = times;
        this.addTimes = true;

        return this;
    }

    /**
     * Add a text element to the component
     * builder
     *
     * @param text the text element
     * @return the component builder
     */
    public ComponentBuilder text(final String text) {
        if (this.text != null) {
            String format = getFormat();
            if (!StringUtils.containsLetter(this.text) && !StringUtils.containsNumber(this.text)) {
                format = ""; //Clear format for non-ascii formatted text
            }

            ChatMessageComponent component = new ChatMessageComponent(format + this.text);
            component.onClick(clickAction);
            component.onHover(hoverAction);

            clickAction = null;
            hoverAction = null;
            messageComponents.add(component);
        }

        this.text = text;
        return this;
    }

    /**
     * Add a title element to the component
     *
     * @param title the title
     * @return the component builder
     */
    public ComponentBuilder title(final String title) {
        if (this.title != null) {
            String format = getFormat();
            if (!StringUtils.containsLetter(this.title) && !StringUtils.containsNumber(this.title)) {
                format = ""; //Clear format for non-ascii formatted text
            }

            titleMessageComponents.add(new SimpleMessageComponent(format + this.title, TextMessageType.TITLE));
        }

        this.title = title;
        return this;
    }

    /**
     * Add a subtitle element to the component
     *
     * @param subtitle the subtitle
     * @return the component builder
     */
    public ComponentBuilder subtitle(final String subtitle) {
        if (this.subtitle != null) {
            String format = getFormat();
            if (!StringUtils.containsLetter(this.subtitle) && !StringUtils.containsNumber(this.subtitle)) {
                format = ""; //Clear format for non-ascii formatted text
            }

            subtitleMessageComponents.add(new SimpleMessageComponent(format + this.subtitle, TextMessageType.SUBTITLE));
        }

        this.subtitle = subtitle;
        return this;
    }

    public ComponentBuilder actionbar(final String actionbar) {
        if (this.actionbar != null) {
            String format = getFormat();
            if (!StringUtils.containsLetter(this.actionbar) && !StringUtils.containsNumber(this.actionbar)) {
                format = ""; //Clear format for non-ascii formatted text
            }

            SimpleMessageComponent component = new SimpleMessageComponent(format + this.actionbar, TextMessageType.ACTIONBAR);
            actionbarComponents.add(component);
        }

        this.actionbar = actionbar;
        return this;
    }

    /**
     * Add a listener
     *
     * @param click the listener
     * @return the component builder
     */
    public ComponentBuilder onClick(final ComponentClick click) {
        clickAction = click;
        return this;
    }

    /**
     * Add a listener
     *
     * @param action the action trigger
     * @param content the action content
     * @return the component builder
     */
    public ComponentBuilder onClick(final ClickAction action, final String content) {
        clickAction = new ClickEvent(action, content);
        return this;
    }

    /**
     * Add a listener
     *
     * @param hover the listener
     * @return the component builder
     */
    public ComponentBuilder onHover(final ComponentHover hover) {
        hoverAction = hover;
        return this;
    }

    /**
     * Add a listener
     *
     * @param action the action trigger
     * @param content the action content
     * @return the component builder
     */
    public ComponentBuilder onHover(final HoverAction action, final String content) {
        hoverAction = new HoverEvent(action, content);
        return this;
    }

    /**
     * Add a listener
     *
     * @param action the action trigger
     * @param content the action content
     * @param entityId action extra#1
     * @param entityName action extra#2
     * @return the component builder
     */
    public ComponentBuilder onHover(final HoverAction action, final String content, final int entityId, final String entityName) {
        hoverAction = new HoverEvent(action, content, entityId, entityName);
        return this;
    }

    /**
     * Create the component
     *
     * @param excludes the components to exclude
     * @return the component
     */
    public Component[] build(final TextMessageType... excludes) {
        Set<TextMessageType> excludesSet = EnumSet.noneOf(TextMessageType.class);
        excludesSet.addAll(Arrays.asList(excludes));

        if (this.text != null) {
            String format = getFormat();

            ChatMessageComponent component = new ChatMessageComponent(format + this.text);
            component.onClick(clickAction);
            component.onHover(hoverAction);

            messageComponents.add(component);
        }
        if (this.title != null) {
            String format = getFormat();

            SimpleMessageComponent component = new SimpleMessageComponent(format + this.title, TextMessageType.TITLE);
            titleMessageComponents.add(component);
        }
        if (this.subtitle != null) {
            String format = getFormat();

            SimpleMessageComponent component = new SimpleMessageComponent(format + this.subtitle, TextMessageType.SUBTITLE);
            subtitleMessageComponents.add(component);
        }
        if (this.actionbar != null) {
            String format = getFormat();

            SimpleMessageComponent component = new SimpleMessageComponent(format + this.actionbar, TextMessageType.ACTIONBAR);
            actionbarComponents.add(component);
        }

        List<Component> componentBundle = new ArrayList<>();
        if (!messageComponents.isEmpty()) {
            MessageComponent firstElement = null;
            for (MessageComponent component : messageComponents) {
                if (firstElement == null) {
                    firstElement = component;
                    continue;
                }

                firstElement.addExtra(component);
            }

            componentBundle.add(firstElement);
        }
        boolean hasTimes = !titleMessageComponents.isEmpty() || !subtitleMessageComponents.isEmpty() || addTimes;
        if (hasTimes) {
            TimesMessage times = new TimesMessage(fadeIn, show, fadeOut);
            componentBundle.add(times);
        }

        boolean iterateSubtitles = true;
        if (!titleMessageComponents.isEmpty()) {
            iterateSubtitles = false;

            int index = 0;
            for (MessageComponent component : titleMessageComponents) {
                componentBundle.add(component);
                if (index < subtitleMessageComponents.size()) {
                    MessageComponent subtitle = subtitleMessageComponents.get(index);
                    componentBundle.add(subtitle);
                }
            }
        }

        if (iterateSubtitles) {
            componentBundle.addAll(subtitleMessageComponents);
        }

        if (!actionbarComponents.isEmpty()) {
            componentBundle.addAll(actionbarComponents);
        }
        componentBundle.addAll(sequenceComponents);
        componentBundle.removeIf((component -> excludesSet.contains(component.getType())));

        return componentBundle.toArray(new Component[0]);
    }

    /**
     * Parse the object into a component
     *
     * @param object the object
     * @return the parsed component
     * @throws NotComponentException if the json object does not
     * represent a component
     */
    private static Component parse(final JsonObject object) throws NotComponentException {
        if (missesAny(object, "type")) throw new NotComponentException(object);
        if (someNotString(object, "type")) throw new NotComponentException(object);

        String rawType = object.get("type").getAsString();
        if (StringUtils.startsWithIgnoreCase(rawType, "seq_")) {
            TextMessageType type;
            try {
                type = TextMessageType.valueOf(rawType.toUpperCase().substring(4));
            } catch (IllegalArgumentException ex) {
                throw new NotComponentException(object);
            }

            if (type.equals(TextMessageType.TIMES)) throw new NotComponentException(object);
            if (someNotNumber(object, "size", "interval", "repeats")) throw new NotComponentException(object);
            if (someNotArray(object, "sequence")) throw new NotComponentException(object);

            int size = object.get("size").getAsInt();
            long interval = object.get("interval").getAsLong();
            int repeats = object.get("repeats").getAsInt();

            AnimatedComponent animation = new AnimatedComponent(repeats, Times.exact(interval), type);
            if (size <= 0) {
                return animation;
            }

            JsonArray sequence = object.get("sequence").getAsJsonArray();
            Map<Integer, Component> componentMap = new HashMap<>();

            for (JsonElement element : sequence) {
                if (element.isJsonObject()) {
                    JsonObject elementObject = element.getAsJsonObject();
                    if (someNotNumber(elementObject, "index")) continue;
                    if (someNotObject(elementObject, "component")) continue;

                    int index = elementObject.get("index").getAsInt();
                    JsonObject child = elementObject.getAsJsonObject("component");
                    try {
                        Component parsed = parse(child);
                        componentMap.put(index, parsed);
                    } catch (NotComponentException ignored) {}
                }
            }

            if (componentMap.size() != size) {
                throw new IllegalStateException("Sequence size (" + componentMap.size() +  ") does not match the json stored sequence data (" + size + ")!");
            }

            List<Integer> sorted = new ArrayList<>(componentMap.keySet());
            Collections.sort(sorted);

            for (int index : sorted) {
                animation.setSequence(index, componentMap.get(index));
            }

            return animation;
        }

        TextMessageType type;
        try {
            type = TextMessageType.valueOf(rawType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new NotComponentException(object);
        }

        switch (type) {
            case ACTIONBAR:
                if (someNotString(object, "content")) throw new NotComponentException(object);
                String barContent = object.get("content").getAsString();

                return new SimpleMessageComponent(barContent, TextMessageType.ACTIONBAR);
            case TITLE:
                if (someNotString(object, "content")) throw new NotComponentException(object);
                String titleContent = object.get("content").getAsString();

                return new SimpleMessageComponent(titleContent, TextMessageType.TITLE);
            case SUBTITLE:
                if (someNotString(object, "content")) throw new NotComponentException(object);
                String subtitleContent = object.get("content").getAsString();

                return new SimpleMessageComponent(subtitleContent, TextMessageType.SUBTITLE);
            case TIMES:
                if (someNotObject(object, "times")) throw new NotComponentException(object);
                JsonObject times = object.getAsJsonObject("times");
                if (someNotNumber(times, "fadeIn", "stay", "fadeOut")) throw new NotComponentException(object);

                long fadeIn = times.get("fadeIn").getAsLong();
                long show = times.get("stay").getAsLong();
                long fadeOut = times.get("fadeOut").getAsLong();

                Times t1 = Times.exact(fadeIn);
                Times t2 = Times.exact(show);
                Times t3 = Times.exact(fadeOut);

                return new TimesMessage(t1, t2, t3);
            case CHAT:
            default:
                if (someNotString(object, "content")) throw new NotComponentException(object);
                if (someNotObject(object, "hover", "click")) throw new NotComponentException(object);
                if (someNotArray(object, "extra")) throw new NotComponentException(object);
                String chatContent = object.get("content").getAsString();

                ChatMessageComponent chatMessage = new ChatMessageComponent(chatContent);
                if (!someNotObject(object, "hover")) {
                    JsonObject hover = object.getAsJsonObject("hover");
                    HoverEvent event = getHoverEvent(hover);
                    if (event != null) {
                        chatMessage.onHover(event);
                    }
                }
                if (!someNotObject(object, "click")) {
                    JsonObject click = object.getAsJsonObject("click");
                    ClickEvent event = getClickEvent(click);
                    if (event != null) {
                        chatMessage.onClick(event);
                    }
                }
                if (!someNotArray(object, "extra")) {
                    JsonArray array = object.getAsJsonArray("extra");
                    Map<Integer, Component> componentMap = new HashMap<>();

                    for (JsonElement element : array) {
                        if (element.isJsonObject()) {
                            JsonObject elementObject = element.getAsJsonObject();
                            if (someNotNumber(elementObject, "index")) continue;
                            if (someNotObject(elementObject, "component")) continue;

                            int index = elementObject.get("index").getAsInt();
                            JsonObject child = elementObject.getAsJsonObject("component");
                            try {
                                Component parsed = parse(child);
                                componentMap.put(index, parsed);
                            } catch (NotComponentException ignored) {}
                        }
                    }

                    List<Integer> sorted = new ArrayList<>(componentMap.keySet());
                    Collections.sort(sorted);

                    for (int index : sorted) {
                        chatMessage.addExtra(componentMap.get(index));
                    }
                }

                return chatMessage;
        }
    }

    private static boolean missesAny(final JsonObject object, final String... keys) {
        for (String key : keys) {
            if (!object.has(key)) return true;
        }

        return false;
    }

    private static boolean someNotPrimitive(final JsonObject object, final String... keys) {
        if (missesAny(object, keys)) return true;

        for (String key : keys) {
            JsonElement element = object.get(key);
            if (!element.isJsonPrimitive()) return true;
        }

        return false;
    }

    private static boolean someNotArray(final JsonObject object, final String... keys) {
        if (missesAny(object, keys)) return true;

        for (String key : keys) {
            JsonElement element = object.get(key);
            if (!element.isJsonArray()) return true;
        }

        return false;
    }

    private static boolean someNotObject(final JsonObject object, final String... keys) {
        if (missesAny(object, keys)) return true;

        for (String key : keys) {
            JsonElement element = object.get(key);
            if (!element.isJsonObject()) return true;
        }

        return false;
    }

    private static boolean someNotString(final JsonObject object, final String... keys) {
        if (missesAny(object, keys) || someNotPrimitive(object, keys)) return true;

        for (String key : keys) {
            JsonPrimitive element = object.getAsJsonPrimitive(key);
            if (!element.isString()) return true;
        }

        return false;
    }

    private static boolean someNotNumber(final JsonObject object, final String... keys) {
        if (missesAny(object, keys) || someNotPrimitive(object, keys)) return true;

        for (String key : keys) {
            JsonPrimitive element = object.getAsJsonPrimitive(key);
            if (!element.isNumber()) return true;
        }

        return false;
    }

    private static HoverEvent getHoverEvent(JsonObject object) {
        if (!object.has("trigger") || !object.has("content")) return null;

        JsonPrimitive triggerPrimitive = object.get("trigger").getAsJsonPrimitive();
        JsonPrimitive hoverContentPrimitive = object.get("content").getAsJsonPrimitive();

        if (!triggerPrimitive.isString() || !hoverContentPrimitive.isString()) return null;
        try {
            HoverAction action = HoverAction.valueOf(triggerPrimitive.getAsString());
            switch (action) {
                case SHOW_ITEM:
                case SHOW_TEXT:
                    return new HoverEvent(action, hoverContentPrimitive.getAsString());
                case SHOW_ENTITY:
                    if (!object.has("entity")) return null;
                    JsonObject entityObject = object.get("entity").getAsJsonObject();

                    if (!entityObject.has("id") || !entityObject.has("name")) return null;

                    JsonElement idElement = entityObject.get("id");
                    JsonElement nameElement = entityObject.get("name");

                    if (!idElement.isJsonPrimitive() || !nameElement.isJsonPrimitive()) return null;

                    JsonPrimitive idPrimitive = idElement.getAsJsonPrimitive();
                    JsonPrimitive namePrimitive = nameElement.getAsJsonPrimitive();

                    if (!idPrimitive.isNumber() || !namePrimitive.isString()) return null;

                    return new HoverEvent(action, hoverContentPrimitive.getAsString(), idPrimitive.getAsInt(), namePrimitive.getAsString());
                default:
                    return null;
            }
        } catch (IllegalArgumentException ignored) {}

        return null;
    }

    private static ClickEvent getClickEvent(JsonObject object) {
        if (!object.has("trigger") || !object.has("content")) return null;

        JsonPrimitive triggerPrimitive = object.get("trigger").getAsJsonPrimitive();
        JsonPrimitive clickContentPrimitive = object.get("content").getAsJsonPrimitive();

        if (!triggerPrimitive.isString() || !clickContentPrimitive.isString()) return null;

        try {
            ClickAction action = ClickAction.valueOf(triggerPrimitive.getAsString());
            return new ClickEvent(action, clickContentPrimitive.getAsString());
        } catch (IllegalArgumentException ignored) {}

        return null;
    }
}
