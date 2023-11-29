package es.karmadev.api.minecraft.text.component.text;

import es.karmadev.api.minecraft.text.component.Component;

/**
 * Represents a chat message component
 */
public interface MessageComponent extends Component {

    /**
     * Set the message content
     *
     * @param content the content
     */
    void setContent(final String content);

    /**
     * Get the message content
     *
     * @return the message content
     */
    String getContent();
}
