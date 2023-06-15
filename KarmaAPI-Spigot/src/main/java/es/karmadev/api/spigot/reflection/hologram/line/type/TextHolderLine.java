package es.karmadev.api.spigot.reflection.hologram.line.type;

import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;

/**
 * Hologram text line
 */
public interface TextHolderLine extends HologramLine {

    /**
     * Get the line text
     *
     * @return the text
     */
    String getText();

    /**
     * Set the line text
     *
     * @param text the text
     */
    void setText(final String text);
}
