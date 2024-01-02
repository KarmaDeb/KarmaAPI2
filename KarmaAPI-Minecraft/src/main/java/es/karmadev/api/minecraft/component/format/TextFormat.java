package es.karmadev.api.minecraft.component.format;

import es.karmadev.api.minecraft.component.Color;
import es.karmadev.api.minecraft.component.exception.FormattingException;

/**
 * Text format constant
 */
class TextFormat {

    private final static char TAG_OPEN = '<';
    private final static char TAG_CLOSE = '>';
    private final static char COMMON_COLOR = '&';
    private final static char MINECRAFT_COLOR = '§';

    /**
     * Parse a color tag
     *
     * @param text the text to parse
     * @return the parsed text
     * @throws FormattingException if there's a parser exception
     */
    public static String parseColorTag(final String text) throws FormattingException {
        return parse(text, true, false, false, false);
    }

    /**
     * Parse a style tag
     *
     * @param text the text to parse
     * @return the parsed text
     * @throws FormattingException if there's a parser exception
     */
    public static String parseStyleTag(final String text) throws FormattingException {
        return parse(text, false, false, true, false);
    }

    /**
     * Parse a color character
     *
     * @param text the text to parse
     * @return the parsed text
     */
    public static String parseColorChar(final String text) throws FormattingException {
        return parse(text, false, true, false, false);
    }

    /**
     * Parse a style character
     *
     * @param text the text to parse
     * @return the parsed text
     */
    public static String parseStyleChar(final String text) throws FormattingException {
        return parse(text, false, false, false, true);
    }

    /**
     * Parse all the possible styles for tags
     *
     * @param text the text to parse
     * @return the parsed text
     * @throws FormattingException if there's a parser exception
     */
    public static String parseAllTags(final String text) throws FormattingException {
        return parse(text, true, false, true, false);
    }

    /**
     * Parse all the possible styles for chars
     *
     * @param text the text to parse
     * @return the parsed text
     * @throws FormattingException if there's a parser exception
     */
    public static String parseAllChars(final String text) throws FormattingException {
        return parse(text, false, true, false, true);
    }

    /**
     * Parse all the possible styles
     *
     * @param text the text to parse
     * @return the parsed text
     * @throws FormattingException if there's a parser exception
     */
    public static String parseAll(final String text) throws FormattingException {
        return parse(text, true, true, true, true);
    }

    private static String parse(final String text,
                             final boolean tagColor, final boolean charColor,
                             final boolean tagStyle, final boolean charStyle) {
        StringBuilder builder = new StringBuilder();
        int length = text.length();

        boolean escape = false;

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);
            if (character == '\\') {
                escape = !escape;
                continue;
            }

            if (tagColor || tagStyle) {
                if (character == TAG_OPEN && !escape) {
                    int nextIndex = text.indexOf(TAG_CLOSE, i);
                    if (nextIndex > -1) {
                        processTag(tagColor, tagStyle, text.substring(i + 1, nextIndex), builder);
                        i = nextIndex;
                    }

                    continue;
                }

                if (character == COMMON_COLOR && !escape) {
                    if (i + 1 < length) {
                        char next = text.charAt(++i);
                        if (!parseContent(String.valueOf(next), charColor, charStyle, false, builder)) {
                            builder.append(COMMON_COLOR).append(next);
                        }
                    }

                    continue;
                }
            }

            builder.append(character);
            escape = false;
        }

        return builder.toString();
    }

    private static void processTag(final boolean tg, final boolean ts, final String tagContent, final StringBuilder builder) {
        if (parseContent(tagContent, tg, ts, true, builder)) {
            return;
        }

        builder.append(TAG_OPEN).append(tagContent).append(TAG_CLOSE);
    }

    private static boolean parseContent(final String content, final boolean color, final boolean style, final boolean tag, final StringBuilder builder) {
        char openChar = (tag ? TAG_OPEN : COMMON_COLOR);
        char closeChar = (tag ? TAG_CLOSE : '\0');

        if (color) {
            if (processColorFormat(content, builder, openChar, closeChar)) {
                return true;
            }
        }

        return style && processTagFormat(content, builder, openChar, closeChar);
    }

    private static boolean processColorFormat(final String content, final StringBuilder builder, final char charOpen, final char charClose) {
        Color resolved;
        if (content.startsWith("#")) {
            resolved = Color.fromHexString(content);
        } else {
            resolved = Color.getByName(content);
        }
        if (resolved == null && !content.isEmpty()) {
            resolved = Color.getByCode(content.charAt(0));
        }

        if (resolved != null) {
            builder.append(MINECRAFT_COLOR).append(resolved.getParsed());
            return true;
        }

        return false;
    }

    private static boolean processTagFormat(final String content, final StringBuilder builder, final char charOpen, final char charClose) {
        switch (content.toLowerCase()) {
            case "obfuscated":
            case "magic":
                builder.append(MINECRAFT_COLOR).append("k");
                break;
            case "bold":
                builder.append(MINECRAFT_COLOR).append("l");
                break;
            case "strikethrough":
                builder.append(MINECRAFT_COLOR).append("m");
                break;
            case "underline":
                builder.append(MINECRAFT_COLOR).append("n");
                break;
            case "italic":
                builder.append(MINECRAFT_COLOR).append("o");
                break;
            default:
                return false;
        }

        return true;
    }
}
