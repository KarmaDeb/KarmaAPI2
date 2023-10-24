package es.karmadev.api.strings;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String utilities
 */
@SuppressWarnings("unused")
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
    public static String generateString(final int length, final StringOptions... options) {
        char[] string = new char[Math.max(1, Math.min(Short.MAX_VALUE, length))];

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

        Random random = new Random();
        for (short i = 0; i < string.length; i++) {
            int rnd = random.nextInt(mixed.length);
            string[i] = mixed[rnd];
        }

        return new String(string);
    }

    /**
     * Generate a separated string
     *
     * @return the split string
     */
    public static String generateSplit() {
        return generateSplit(32, '-');
    }

    /**
     * Generate a separated string
     *
     * @param splitter the split character
     * @return the split string
     */
    public static String generateSplit(final char splitter) {
        return generateSplit(32, splitter);
    }

    /**
     * Generate a separated string
     *
     * @param length the string length
     * @return the split string
     */
    public static String generateSplit(final int length) {
        return generateSplit(length, '-');
    }

    /**
     * Generate a separated string
     *
     * @param length the string length
     * @param splitter the split character
     * @return the split string
     */
    public static String generateSplit(final int length, final char splitter) {
        String random = generateString(Math.max(length, 12), StringOptions.UPPERCASE, StringOptions.NUMBERS);
        if (splitter == '\0') return random;

        int splitEach = Math.max(Math.max(length, 12) / 6, 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < random.length(); i++) {
            if (i != 0 && i % splitEach == 0) {
                builder.append(splitter);
            }

            builder.append(random.charAt(i));
        }

        return builder.toString();
    }

    /**
     * Format a string
     *
     * @param sequence the text to format
     * @param replaces the replaces
     * @return the formatted text
     */
    public static String format(final CharSequence sequence, final Object... replaces) {
        if (sequence == null) return "";

        String str = sequence.toString();
        for (int i = 0; i < replaces.length; i++) {
            str = str.replace("{" + i + "}", String.valueOf(replaces[i]));
        }

        APISource kore = KarmaKore.INSTANCE();
        if (kore != null) {
            PlaceholderEngine engine = kore.placeholderEngine("default");
            str = engine.parse(str);
        }

        return str;
    }

    /**
     * Format a string
     *
     * @param sequence the text to format
     * @param replaces the replaces
     * @return the formatted text
     */
    public static String format(final CharSequence sequence, final Map<String, Object> replaces) {
        MapEntry[] entries = new MapEntry[replaces.size()];
        int index = 0;
        for (Map.Entry<String, Object> entry : replaces.entrySet()) {
            entries[index++] = MapEntry.fromEntry(entry);
        }

        return format(sequence, entries);
    }

    /**
     * Format a string
     *
     * @param sequence the text to format
     * @param replaces the replaces
     * @return the formatted text
     */
    @SafeVarargs
    public static String format(final CharSequence sequence, final Map.Entry<String, Object>... replaces) {
        if (sequence == null) return "";

        String str = sequence.toString();
        for (Map.Entry<String, Object> entry : replaces) {
            str = str.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }

        APISource kore = KarmaKore.INSTANCE();
        if (kore != null) {
            PlaceholderEngine engine = kore.placeholderEngine("default");
            str = engine.parse(str);
        }

        return str;
    }

    /**
     * Escape the text
     *
     * @param sequence the text to scape
     * @param ignore the characters to not scape
     * @return the scape text
     */
    public static String escape(final CharSequence sequence, final char... ignore) {
        StringBuilder builder = new StringBuilder();
        List<Character> ignoreChars = new ArrayList<>();
        for (char character : ignore) ignoreChars.add(character);

        for (int i = 0; i < sequence.length(); i++) {
            char character = sequence.charAt(i);
            if (ignoreChars.contains(character)) {
                builder.append(character);
                continue;
            }

            switch (character) {
                case '$':
                case '(':
                case ')':
                case '*':
                case '+':
                case '-':
                case '.':
                case '?':
                case '[':
                case ']':
                case '^':
                case '{':
                case '|':
                case '}':
                    builder.append("\\").append(character);
                    break;
                default:
                    builder.append(character);
                    break;
            }
        }

        return builder.toString();
    }

    /**
     * Serialize an object without knowing its
     * type
     *
     * @param instance the object instance
     * @return the serialized instance
     */
    public static String serializeUnsafe(final Object instance) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream ous = new ObjectOutputStream(bos)) {
            ous.writeObject(instance);
            ous.flush();

            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Throwable ex) {
            ExceptionCollector.catchException(StringUtils.class, ex);
            return "";
        }
    }

    /**
     * Serialize an object into a text
     *
     * @param instance the object
     * @return the serialized instance
     * @param <NativeObject> the object type
     * @param <Object> the object to serialize
     */
    public static <NativeObject extends Serializable, Object extends NativeObject> String serialize(final Object instance) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream ous = new ObjectOutputStream(bos)) {
            ous.writeObject(instance);
            ous.flush();

            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Throwable ex) {
            ExceptionCollector.catchException(StringUtils.class, ex);
            return "";
        }
    }

    /**
     * Load a serialized object
     *
     * @param instance the string instance
     * @return the resolved object
     */
    public static Optional<Object> load(final String instance) {
        if (instance == null) return Optional.empty();
        Object loaded = null;

        try {
            byte[] bytes = Base64.getDecoder().decode(instance);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bis)) {
                loaded = ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                ExceptionCollector.catchException(StringUtils.class, ex);
            }
        } catch (IllegalArgumentException ex) {
            ExceptionCollector.catchException(StringUtils.class, ex);
        }

        return Optional.ofNullable(loaded);
    }

    /**
     * Load a serialized object
     *
     * @param instance the string instance
     * @param <T> the object type
     * @return the resolved object
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable T loadAndCast(final String instance) {
        if (instance == null) return null;
        T loaded = null;

        try {
            byte[] bytes = Base64.getDecoder().decode(instance);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bis)) {
                loaded = (T) ois.readObject();
            } catch (IOException | ClassNotFoundException | ClassCastException ex) {
                ExceptionCollector.catchException(StringUtils.class, ex);
            }
        } catch (IllegalArgumentException ex) {
            ExceptionCollector.catchException(StringUtils.class, ex);
        }

        return loaded;
    }

    /**
     * Get if the text contains any letter
     *
     * @param sequence the text
     * @return if the text contains any letter
     */
    public static boolean containsLetter(final CharSequence sequence) {
        if (sequence == null) return false;
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(sequence);

        return matcher.find();
    }

    /**
     * Get if the text contains any number
     *
     * @param sequence the text
     * @return if the text contains any number
     */
    public static boolean containsNumber(final CharSequence sequence) {
        if (sequence == null) return false;
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(sequence);

        return matcher.find();
    }

    /**
     * Extract the numbers from the text
     *
     * @param sequence the text
     * @return the text numbers
     */
    public static Number[] extractNumbers(final CharSequence sequence) {
        if (sequence == null) return new Number[0];

        List<Number> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("-?(?:\\d+[.,]?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?");
        Matcher matcher = pattern.matcher(sequence);

        while (matcher.find()) {
            String raw = matcher.group();
            if (raw.contains(",") || raw.contains(".")) {
                try {
                    numbers.add(Float.parseFloat(raw.replace(",", "")));
                } catch (NumberFormatException ex) {
                    numbers.add(Double.parseDouble(raw.replace(",", "")));
                }
            } else {
                try {
                    numbers.add(Short.parseShort(raw));
                } catch (NumberFormatException ex) {
                    try {
                        numbers.add(Integer.parseInt(raw));
                    } catch (NumberFormatException ex2) {
                        numbers.add(Long.parseLong(raw));
                    }
                }
            }
        }

        return numbers.toArray(new Number[0]);
    }

    /**
     * Remove the numbers from the text, this can be
     * considered as the "reverse" method of {@link StringUtils#extractNumbers(CharSequence)}
     *
     * @param sequence the text
     * @return the text without numbers
     */
    public static String removeNumbers(final CharSequence sequence) {
        if (sequence == null) return "";
        List<String> numbers = new ArrayList<>();

        Pattern pattern = Pattern.compile("-?(?:\\d+[.,]?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?");
        Matcher matcher = pattern.matcher(sequence);

        while (matcher.find()) {
            numbers.add(matcher.group());
        }

        String modified = sequence.toString();
        for (String number : numbers) {
            modified = modified.replace(number, "");
        }

        return modified;
    }

    /**
     * Split the string when it finds the other
     * text
     *
     * @param sequence the text
     * @param other the text to stop at
     * @return the split string
     */
    public static String splitAt(final CharSequence sequence, final CharSequence other) {
        if (sequence == null || other == null) return (sequence != null ? sequence.toString() : "");

        String str = sequence.toString();
        String dst = other.toString();
        String dstEscaped = escape(dst);

        if (str.contains(dst)) {
            String[] data = str.split(dstEscaped);
            return data[0];
        }

        return str;
    }

    /**
     * Shuffle the specified strings
     *
     * @param strings the strings
     * @return the shuffled strings
     */
    public static String shuffle(final String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String str : strings) {
            builder.append(str);
        }

        Random random = new Random();
        char[] chars = builder.toString().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}

class MapEntry implements Map.Entry<String, Object> {

    private final String key;
    private final Object value;

    MapEntry(final String key, final Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key corresponding to this entry.
     *
     * @return the key corresponding to this entry
     * @throws IllegalStateException implementations may, but are not
     *                               required to, throw this exception if the entry has been
     *                               removed from the backing map.
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * Returns the value corresponding to this entry.  If the mapping
     * has been removed from the backing map (by the iterator's
     * <tt>remove</tt> operation), the results of this call are undefined.
     *
     * @return the value corresponding to this entry
     * @throws IllegalStateException implementations may, but are not
     *                               required to, throw this exception if the entry has been
     *                               removed from the backing map.
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation).  (Writes through to the map.)  The
     * behavior of this call is undefined if the mapping has already been
     * removed from the map (by the iterator's <tt>remove</tt> operation).
     *
     * @param value new value to be stored in this entry
     * @return old value corresponding to the entry
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *                                       is not supported by the backing map
     * @throws ClassCastException            if the class of the specified value
     *                                       prevents it from being stored in the backing map
     * @throws NullPointerException          if the backing map does not permit
     *                                       null values, and the specified value is null
     * @throws IllegalArgumentException      if some property of this value
     *                                       prevents it from being stored in the backing map
     * @throws IllegalStateException         implementations may, but are not
     *                                       required to, throw this exception if the entry has been
     *                                       removed from the backing map.
     */
    @Override
    public Object setValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    public static MapEntry fromEntry(final Map.Entry<String, Object> entry) {
        return new MapEntry(entry.getKey(), entry.getValue());
    }
}
