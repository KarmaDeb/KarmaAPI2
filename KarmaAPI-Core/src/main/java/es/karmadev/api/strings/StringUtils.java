package es.karmadev.api.strings;

import es.karmadev.api.array.ArrayUtils;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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

    private final static char[] DELIMITER_CHARACTERS = {
            ' ',
            '_'
    };

    /**
     * Parse a string into snake_case. This cannot detect words, so
     * it won't convert a string without spaces to snake_case.
     *
     * @param sequence the text
     * @return the snake_case string
     */
    public static String toSnakeCase(final CharSequence sequence) {
        return toSnakeCase(sequence, DELIMITER_CHARACTERS);
    }

    /**
     * Parse a string into camelCase. This cannot detect words, so
     * it won't convert a string without spaces to camelCase.
     *
     * @param sequence the text
     * @return the camelCase string
     */
    public static String toCamelCase(final CharSequence sequence) {
        return toCamelCase(sequence, DELIMITER_CHARACTERS);
    }

    /**
     * Parse a string into snake_case.
     *
     * @param sequence the text
     * @param delimiters the delimiters to use when
     *                   splitting the string
     * @return the snake_case string
     */
    public static String toSnakeCase(final CharSequence sequence, final char[] delimiters) {
        if (sequence == null) return "";
        String text = sequence.toString();

        if (!text.contains(" ")) return text;

        StringBuilder builder = new StringBuilder();
        char[] characters = text.toCharArray();

        char previousChar = '\0';
        for (char character : characters) {
            if (character == '_') character = ' ';

            if (previousChar == '\0') {
                previousChar = character;
            }

            if (ArrayUtils.contains(delimiters, character)) {
                if (ArrayUtils.contains(delimiters, previousChar)) continue;

                builder.append('_');
                continue;
            }

            if (Character.isLetterOrDigit(character)) {
                builder.append(Character.toLowerCase(character));
            }
        }

        return builder.toString();
    }

    /**
     * Parse a string into camelCase. This cannot detect words, so
     * it won't convert a string without spaces to camelCase.
     *
     * @param sequence the text
     * @param delimiters the delimiters to use when
     *                   splitting the string
     * @return the camelCase string
     */
    public static String toCamelCase(final CharSequence sequence, final char[] delimiters) {
        if (sequence == null) return "";
        String text = sequence.toString();

        if (!text.contains(" ")) return text;

        StringBuilder builder = new StringBuilder();
        char[] characters = text.toCharArray();

        boolean upper = false;
        for (char character : characters) {
            if (ArrayUtils.contains(delimiters, character)) {
                upper = true;
                continue;
            }

            if (Character.isLetterOrDigit(character)) {
                char value = Character.toLowerCase(character);
                if (upper) {
                    value = Character.toUpperCase(character);
                    upper = false;
                }

                builder.append(value);
            }
        }

        return builder.toString();
    }

    /**
     * Get if the strings starts with the
     * given string ignoring cases
     *
     * @param sequence the sequence to check if starts with
     * @param startsWith the content to check with
     * @return if the string starts with the given string
     */
    public static boolean startsWithIgnoreCase(final CharSequence sequence, final CharSequence startsWith) {
        if (sequence == null || startsWith == null) return false;
        if (startsWith.length() == sequence.length()) return ObjectUtils.equalsIgnoreCase(sequence, startsWith);
        //If both sequences are the same size, we can just compare

        if (startsWith.length() > sequence.length()) return false;

        for (int i = 0; i < startsWith.length(); i++) {
            char character = startsWith.charAt(i);
            char sequenceCharacter = sequence.charAt(i);
            if (Character.toLowerCase(character) != Character.toLowerCase(sequenceCharacter)) return false;
        }

        return true;
    }

    /**
     * Get if the strings ends with the
     * given string ignoring cases
     *
     * @param sequence the sequence to check if ends with
     * @param endsWith the content to check with
     * @return if the string ends with the given string
     */
    public static boolean endsWithIgnoreCase(final CharSequence sequence, final CharSequence endsWith) {
        if (sequence == null || endsWith == null) return false;
        if (endsWith.length() == sequence.length()) return ObjectUtils.equalsIgnoreCase(sequence, endsWith);
        //If both sequences are the same size, we can just compare

        if (endsWith.length() > sequence.length()) return false;

        int offset = sequence.length() - endsWith.length();
        for (int i = endsWith.length() - 1; i >= 0; i--) {
            char character = endsWith.charAt(i);
            char sequenceCharacter = sequence.charAt(offset + i);

            if (Character.toLowerCase(character) != Character.toLowerCase(sequenceCharacter)) return false;
        }

        return true;
    }

    /**
     * Get if the string contains the
     * given string ignoring cases
     *
     * @param sequence the sequence to check if contains
     * @param contains the content to check with
     * @return if the string contains the given string
     */
    public static boolean containsIgnoreCase(final CharSequence sequence, final CharSequence contains) {
        if (sequence == null || contains == null) return false;
        if (contains.length() == sequence.length()) return ObjectUtils.equalsIgnoreCase(sequence, contains);
        if (contains.length() > sequence.length()) return false;

        int matchIndex = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (matchIndex >= contains.length()) return true;

            char character = sequence.charAt(i);
            char containsCharacter = contains.charAt(matchIndex);

            if (Character.toLowerCase(character) == Character.toLowerCase(containsCharacter)) {
                matchIndex++;
            } else {
                matchIndex = 0;
            }
        }

        return matchIndex >= contains.length();
    }

    /**
     * Get the next word of a sequence
     *
     * @param sequence the sequence to iterate with
     * @return the next sequence word (starting from index 0)
     */
    public static String nextWord(final CharSequence sequence) {
        return nextWord(sequence, -1, -1);
    }

    /**
     * Get the next word of a sequence
     *
     * @param sequence the sequence to iterate with
     * @param startIndex the start index
     * @return the next sequence word
     */
    public static String nextWord(final CharSequence sequence, final int startIndex) {
        return nextWord(sequence, startIndex, -1);
    }

    /**
     * Get the next word of a sequence
     *
     * @param sequence the sequence to iterate with
     * @param startIndex the start index
     * @param maxIndex the max index
     * @return the next sequence word
     */
    public static String nextWord(final CharSequence sequence, final int startIndex, final int maxIndex) {
        if (sequence == null || sequence.length() == 0) return "";
        int start = Math.max(0, startIndex);
        int end = maxIndex;
        if (end < start) {
            end = sequence.length();
        }

        CharSequence subSequence = sequence.subSequence(start, end);
        StringBuilder builder = new StringBuilder();
        boolean building = false;
        for (int i = 0; i < subSequence.length(); i++) {
            char character = subSequence.charAt(i);
            if (character == ' ') {
                if (building) {
                    if (builder.length() > 0) {
                        break; //We've reached the end of the word
                    }

                    continue;
                }

                building = true;
                continue;
            }

            building = true; //At this point, the character is not a space, regardless if we just started iterating or not
            builder.append(character);
        }

        return builder.toString();
    }

    /**
     * Get all the matching strings in the
     * regex. For example; MY NAME IS *, will
     * return John if we call this method with "My name is John"
     * <p/>
     * #findMatches("MY NAME IS *", "My name is John", '*') => ["John"];
     *
     * @param regex the regex
     * @param text the text to find at
     * @param matchCharacter the matching character
     * @return the found matches
     */
    public static Collection<String> findMatches(final String regex, final String text, final char matchCharacter) {
        List<String> matches = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        int lastIndex = 0;
        List<String> groups = new ArrayList<>();
        for (int i = 0; i < regex.length(); i++) {
            char character = regex.charAt(i);
            if (character == '\\') {
                if (!escaped) {
                    escaped = true;
                } else {
                    i += 2;
                    escaped = false;
                }

                continue;
            }

            escaped = false;
            if (character == matchCharacter) {
                String groupName = "group" + (groups.size() + 1);
                builder.append(regex, lastIndex, i).append("(?<").append(groupName).append(">.*)");
                lastIndex = i + 1;

                groups.add(groupName);
            }
        }

        Pattern pattern = Pattern.compile(builder.toString(), Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                matches.add(matcher.group(i));
            }
        }

        return matches;
    }

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

            try {
                numbers.add(Short.parseShort(raw));
            } catch (NumberFormatException ex) {
                try {
                    numbers.add(Integer.parseInt(raw));
                } catch (NumberFormatException ex2) {
                    try {
                        numbers.add(Long.parseLong(raw));
                    } catch (NumberFormatException ex3) {
                        try {
                            numbers.add(Float.parseFloat(raw));
                        } catch (NumberFormatException ex4) {
                            try {
                                numbers.add(Double.parseDouble(raw));
                            } catch (NumberFormatException ignored) {}
                        }
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
