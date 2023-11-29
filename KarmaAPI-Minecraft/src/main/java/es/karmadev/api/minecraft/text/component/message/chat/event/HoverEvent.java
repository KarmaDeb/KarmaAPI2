package es.karmadev.api.minecraft.text.component.message.chat.event;

import es.karmadev.api.minecraft.text.component.text.hover.ComponentHover;
import es.karmadev.api.minecraft.text.component.text.hover.HoverAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a hover event
 */
@AllArgsConstructor
@Getter
public final class HoverEvent implements ComponentHover {

    private final HoverAction action;
    private final String content;
    private final int entityId;
    private final String entityName;

    /**
     * Create the hover event
     *
     * @param action the action
     * @param content the content
     */
    public HoverEvent(final HoverAction action, final String content) {
        if (action.equals(HoverAction.SHOW_ENTITY)) throw new IllegalArgumentException("Show requires full-constructor");
        this.action = action;
        this.content = content;
        this.entityId = 0;
        this.entityName = null;
    }
}
