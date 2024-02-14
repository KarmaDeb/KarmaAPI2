package es.karmadev.api.minecraft.text.component.message.chat;

import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.minecraft.text.TextMessageType;
import es.karmadev.api.minecraft.text.component.Component;
import es.karmadev.api.minecraft.text.component.text.ChatComponent;
import es.karmadev.api.minecraft.text.component.text.MessageComponent;
import es.karmadev.api.minecraft.text.component.text.click.ComponentClick;
import es.karmadev.api.minecraft.text.component.text.hover.ComponentHover;
import es.karmadev.api.minecraft.text.component.text.hover.HoverAction;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a text component
 */
public class ChatMessageComponent implements ChatComponent {

    @Getter
    private String content;
    private final List<Component> extra = new ArrayList<>();

    private ComponentClick clickAction;
    private ComponentHover hoverAction;

    /**
     * Create an empty text component
     */
    public ChatMessageComponent() {
        this.content = "";
    }

    /**
     * Create the text component
     *
     * @param content the text content
     */
    public ChatMessageComponent(final String content) {
        this.content = content;
    }

    /**
     * Set the message content
     *
     * @param content the content
     */
    @Override
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Set the component hover event
     *
     * @param hover the hover event
     */
    @Override
    public void onHover(final ComponentHover hover) {
        this.hoverAction = hover;
    }

    /**
     * Set the component click event
     *
     * @param click the click event
     */
    @Override
    public void onClick(final ComponentClick click) {
        this.clickAction = click;
    }

    /**
     * Get the click event for the specified
     * action
     *
     * @return the action event
     */
    @Override
    public @Nullable ComponentClick getClickEvent() {
        return clickAction;
    }

    /**
     * Get the hover event for the specified
     * action
     *
     * @return the action event
     */
    @Override
    public @Nullable ComponentHover getHoverEvent() {
        return hoverAction;
    }

    /**
     * Get the text message type
     *
     * @return the text message type
     */
    @Override
    public TextMessageType getType() {
        return TextMessageType.CHAT;
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
        throw new UnsupportedOperationException("ChatMessageComponent does not allow type changes");
    }

    /**
     * Get the component content
     *
     * @return the content
     */
    @Override
    public String getRaw() {
        StringBuilder builder = new StringBuilder();
        if (hoverAction == null && clickAction == null) {
            builder.append(content);
        } else {
            builder.append("<container ");

            if (clickAction != null) {
                builder.append("click=\"")
                        .append(clickAction.getAction())
                        .append("\" content=\"")
                        .append(clickAction.getContent())
                        .append("\" ");
            }
            if (hoverAction != null) {
                builder.append("hover=\"")
                        .append(hoverAction.getAction())
                        .append("\" content=\"")
                        .append(hoverAction.getContent())
                        .append("\"");
            }

            builder.append(">").append(content).append("</container>");
        }

        for (Component component : extra) {
            if (component instanceof MessageComponent) {
                builder.append(component.getRaw());
            }
        }

        return builder.toString();
    }

    /**
     * Add an extra component to
     * the current one
     *
     * @param other the component to add
     */
    @Override
    public void addExtra(final Component other) {
        if (other.equals(this)) throw new IllegalArgumentException("Cannot add a component to itself");
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
        JsonObject object = JsonObject.newObject("", "");
        object.put("type", TextMessageType.CHAT.name());
        object.put("content", content);

        JsonObject hover = JsonObject.newObject("", "");
        JsonObject click = JsonObject.newObject("", "");
        JsonArray extras = JsonArray.newArray("", "");

        if (hoverAction != null) {
            hover.put("trigger", hoverAction.getAction().name());
            hover.put("content", hoverAction.getContent());
            if (hoverAction.getAction().equals(HoverAction.SHOW_ENTITY)) {
                JsonObject entityObject = JsonObject.newObject("", "");
                entityObject.put("id", hoverAction.getEntityId());
                entityObject.put("name", hoverAction.getEntityName());

                hover.put("entity", entityObject);
            }
        }

        if (clickAction != null) {
            click.put("trigger", clickAction.getAction().name());
            click.put("content", clickAction.getContent());
        }

        for (int index = 0; index < extra.size(); index++) {
            Component extraComponent = extra.get(index);
            if (extraComponent == null) continue;

            JsonInstance extraObject = JsonReader.read(extraComponent.toJson(pretty));
            if (!extraObject.isObjectType()) continue;

            JsonObject extra = JsonObject.newObject("", "");
            extra.put("index", index);
            extra.put("component", extraObject);

            extras.add(extra);
        }

        object.put("hover", hover);
        object.put("click", click);
        object.put("extra", extras);

        return object.toString(pretty);
    }
}
