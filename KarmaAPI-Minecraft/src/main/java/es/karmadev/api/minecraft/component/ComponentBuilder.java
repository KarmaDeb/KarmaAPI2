package es.karmadev.api.minecraft.component;

import es.karmadev.api.minecraft.component.format.Formatting;
import es.karmadev.api.minecraft.component.node.NodeImpl;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a component builder, which helps
 * on building components
 */
public final class ComponentBuilder {

    private final NodeImpl parentNode;
    private Formatting formatting = Formatting.ALL;

    /**
     * Initialize the component builder
     */
    ComponentBuilder() {
        parentNode = new NodeImpl();
    }

    /**
     * Initialize the component builder
     *
     * @param initialContent the initial component content
     */
    ComponentBuilder(final @NonNull CharSequence initialContent) {
        parentNode = NodeImpl.fromText(initialContent.toString());
    }

    /**
     * Initialize the component builder
     *
     * @param number the initial component content
     */
    ComponentBuilder(final byte number) {
        parentNode = NodeImpl.fromText(String.valueOf(number));
    }

    /**
     * Initialize the component builder
     *
     * @param number the initial component content
     */
    ComponentBuilder(final short number) {
        parentNode = NodeImpl.fromText(String.valueOf(number));
    }

    /**
     * Initialize the component builder
     *
     * @param number the initial component content
     */
    ComponentBuilder(final int number) {
        parentNode = NodeImpl.fromText(String.valueOf(number));
    }

    /**
     * Initialize the component builder
     *
     * @param number the initial component content
     */
    ComponentBuilder(final long number) {
        parentNode = NodeImpl.fromText(String.valueOf(number));
    }

    /**
     * Initialize the component builder
     *
     * @param number the initial component content
     */
    ComponentBuilder(final float number) {
        parentNode = NodeImpl.fromText(String.valueOf(number));
    }

    /**
     * Initialize the component builder
     *
     * @param number the initial component content
     */
    ComponentBuilder(final double number) {
        parentNode = NodeImpl.fromText(String.valueOf(number));
    }

    /**
     * Initialize the component builder
     *
     * @param bool the initial component content
     */
    ComponentBuilder(final boolean bool) {
        parentNode = NodeImpl.fromText(String.valueOf(bool));
    }

    /**
     * Set the component builder format
     *
     * @param format the formatter
     * @return the builder
     */
    public ComponentBuilder format(final @Nullable Formatting format) {
        this.formatting = format;
        return this;
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder black() {
        return color(Color.BLACK);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder darkBlue() {
        return color(Color.DARK_BLUE);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder darkGreen() {
        return color(Color.DARK_GREEN);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder darkAqua() {
        return color(Color.DARK_AQUA);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder darkRed() {
        return color(Color.DARK_RED);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder darkPurple() {
        return color(Color.DARK_PURPLE);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder gold() {
        return color(Color.GOLD);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder gray() {
        return color(Color.GRAY);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder darkGray() {
        return color(Color.DARK_GRAY);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder blue() {
        return color(Color.BLUE);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder green() {
        return color(Color.GREEN);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder aqua() {
        return color(Color.AQUA);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder red() {
        return color(Color.RED);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder purple() {
        return color(Color.PURPLE);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder yellow() {
        return color(Color.YELLOW);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder white() {
        return color(Color.WHITE);
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder reset() {
        parentNode.append("<reset>");
        return this;
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder magic() {
        parentNode.append("<magic>");
        return this;
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder strikethrough() {
        parentNode.append("<strikethrough>");
        return this;
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder underlined() {
        parentNode.append("<underline>");
        return this;
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder italic() {
        parentNode.append("<italic>");
        return this;
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder bold() {
        parentNode.append("<bold>");
        return this;
    }

    /**
     * Append a color element to the
     * node
     *
     * @return the builder
     */
    public ComponentBuilder color(final Color color) {
        if (color.isCustom()) {
            parentNode.append("<" + color.getName().toLowerCase() + ">");
            return this;
        }

        int hex = color.getHex();
        parentNode.append("<#" + Integer.toHexString(hex) + ">");
        return this;
    }

    /**
     * Append an empty space to the
     * builder
     *
     * @return the builder
     */
    public ComponentBuilder space() {
        parentNode.append(" ");
        return this;
    }

    /**
     * Append a new line to the
     * builder
     *
     * @return the builder
     */
    public ComponentBuilder newLine() {
        parentNode.append("\n");
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param content the content
     * @return the builder
     */
    public ComponentBuilder text(final @NonNull CharSequence content) {
        parentNode.append(content);
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param number the content
     * @return the builder
     */
    public ComponentBuilder text(final byte number) {
        parentNode.append(number);
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param number the content
     * @return the builder
     */
    public ComponentBuilder text(final short number) {
        parentNode.append(number);
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param number the content
     * @return the builder
     */
    public ComponentBuilder text(final int number) {
        parentNode.append(number);
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param number the content
     * @return the builder
     */
    public ComponentBuilder text(final long number) {
        parentNode.append(number);
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param number the content
     * @return the builder
     */
    public ComponentBuilder text(final float number) {
        parentNode.append(number);
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param number the content
     * @return the builder
     */
    public ComponentBuilder text(final double number) {
        parentNode.append(number);
        return this;
    }

    /**
     * Append a text element into the
     * builder parent node
     *
     * @param bool the content
     * @return the builder
     */
    public ComponentBuilder text(final boolean bool) {
        parentNode.append(bool);
        return this;
    }

    /**
     * Append a builder into the
     * builder parent node
     * 
     * @param other the other builder
     * @return the builder
     */
    public ComponentBuilder append(final ComponentBuilder other) {
        return append(other.build());
    }

    /**
     * Append a component into the
     * builder parent node
     * 
     * @param other the other component
     * @return the builder
     */
    public ComponentBuilder append(final Component other) {
        this.parentNode.append(other);
        return this;
    }
    
    /**
     * Build the component
     * 
     * @return the component result
     */
    public Component build() {
        return null;
    }
}
