package es.karmadev.api.minecraft.component.node;

import es.karmadev.api.minecraft.component.Component;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a node component. A node
 * is nothing but a generic representation
 * which represents itself a component.
 * The node is an immutable type. This means that
 * to modify it, it must be as a new
 * instance except for the {@link #append(Component) append}
 * method, which modified the instance
 */
public class NodeImpl implements Component {

    private final String content;
    private final String raw;

    private final Set<Component> children = new LinkedHashSet<>();

    /**
     * Create a node from raw text
     *
     * @param rawContent the raw content
     * @return the node instance
     */
    public static NodeImpl fromText(final String rawContent) {
        String content = rawContent;
        return new NodeImpl(content, content);
    }

    /**
     * Create an empty node
     */
    public NodeImpl() {
        this("", "");
    }

    /**
     * Create a node with the
     * specified content
     *
     * @param content the node content
     * @param raw the raw element
     */
    private NodeImpl(final @NonNull CharSequence content, final @NonNull CharSequence raw) {
        this.content = content.toString();
        this.raw = raw.toString();
    }

    /**
     * Get the node content
     *
     * @return the node content
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * Get the raw component. The raw component
     * is a one-line string containing all
     * the node and its children
     *
     * @return the raw component
     */
    @Override
    public String getRaw() {
        return raw;
    }

    /**
     * Append a node to the current
     * node
     *
     * @param other the node to append
     * @return the node
     */
    @Override
    public NodeImpl append(final Component other) {
        if (other == null || other.equals(this)) return this;
        children.add(other);

        return this;
    }

    /**
     * Get the children nodes of the
     * node
     *
     * @return the node children
     */
    @Override
    public Collection<? extends Component> getChildren() {
        return Collections.unmodifiableSet(children);
    }
}
