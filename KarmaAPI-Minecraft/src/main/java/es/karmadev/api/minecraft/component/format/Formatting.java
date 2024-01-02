package es.karmadev.api.minecraft.component.format;

import es.karmadev.api.minecraft.component.exception.FormattingException;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a formatting for a
 * text element. The formatting controls
 * such things as color or text style
 */
@FunctionalInterface
public interface Formatting {

    /**
     * Process all the default formatting
     * in a single iteration. The difference
     * between this method and concatenating
     * all the formats with the {@link #and(Formatting) and}
     * method is that this should be faster as
     * everything is iterated a single time
     */
    Formatting ALL = TextFormat::parseAll;

    /**
     * Default formatting for color chars. This
     * format style will replace & characters
     * with minecraft-compatible § character
     */
    Formatting COLOR_CODES = TextFormat::parseStyleChar;

    /**
     * Default formatting for style chars. This
     * format style will replace & characters
     * with minecraft-compatible § character
     */
    Formatting STYLE_CODES = TextFormat::parseColorChar;

    /**
     * Default formatting for color tags. This
     * format style will replace mime tags which
     * contain a valid color with its minecraft
     * result.
     */
    Formatting COLOR_TAGS = TextFormat::parseColorTag;

    /**
     * Default formatting for style tags. This
     * format style will replace mime tags which
     * contain a valid style with its minecraft
     * result.
     */
    Formatting STYLE_TAGS = TextFormat::parseStyleTag;

    /**
     * Default formatting for all tags.
     */
    Formatting TAGS = TextFormat::parseAllTags;

    /**
     * Default formatting for all chars.
     */
    Formatting CODES = TextFormat::parseAllChars;

    /**
     * Format the text. The return value
     * of this method should be the formatted
     * string
     *
     * @param string the non-formatted string
     * @return the formatted string
     * @throws FormattingException if the format process
     * fails. Alternatively, implementations may choose to
     * fail silently and keep processing the string
     */
    String format(final String string) throws FormattingException;

    /**
     * Format the text. The return value of
     * this method should be the formatted
     * string
     *
     * @param string the non-formatted string
     * @param replacements the text placeholder map
     * @return the formatted string
     * @throws FormattingException if the format process
     * fails. Alternatively, implementations may choose to
     * fail silently and keep processing the string
     */
    default String format(final String string, final Map<String, Object> replacements) throws FormattingException {
        if (replacements == null || replacements.isEmpty()) return format(string);
        StringBuilder parsedBuilder = new StringBuilder(string);

        for (String key : replacements.keySet()) {
            Object value = replacements.get(key);
            if (value == null) continue;

            int index = parsedBuilder.indexOf("$" + key);
            while (index != -1) {
                parsedBuilder.replace(index, index + key.length(), String.valueOf(value));
                index = parsedBuilder.indexOf(key, index + 1);
            }
        }

        return format(parsedBuilder.toString());
    }

    /**
     * Merge another formatting style
     * with the current one, allowing
     * to create multi-formatting on
     * a single format style
     *
     * @param other the format style to
     *              bind with
     * @return the bind formatter
     */
    default Formatting and(final Formatting other) {
        return (text) -> format(other.format(text));
    }
}
