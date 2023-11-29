package es.karmadev.api.minecraft.text;

import es.karmadev.api.strings.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minecraft color component
 */
public final class Colorize {

    public final static char ALTERNATE_COLOR_CODE = '&';
    private final static Colorize COMMON = new Colorize(ALTERNATE_COLOR_CODE);

    private final char code;
    private final Pattern colorPattern;

    /**
     * Initialize the colorize
     * utility
     *
     * @param code the code
     */
    public Colorize(final char code) {
        this.code = code;
        String codeStr = String.valueOf(code);
        colorPattern = Pattern.compile(StringUtils.escape(codeStr) + "[0-9a-fkormn]");
    }

    /**
     * Parse the string into a colored
     * string replacing the alternate color
     * code with the minecraft color code
     *
     * @param text the text to colorize
     * @param replaces the message replaces
     * @return the colorized text
     */
    public String toColor(final String text, final Object... replaces) {
        if (text == null) return null;
        return StringUtils.format(text, replaces).replace(code, '§');
    }

    /**
     * Strip the colors from the given
     * text using the alternate color
     * code
     *
     * @param text the text to remove colors from
     * @param replaces the text replaces
     * @return the un-formatted text
     */
    public String stripColor(final String text, final Object... replaces) {
        if (text == null) return null;
        String check = text.replace('§', code);
        Matcher matcher = colorPattern.matcher(check);

        StringBuilder builder = new StringBuilder();
        int source = 0;
        boolean parsed = false;

        while (matcher.find()) {
            parsed = true;
            int start = matcher.start();
            int end = matcher.end();

            builder.append(text, source, start);
            source = end;
        }

        if (parsed) {
            if (source < text.length()) {
                builder.append(text, source, text.length());
            }
        } else {
            builder.append(text);
        }

        return StringUtils.format(builder, replaces);
    }

    /**
     * Parse the color
     *
     * @param text the text
     * @param replaces the text replaces
     * @return the colored text
     */
    public static String colorize(final String text, final Object... replaces) {
        return COMMON.toColor(text, replaces);
    }

    /**
     * Parse colors of the array
     *
     * @param messages the array
     * @return the colored texts
     */
    public static String[] colorize(final String[] messages) {
        String[] newArray = new String[messages.length];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = colorize(messages[i]);
        }

        return newArray;
    }

    /**
     * Parse colors of the collection
     *
     * @param collection the collection to parse
     * @param collectionSupplier the collection object supplier
     * @return the colored collection
     * @param <T> the collection object
     */
    public static <T extends Collection<String>> T colorize(final T collection, final Supplier<T> collectionSupplier) {
        T array = collectionSupplier.get();
        for (String element : collection) {
            array.add(colorize(element));
        }

        return array;
    }

    /**
     * Strip the color
     *
     * @param text the text
     * @param replaces the text replaces
     * @return the color stripped text
     */
    public static String unColorize(final String text, final Object... replaces) {
        return COMMON.stripColor(text, replaces);
    }

    /**
     * Strip colors of the array
     *
     * @param messages the array
     * @return the color stripped texts
     */
    public static String[] unColorize(final String[] messages) {
        String[] newArray = new String[messages.length];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = unColorize(messages[i]);
        }

        return newArray;
    }

    /**
     * Strip colors of the collection
     *
     * @param collection the collection to parse
     * @param collectionSupplier the collection object supplier
     * @return the color stripped collection
     * @param <T> the collection object
     */
    public static <T extends Collection<String>> T unColorize(final T collection, final Supplier<T> collectionSupplier) {
        T array = collectionSupplier.get();
        for (String element : collection) {
            array.add(unColorize(element));
        }

        return array;
    }
}
