package es.karmadev.api.logger.log.console;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Console colors
 */
@SuppressWarnings("unused")
public enum ConsoleColor {
    /**
     * Console color
     */
    BLACK('0', "\033[0;38;2;0;0;0m", "\u001B[30m"),
    /**
     * Console color
     */
    DARK_BLUE('1', "\033[0;38;2;0;0;128m", "\u001B[34m"),
    /**
     * Console color
     */
    DARK_GREEN('2', "\033[0;38;2;0;128;0m", "\u001B[32m"),
    /**
     * Console color
     */
    DARK_AQUA('3', "\033[0;38;2;0;128;128m", "\u001B[36m"),
    /**
     * Console color
     */
    DARK_RED('4', "\033[0;38;2;128;0;0m", "\u001B[31m"),
    /**
     * Console color
     */
    DARK_PURPLE('5', "\033[0;38;2;128;0;128m", "\u001B[35m"),
    /**
     * Console color
     */
    DARK_YELLOW('6', "\033[0;38;2;128;128;0m", "\u001B[33m"),
    /**
     * Console color
     */
    GRAY('7', "\033[0;38;2;192;192;192m", "\u001B[37m"),
    /**
     * Console color
     */
    DARK_GRAY('8', "\033[0;38;2;128;128;128m", "\u001B[90m"),
    /**
     * Console color
     */
    BLUE('9', "\033[0;38;2;0;95;255m", "\u001B[94m"),
    /**
     * Console color
     */
    GREEN('a', "\033[0;38;2;0;255;0m", "\u001B[92m"),
    /**
     * Console color
     */
    AQUA('b', "\033[0;38;2;0;255;255m", "\u001B[96m"),
    /**
     * Console color
     */
    RED('c', "\033[0;38;2;255;0;0m", "\u001B[91m"),
    /**
     * Console color
     */
    PURPLE('d', "\033[0;38;2;255;0;255m", "\u001B[95m"),
    /**
     * Console color
     */
    YELLOW('e', "\033[0;38;2;255;255;0m", "\u001B[93m"),
    /**
     * Console color
     */
    WHITE('f', "\033[0;38;2;255;255;255m", "\u001B[97m"),
    /**
     * Console color
     */
    RESET('r', "\033[0m", "\u001B[0m"),
    /**
     * Console color
     */
    BOLD('l', "\033[1m", "\u001B[1m"),
    /**
     * Console color
     */
    UNDERLINE('n', "\033[4m", "\u001B[4m"),
    /**
     * Console color
     */
    ITALIC('o', "\033[3m", "\u001B[3m"),
    /**
     * Console color
     */
    STRIKETHROUGH('m', "\033[9m", "\u001B[9m");

    @SuppressWarnings("NonFinalFieldInEnum")
    public static boolean forceOtherOs = false;
    @SuppressWarnings("NonFinalFieldInEnum")
    public static char ignoreCharacter = '\\';

    @Getter
    private final char code;
    @Getter
    private final char colorCode;
    @Getter
    private final String unixCode;
    @Getter
    private final String winCode;

    private final static Map<Character, String> winCodes = new ConcurrentHashMap<>();
    private final static Map<Character, String> unixCodes = new ConcurrentHashMap<>();

    private final static Map<Character, ConsoleColor> codes = new ConcurrentHashMap<>();


    static {
        for (ConsoleColor color : ConsoleColor.values()) {
            codes.put(color.code, color);
            winCodes.put(color.code, color.winCode);
            unixCodes.put(color.code, color.unixCode);
        }
    }

    /**
     * Initialize the console color
     *
     * @param code the color code
     * @param unix the unix code
     * @param win the windows code
     */
    ConsoleColor(final char code, final String unix, final String win) {
        String os = System.getProperty("os.name").toLowerCase();

        this.code = code;
        this.colorCode = (os.contains("windows") ? '\u001B' : '\033');
        unixCode = unix;
        winCode = win;
    }

    /**
     * Get the code
     *
     * @return the code
     */
    public String toColorCode() {
        return toColorCode('§');
    }

    /**
     * Get the code
     *
     * @param id the code escaper, by default §
     * @return the code
     */
    public String toColorCode(final char id) {
        return String.valueOf(id) + code;
    }

    /**
     * Get the code
     *
     * @return the OS code
     */
    public String toOsCode() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return (forceOtherOs ? unixCodes.get(code) : winCodes.get(code));
        } else {
            return  (forceOtherOs ? winCodes.get(code) : unixCodes.get(code));
        }
    }

    /**
     * Get the console color from the
     * color code
     *
     * @param code the code
     * @return the color from the code
     */
    public ConsoleColor fromCode(final char code) {
        return codes.getOrDefault(code, null);
    }

    /**
     * Print a colored message
     *
     * @param message the message to print
     */
    public void print(final String message) {
        String uncolored = toColorCode() + strip(message);
        PlaceholderEngine engine = KarmaKore.INSTANCE().placeholderEngine("default");
        String placeholderParsed = engine.parse(uncolored);

        System.out.println(placeholderParsed);
    }

    /**
     * Parse colors of the array
     *
     * @param messages the array
     * @return the parsed collection
     */
    public static String[] parse(final String... messages) {
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
     * @return the parsed collection
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
     * Parse colors of the string
     *
     * @param message the message
     * @return the parsed message
     */
    public static String parse(final String message) {
        String os = System.getProperty("os.name").toLowerCase();
        String str = message.replace("§", "&") + "&r";

        //Pattern pattern = Pattern.compile("&[0-9a-flrnom]");
        //Map<String, String> replacements = new HashMap<>();

        //Matcher matcher = pattern.matcher(str);

        StringBuilder builder = new StringBuilder();
        boolean ignoreNext = false;
        for (int i = 0; i < str.length(); i++) {
            char character = str.charAt(i);
            String append = null;

            if (character == '&') {
                if (!ignoreNext) {
                    char next = '\0';
                    boolean hasNext = false;
                    if (i + 1 < str.length()) {
                        next = str.charAt(i + 1);
                        hasNext = true;
                    }

                    if (hasNext) {
                        if (codes.containsKey(next)) {
                            ConsoleColor color = codes.get(next);
                            append = color.toOsCode();
                            i++;
                        }
                    }
                }

                ignoreNext = false;
            }
            if (character == ignoreCharacter) {
                ignoreNext = true;
                append = ""; //append nothing
            }
            if (append == null) append = String.valueOf(character);

            builder.append(append);
        }

        /*while (matcher.find()) {
            int pre = Math.max(matcher.start() - 1, 0);
            int start = matcher.start();
            int end = matcher.end();

            if (start != pre) {
                char character = str.charAt(pre);
                if (character == ignoreCharacter) {
                    String part = str.substring(pre, end);
                    replacements.put(part, str.substring(start, end));
                    continue;
                }
            }

            String part = str.substring(start, end);
            char code = part.charAt(1);

            String osCode;
            if (os.contains("windows")) {
                osCode = (forceOtherOs ? unixCodes.get(code) : winCodes.get(code));
            } else {
                osCode = (forceOtherOs ? winCodes.get(code) : unixCodes.get(code));
            }


        }

        for (String key : replacements.keySet()) {
            String value = replacements.get(key);
            str = str.replace(key, value);
        }

        return str;*/
        return builder.toString();
    }

    /**
     * Strip all the color codes from the string
     *
     * @param message the message
     * @return the uncolored message
     */
    public static String strip(final String message) {
        String str = message;
        if (str.contains("§")) {
            str = str.replace("§", "&");
        }

        Pattern pattern = Pattern.compile("&[0-9a-flrnom]");
        Matcher matcher = pattern.matcher(str);

        Map<String, String> parts = new ConcurrentHashMap<>();
        while (matcher.find()) {
            int pre = Math.max(matcher.start() - 1, 0);
            int start = matcher.start();
            int end = matcher.end();

            if (start != pre) {
                char character = str.charAt(pre);
                if (character == ignoreCharacter) {
                    String part = str.substring(pre, end);
                    parts.put(part, str.substring(start, end));
                    continue;
                }
            }

            String part = str.substring(start, end);
            parts.put(part, "");
        }

        for (String unixValue : unixCodes.values()) str = str.replace(unixValue, "");
        for (String winValue : winCodes.values()) str = str.replace(winValue, "");
        for (String part : parts.keySet()) str = str.replace(part, parts.get(part));

        return str;
    }

    /**
     * Get all the colors of the string
     *
     * @param message the message
     * @return the message colors
     */
    public static String[] colors(final String message) {
        String str = message;
        if (str.contains("§")) {
            str = str.replace("§", "&");
        }

        Set<String> parts = new LinkedHashSet<>();
        for (char cCode : codes.keySet()) {
            String code = "&" + cCode;
            String unixReplacement = unixCodes.get(cCode);
            String winReplacement = winCodes.get(cCode);

            if (str.contains(unixReplacement) || str.contains(winReplacement) || str.contains(code)) {
                parts.add(code);
            }
        }

        String[] colors = parts.toArray(new String[0]);
        String lastColor = colors[colors.length - 1];
        if (lastColor.equalsIgnoreCase("&r")) {
            parts.remove(lastColor);
        }

        return parts.toArray(new String[0]);
    }

    /**
     * Get the last color of the message
     *
     * @param message the message
     * @return the last color
     */
    public static String lastColor(final String message) {
        String[] colors = colors(message);
        return colors[colors.length - 1];
    }

    /**
     * Print a message
     *
     * @param message the message
     */
    public static void printColored(final String message) {
        String colorParsed = parse(message);
        PlaceholderEngine engine = KarmaKore.INSTANCE().placeholderEngine("default");
        String placeholderParsed = engine.parse(colorParsed);

        System.out.println(placeholderParsed);
    }
}
