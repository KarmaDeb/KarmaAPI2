package es.karmadev.api.minecraft.text;

import com.google.gson.*;
import es.karmadev.api.minecraft.text.component.AnimationComponent;
import es.karmadev.api.minecraft.text.component.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Text component, for creating texts
 */
public class Component {

    private final static Component component = new Component();

    private Component() {}

    /**
     * Create an animated component
     *
     * @return the animated component builder
     */
    public static AnimationBuilder animation() {
        return component.createBuilder();
    }

    /**
     * Create a simple component
     *
     * @return the simple component
     */
    public static RawBuilder simple() {
        return component.createSimpleBuilder();
    }

    /**
     * Create a component from the json element
     *
     * @param json the json
     * @return the component
     * @throws IllegalArgumentException if the json string is not a known component
     */
    public static TextComponent fromJson(final String json) throws IllegalArgumentException {
        Gson gson = new GsonBuilder().create();
        JsonElement element = gson.fromJson(json, JsonElement.class);

        if (element == null) return null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("sequence") && object.has("raw")) {
                JsonElement sequenceElement = object.get("sequence");
                JsonElement rawElement = object.get("raw");

                if (!sequenceElement.isJsonNull()) {
                    throw new IllegalArgumentException("Cannot parse simple text component from json string (" + json + ") because it's not a simple text component");
                }

                if (!rawElement.isJsonPrimitive() || !rawElement.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException("Cannot parse simple text component from json string (" + json + ") because it doesn't has a valid text content");
                }

                String raw = rawElement.getAsString();
                return new RawText(raw);
            }

            throw new IllegalArgumentException("Cannot parse simple text component from json string (" + json + ") because it doesn't has required data [\"sequence\",\"raw\"]");
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

        }

        throw new IllegalArgumentException("Cannot parse animation component from json string (" + json + ") because it's not of a known tye");
    }

    private AnimationBuilder createBuilder() {
        return new AnimationBuilder();
    }

    private RawBuilder createSimpleBuilder() {
        return new RawBuilder();
    }

    /**
     * Animation builder
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    public class AnimationBuilder {

        private final List<TextComponent> components = new ArrayList<>();

        private AnimationBuilder() {}

        /**
         * Append a component to the animation
         *
         * @param component the component
         * @return the builder
         */
        public AnimationBuilder append(final TextComponent component) {
            components.add(component);
            return this;
        }

        /**
         * Append a raw text to the animation
         *
         * @param raw the raw text
         * @return the builder
         */
        public AnimationBuilder append(final String raw) {
            components.add(new RawText(raw));
            return this;
        }

        /**
         * Build the animation sequence
         *
         * @param repeats the amount of times to repeat
         *                the sequence
         * @param interval the sequence repeat interval
         * @return the animated text
         */
        public AnimationComponent build(final int repeats, final long interval) {
            AnimatedText text = new AnimatedText(repeats, interval);
            components.forEach(text::addSequence);

            return text;
        }
    }

    /**
     * Raw builder
     */
    public class RawBuilder {

        private String content;

        private RawBuilder() {}

        /**
         * Set the content
         *
         * @param content the text content
         * @return the builder
         */
        public RawBuilder text(final String content) {
            this.content = content;
            return this;
        }

        /**
         * Build the component
         *
         * @return the component
         */
        public final TextComponent build() {
            return new RawText(content);
        }
    }
}
