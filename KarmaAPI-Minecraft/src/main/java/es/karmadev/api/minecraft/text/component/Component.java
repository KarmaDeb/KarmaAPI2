package es.karmadev.api.minecraft.text.component;

import es.karmadev.api.minecraft.text.TextMessageType;

import java.util.Collection;

/**
 * Text component
 */
public interface Component {

    /**
     * Create a new component builder
     *
     * @return the new component builder
     */
    static ComponentBuilder builder() {
        return ComponentBuilder.builder();
    }

    /**
     * Get the text message type
     *
     * @return the text message type
     */
    TextMessageType getType();

    /**
     * Update the text message type, please note
     * if the current/new type is {@link TextMessageType#TIMES},
     * this could result in unexpected behaviour
     *
     * @param newType the new type
     */
    void setType(final TextMessageType newType);

    /**
     * Get the raw text
     *
     * @return the raw text
     */
    String getRaw();

    /**
     * Add an extra component to
     * the current one
     *
     * @param other the component to add
     */
    void addExtra(final Component other);

    /**
     * Get the component extra elements
     *
     * @return the extra components
     */
    Collection<? extends Component> getExtra();

    /**
     * Get the component as a
     * json string
     *
     * @param pretty if the json should be pretty
     * @return the json component
     */
    String toJson(final boolean pretty);
}
