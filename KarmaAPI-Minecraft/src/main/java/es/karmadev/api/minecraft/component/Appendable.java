package es.karmadev.api.minecraft.component;

import es.karmadev.api.minecraft.component.node.NodeImpl;
import lombok.NonNull;

public interface Appendable<T extends Component> {

    /**
     * Append a node to the current
     * node
     *
     * @param other the node to append
     * @return the node
     */
    T append(final Component other);

    /**
     * Append raw text to the node
     *
     * @param content the node content
     * @return the node
     */
    default T append(final @NonNull CharSequence content) {
        return append(NodeImpl.fromText(content.toString()));
    }

    /**
     * Append a number to the node
     *
     * @param number the number content
     * @return the node
     */
    default T append(final byte number) {
        return append(String.valueOf(number));
    }

    /**
     * Append a number to the node
     *
     * @param number the number content
     * @return the node
     */
    default T append(final short number) {
        return append(String.valueOf(number));
    }

    /**
     * Append a number to the node
     *
     * @param number the number content
     * @return the node
     */
    default T append(final int number) {
        return append(String.valueOf(number));
    }

    /**
     * Append a number to the node
     *
     * @param number the number content
     * @return the node
     */
    default T append(final long number) {
        return append(String.valueOf(number));
    }

    /**
     * Append a number to the node
     *
     * @param number the number content
     * @return the node
     */
    default T append(final float number) {
        return append(String.valueOf(number));
    }

    /**
     * Append a number to the node
     *
     * @param number the number content
     * @return the node
     */
    default T append(final double number) {
        return append(String.valueOf(number));
    }

    /**
     * Append a boolean to the node
     *
     * @param bool the boolean content
     * @return the node
     */
    default T append(final boolean bool) {
        return append(String.valueOf(bool));
    }
}
