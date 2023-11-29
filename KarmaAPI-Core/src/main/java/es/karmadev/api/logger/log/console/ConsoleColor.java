package es.karmadev.api.logger.log.console;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.strings.StringUtils;
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
@Getter
@SuppressWarnings("unused")
public enum ConsoleColor {
    /**
     * Console color
     */
    BLACK('0', "[0;38;2;0;0;0m", "[30m"),
    /**
     * Console color
     */
    DARK_BLUE('1', "[0;38;2;0;0;128m", "[34m"),
    /**
     * Console color
     */
    DARK_GREEN('2', "[0;38;2;0;128;0m", "[32m"),
    /**
     * Console color
     */
    DARK_AQUA('3', "[0;38;2;0;128;128m", "[36m"),
    /**
     * Console color
     */
    DARK_RED('4', "[0;38;2;128;0;0m", "[31m"),
    /**
     * Console color
     */
    DARK_PURPLE('5', "[0;38;2;128;0;128m", "[35m"),
    /**
     * Console color
     */
    DARK_YELLOW('6', "[0;38;2;128;128;0m", "[33m"),
    /**
     * Console color
     */
    GRAY('7', "[0;38;2;192;192;192m", "[37m"),
    /**
     * Console color
     */
    DARK_GRAY('8', "[0;38;2;128;128;128m", "[90m"),
    /**
     * Console color
     */
    BLUE('9', "[0;38;2;0;95;255m", "94m"),
    /**
     * Console color
     */
    GREEN('a', "[0;38;2;0;255;0m", "[92m"),
    /**
     * Console color
     */
    AQUA('b', "[0;38;2;0;255;255m", "[96m"),
    /**
     * Console color
     */
    RED('c', "[0;38;2;255;0;0m", "[91m"),
    /**
     * Console color
     */
    PURPLE('d', "[0;38;2;255;0;255m", "[95m"),
    /**
     * Console color
     */
    YELLOW('e', "[0;38;2;255;255;0m", "[93m"),
    /**
     * Console color
     */
    WHITE('f', "[0;38;2;255;255;255m", "[97m"),
    /**
     * Console color
     */
    RESET('r', "[0m", "[0m"),
    /**
     * Console color
     */
    BOLD('l', "[1m", "[1m"),
    /**
     * Console color
     */
    UNDERLINE('n', "[4m", "[4m"),
    /**
     * Console color
     */
    ITALIC('o', "[3m", "[3m"),
    /**
     * Console color
     */
    STRIKETHROUGH('m', "[9m", "[9m");

    @SuppressWarnings("NonFinalFieldInEnum")
    public static boolean forceOtherOs = false;
    @SuppressWarnings("NonFinalFieldInEnum")
    public static char ignoreCharacter = '\\';

    private final char code;
    private final char colorCode;
    private final String unixCode;
    private final String winCode;

    private final static Map<Character, String> winCodes = new ConcurrentHashMap<>();
    private final static Map<Character, String> unixCodes = new ConcurrentHashMap<>();

    private final static Map<Character, ConsoleColor> codes = new ConcurrentHashMap<>();

    private final static Pattern colorPattern = Pattern.compile("&(?<code>[0-9a-f]|l|r|n|o|m)");


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
        this.code = code;
        this.colorCode = '\u001B';
        unixCode = colorCode + unix;
        winCode = colorCode + win;
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

        StringBuilder builder = new StringBuilder();
        Matcher matcher = colorPattern.matcher(str);

        int source = 0;
        boolean parsed = false;
        while (matcher.find()) {
            parsed = true;
            int start = matcher.start();
            int end = matcher.end();

            builder.append(str, source, start);

            char code = matcher.group("code").charAt(0);
            String osCode = (os.contains("windows") ? winCodes.get(code) : unixCodes.get(code));

            if (osCode == null) continue; //Non-existent, skip
            builder.append(osCode);
            source = end;
        }

        if (parsed) {
            if (source < str.length()) {
                builder.append(str, source, str.length());
            }
        } else {
            builder.append(str);
        }

        return builder.toString();
    }

    /**
     * Strip all the color codes from the string
     *
     * @param message the message
     * @return the uncolored message
     */
    public static String strip(final String message) {
        String os = System.getProperty("os.name").toLowerCase();
        String str = message.replace("§", "&");

        for (Character code : codes.keySet()) {
            String winCode = winCodes.get(code);
            String unixCode = unixCodes.get(code);

            str = str.replace(winCode, "&" + code)
                    .replace(unixCode, "&" + code);
        }

        StringBuilder builder = new StringBuilder();
        Matcher matcher = colorPattern.matcher(str);

        int source = 0;
        boolean parsed = false;

        while (matcher.find()) {
            parsed = true;
            int start = matcher.start();
            int end = matcher.end();

            builder.append(str, source, start);
            source = end;
        }

        if (parsed) {
            if (source < str.length()) {
                builder.append(str, source, str.length());
            }
        } else {
            builder.append(str);
        }

        return builder.toString();
    }

    /**
     * Get all the colors of the string
     *
     * @param message the message
     * @return the message colors
     */
    public static String[] colors(final String message) {
        String str = message.replace("§", "&");

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
