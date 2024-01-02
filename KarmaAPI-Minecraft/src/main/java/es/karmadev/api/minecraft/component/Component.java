package es.karmadev.api.minecraft.component;

import es.karmadev.api.minecraft.component.node.NodeImpl;
import lombok.NonNull;

import java.util.Collection;

/**
 * Represents a component element
 * for a minecraft client. Components
 * indicate how a text is shown to a
 * client.
 */
public interface Component extends Appendable<NodeImpl> {

    /**
     * Create a new component builder
     *
     * @return the component builder
     */
    static ComponentBuilder empty() {
        return new ComponentBuilder();
    }

    /**
     * Create a new component builder
     *
     * @param text the builder text
     * @return the component builder
     */
    static ComponentBuilder text(final @NonNull CharSequence text) {
        return new ComponentBuilder(text);
    }

    /**
     * Create a new component builder
     *
     * @param number the builder number
     * @return the component builder
     */
    static ComponentBuilder text(final byte number) {
        return new ComponentBuilder(number);
    }

    /**
     * Create a new component builder
     *
     * @param number the builder number
     * @return the component builder
     */
    static ComponentBuilder text(final short number) {
        return new ComponentBuilder(number);
    }

    /**
     * Create a new component builder
     *
     * @param number the builder number
     * @return the component builder
     */
    static ComponentBuilder text(final int number) {
        return new ComponentBuilder(number);
    }

    /**
     * Create a new component builder
     *
     * @param number the builder number
     * @return the component builder
     */
    static ComponentBuilder text(final long number) {
        return new ComponentBuilder(number);
    }

    /**
     * Create a new component builder
     *
     * @param number the builder number
     * @return the component builder
     */
    static ComponentBuilder text(final float number) {
        return new ComponentBuilder(number);
    }

    /**
     * Create a new component builder
     *
     * @param number the builder number
     * @return the component builder
     */
    static ComponentBuilder text(final double number) {
        return new ComponentBuilder(number);
    }

    /**
     * Create a new component builder
     *
     * @param bool the builder boolean
     * @return the component builder
     */
    static ComponentBuilder text(final boolean bool) {
        return new ComponentBuilder(bool);
    }

    /**
     * Get the component content
     *
     * @return the component content
     */
    String getContent();

    /**
     * Get the raw component. The raw component
     * is a one-line string containing all
     * the component and its children
     *
     * @return the raw component
     */
    String getRaw();

    /**
     * Get the children nodes of the
     * node
     *
     * @return the node children
     */
    Collection<? extends Component> getChildren();
}