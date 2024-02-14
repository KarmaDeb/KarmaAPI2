package es.karmadev.api.minecraft.text.component.message;

import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.KsonException;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.minecraft.text.TextMessageType;
import es.karmadev.api.minecraft.text.component.Component;
import es.karmadev.api.minecraft.text.component.text.MessageComponent;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.*;

/**
 * Represents any type of message component
 */
@AllArgsConstructor
public class SimpleMessageComponent implements MessageComponent {

    private final static String CHAT_TAG = "<chat>%s</chat>";
    private final static String TITLE_TAG = "<header title>%s</header>";
    private final static String SUBTITLE_TAG = "<header subtitle>%s</header>";
    private final static String ACTIONBAR_TAG = "<actionbar>%s</actionbar>";

    @NonNull
    private String content;
    private TextMessageType type;

    private final List<Component> extra = new ArrayList<>();

    /**
     * Create an empty simple message
     *
     * @param type the message type
     */
    public SimpleMessageComponent(final TextMessageType type) {
        this("", type);
    }

    /**
     * Get the text message type
     *
     * @return the text message type
     */
    @Override
    public TextMessageType getType() {
        return type;
    }

    /**
     * Update the text message type, please note
     * if the current/new type is {@link TextMessageType#TIMES},
     * this could result in unexpected behaviour
     *
     * @param newType the new type
     */
    @Override
    public void setType(final TextMessageType newType) {
        if (this.type.equals(newType)) return;

        if (this.type.equals(TextMessageType.TIMES))
            throw new IllegalArgumentException("Times message type cannot be changed to another type");

        if (newType.equals(TextMessageType.TIMES))
            throw new IllegalArgumentException("Cannot convert text message type to times");
    }

    /**
     * Get the raw text
     *
     * @return the raw text
     */
    @Override
    public String getRaw() {
        switch (type) {
            case CHAT:
                return String.format(CHAT_TAG, content);
            case TITLE:
                return String.format(TITLE_TAG, content);
            case SUBTITLE:
                return String.format(SUBTITLE_TAG, content);
            case ACTIONBAR:
                return String.format(ACTIONBAR_TAG, content);
            default:
                return null;
        }
    }

    /**
     * Add an extra component to
     * the current one
     *
     * @param other the component to add
     */
    @Override
    public void addExtra(final Component other) {
        extra.add(other);
    }

    /**
     * Get the component extra elements
     *
     * @return the extra components
     */
    @Override
    public Collection<? extends Component> getExtra() {
        return Collections.unmodifiableList(extra);
    }

    /**
     * Get the component as a
     * json string
     *
     * @param pretty if the json should be pretty
     * @return the json component
     */
    @Override
    public String toJson(final boolean pretty) {
        JsonObject main = JsonObject.newObject("", "");

        JsonObject click = JsonObject.newObject("", "click");
        JsonObject hover = JsonObject.newObject("", "hover");

        JsonObject extra = JsonObject.newObject("", "extra");
        JsonArray extraElements = JsonArray.newArray("extra", "extraElements");

        main.put("type", type.name());
        main.put("content", content);
        if (this.type.equals(TextMessageType.CHAT)) {
            main.put("click", click);
            main.put("hover", hover);

            extra.put("size", this.extra.size());
            int index = 0;
            for (Component component : this.extra) {
                String rawJson = component.toJson(pretty);
                try {
                    JsonInstance element = JsonReader.read(rawJson);

                    JsonObject extraElement = JsonObject.newObject("extra", "elements");
                    extraElement.put("index", ++index);
                    extraElement.put("component", element);

                    extraElements.add(extraElement);
                } catch (KsonException ignored) {
                }
            }
            extra.put("elements", extraElements);

            main.put("extra", extra);
        }

        return main.toString();
    }

    /**
     * Set the message content
     *
     * @param content the content
     */
    @Override
    public void setContent(final @NonNull String content) {
        this.content = content;
    }

    /**
     * Get the message content
     *
     * @return the message content
     */
    @Override
    public @NonNull String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageComponent)) return false;

        MessageComponent that = (MessageComponent) o;
        return Objects.equals(content, that.getContent()) && type == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, type);
    }
}
