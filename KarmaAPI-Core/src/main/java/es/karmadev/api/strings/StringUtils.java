package es.karmadev.api.strings;

import java.util.*;

/**
 * String utilities
 */
public class StringUtils {

    private final static char[][] RANDOM_CHARACTERS = {
            {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'},
            {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',  'Y', 'Z'},
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}
    };

    /**
     * Replace the last matching string
     * regex by the specified replacement
     *
     * @param text the text to replace
     * @param regex the regex to find for
     * @param replacement the replacement
     * @return the replaced string
     */
    public static String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    /**
     * Parses a collection of strings into a
     * single string
     *
     * @param list the collection of strings
     * @param spacer the spacer to use in each item
     * @return the parsed string
     */
    public static String listToString(final Collection<String> list, final ListSpacer spacer) {
        char[] spacerChar = {'\0'};

        switch (spacer) {
            case COMMA:
                spacerChar = new char[]{',', ' '};
                break;
            case NEW_LINE:
                spacerChar[0] = '\n';
                break;
            case NONE:
            default:
                break;
        }

        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (String item : list) {
            builder.append(item);
            if (index++ <= list.size() - 1) {
                builder.append(spacerChar);
            }
        }

        return builder.toString();
    }

    /**
     * Parses an array of strings into a
     * single string
     *
     * @param array the array of strings
     * @param spacer the spacer to use in each item
     * @return the parsed string
     */
    public static String listToString(final String[] array, final ListSpacer spacer) {
        char[] spacerChar = {'\0'};

        switch (spacer) {
            case COMMA:
                spacerChar = new char[]{',', ' '};
                break;
            case NEW_LINE:
                spacerChar[0] = '\n';
                break;
            case NONE:
            default:
                break;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i <= array.length - 1) {
                builder.append(spacerChar);
            }
        }

        return builder.toString();
    }

    /**
     * Insert a character in each period of
     * characters
     *
     * @param text the text to insert to
     * @param insert the text to insert in
     * @param period the period of characters
     *               to insert between
     * @return the modified text
     */
    public static String insertInEach(final String text, final String insert, final int period) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (char character : text.toCharArray()) {
            if (index++ % period == 0 && index != 1) {
                builder.append(insert);
            }
            builder.append(character);
        }

        return builder.toString();
    }

    /**
     * Generate a random string
     *
     * @return the generated string
     */
    public static String generateString() {
        return generateString(16);
    }

    /**
     * Generate a random string
     *
     * @param options the string options
     * @return the generated string
     */
    public static String generateString(final StringOptions... options) {
        return generateString(16, options);
    }

    /**
     * Generate a random string
     *
     * @param length the string length
     * @param options the string options
     * @return the generated string
     */
    public static String generateString(int length, final StringOptions... options) {
        if (length > Short.MAX_VALUE) length = Short.MAX_VALUE;

        char[] lowerCase = RANDOM_CHARACTERS[0];
        char[] upperCase = RANDOM_CHARACTERS[1];
        char[] numbers = RANDOM_CHARACTERS[2];

        List<StringOptions> optionsList = Arrays.asList(options);
        if (optionsList.isEmpty()) optionsList = Arrays.asList(StringOptions.values()); //We cannot generate empty strings

        int mixedLength = 0;
        int part1 = 0;
        int part2 = 0;
        if (optionsList.contains(StringOptions.LOWERCASE)) {
            mixedLength += lowerCase.length;
            part1 = lowerCase.length;
            part2 = lowerCase.length;
        }
        if (optionsList.contains(StringOptions.UPPERCASE)) {
            mixedLength += upperCase.length;
            part2 = lowerCase.length + part1;
        }
        if (optionsList.contains(StringOptions.NUMBERS)) {
            mixedLength += numbers.length;
        }

        char[] mixed = new char[mixedLength];
        if (optionsList.contains(StringOptions.LOWERCASE))
            System.arraycopy(lowerCase, 0, mixed, 0, lowerCase.length);

        if (optionsList.contains(StringOptions.UPPERCASE))
            System.arraycopy(upperCase, 0, mixed, part1, upperCase.length);

        if (optionsList.contains(StringOptions.NUMBERS))
            System.arraycopy(numbers, 0, mixed, part2, numbers.length);

        StringBuilder builder = new StringBuilder();
        for (short i = 0; i < length; i++) {
            int random = new Random().nextInt(mixed.length);
            builder.append(mixed[random]);
        }

        return builder.toString();
    }


}
