package es.karmadev.api.minecraft.color;

import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.strings.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Minecraft color component
 */
public final class ColorComponent {

    /**
     * Parse the color
     *
     * @param text the text
     * @param replaces the text replaces
     * @return the colored text
     */
    public static String parse(final String text, final Object... replaces) {
        return StringUtils.format(text, replaces).replace('&', '§');
    }

    /**
     * Parse colors of the array
     *
     * @param messages the array
     * @return the colored texts
     */
    public static String[] parse(final String[] messages) {
        String[] newArray = new String[messages.length];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = parse(messages[i]);
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
    public static <T extends Collection<String>> T parse(final T collection, final Supplier<T> collectionSupplier) {
        T array = collectionSupplier.get();
        for (String element : collection) {
            array.add(parse(element));
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
    public static String strip(final String text, final Object... replaces) {
        return ConsoleColor.strip(StringUtils.format(text, replaces));
    }

    /**
     * Strip colors of the array
     *
     * @param messages the array
     * @return the color stripped texts
     */
    public static String[] strip(final String[] messages) {
        String[] newArray = new String[messages.length];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = strip(messages[i]);
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
    public static <T extends Collection<String>> T strip(final T collection, final Supplier<T> collectionSupplier) {
        T array = collectionSupplier.get();
        for (String element : collection) {
            array.add(strip(element));
        }

        return array;
    }
}
