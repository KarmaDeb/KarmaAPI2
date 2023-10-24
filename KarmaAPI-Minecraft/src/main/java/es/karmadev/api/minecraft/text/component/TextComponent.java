package es.karmadev.api.minecraft.text.component;

/**
 * Text component
 */
public interface TextComponent {

    /**
     * Get the raw text
     *
     * @return the raw text
     */
    String getRaw();

    /**
     * Converse the component into a json
     * string
     *
     * @return the json component
     */
    String toJson();
}
