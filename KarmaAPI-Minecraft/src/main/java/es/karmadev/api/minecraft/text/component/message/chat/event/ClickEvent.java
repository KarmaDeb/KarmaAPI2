package es.karmadev.api.minecraft.text.component.message.chat.event;

import es.karmadev.api.minecraft.text.component.text.click.ClickAction;
import es.karmadev.api.minecraft.text.component.text.click.ComponentClick;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a click event
 */
@AllArgsConstructor @Getter
public final class ClickEvent implements ComponentClick {

    private final ClickAction action;
    private final String content;
}
