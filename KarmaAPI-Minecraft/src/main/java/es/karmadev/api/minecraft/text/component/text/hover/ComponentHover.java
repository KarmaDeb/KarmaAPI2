package es.karmadev.api.minecraft.text.component.text.hover;

/**
 * Represents when a component is
 * hovered
 */
public interface ComponentHover {

    /**
     * Get the action that gets triggered
     *
     * @return the action that gets
     * triggered
     */
    HoverAction getAction();

    /**
     * Get the content
     *
     * @return the content, used by
     * the action
     */
    String getContent();

    /**
     * Get the entity id to show
     * when hover
     *
     * @return the entity id
     */
    int getEntityId();

    /**
     * Get the entity name to show
     * when hover
     *
     * @return the entity name
     */
    String getEntityName();
}
