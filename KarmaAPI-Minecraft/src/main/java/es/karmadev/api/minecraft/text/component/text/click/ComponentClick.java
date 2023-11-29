package es.karmadev.api.minecraft.text.component.text.click;

/**
 * Represents when a component is
 * hovered
 */
public interface ComponentClick {

    /**
     * Get the action that gets triggered
     *
     * @return the action that gets
     * triggered
     */
    ClickAction getAction();

    /**
     * Get the content
     *
     * @return the content, used by
     * the action
     */
    String getContent();
}
