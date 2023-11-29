package es.karmadev.api.minecraft.text.component.text;

import es.karmadev.api.minecraft.text.component.text.click.ComponentClick;
import es.karmadev.api.minecraft.text.component.text.hover.ComponentHover;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a chat message component, which
 * can have special behaviour when clicked or
 * hovered
 */
public interface ChatComponent extends MessageComponent {

    /**
     * Set the component hover event
     *
     * @param hover the hover event
     */
    void onHover(final ComponentHover hover);

    /**
     * Set the component click event
     *
     * @param click the click event
     */
    void onClick(final ComponentClick click);

    /**
     * Get the click event for the specified
     * action
     *
     * @return the action event
     */
    @Nullable
    ComponentClick getClickEvent();

    /**
     * Get the hover event for the specified
     * action
     *
     * @return the action event
     */
    @Nullable
    ComponentHover getHoverEvent();
}
