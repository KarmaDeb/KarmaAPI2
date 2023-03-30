package es.karmadev.api.logger.console;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Console colors
 */
public enum ConsoleColor {
    /**
     * Console color
     */
    BLACK('0', "\033[0;38;2;0;0;0m", "\u001B[30m"),
    /**
     * Console color
     */
    DARK_BLUE('1', "\033[0;38;2;0;0;128m", "\u001B[94m"),
    /**
     * Console color
     */
    DARK_GREEN('2', "\033[0;38;2;0;128;0m", "\u001B[92m"),
    /**
     * Console color
     */
    DARK_AQUA('3', "\033[0;38;2;0;128;128m", "\u001B[96m"),
    /**
     * Console color
     */
    DARK_RED('4', "\033[0;38;2;128;0;0m", "\u001B[91m"),
    /**
     * Console color
     */
    DARK_PURPLE('5', "\033[0;38;2;128;0;128m", "\u001B[95m"),
    /**
     * Console color
     */
    DARK_YELLOW('6', "\033[0;38;2;128;128;0m", "\u001B[93m"),
    /**
     * Console color
     */
    GRAY('7', "\033[0;38;2;192;192;192m", "\u001B[90m"),
    /**
     * Console color
     */
    DARK_GRAY('8', "\033[0;38;2;128;128;128m", "\u001B[97m"),
    /**
     * Console color
     */
    BLUE('9', "\033[0;38;2;0;95;255m", "\u001B[34m"),
    /**
     * Console color
     */
    GREEN('a', "\033[0;38;2;0;255;0m", "\u001B[32m"),
    /**
     * Console color
     */
    AQUA('b', "\033[0;38;2;0;255;255m", "\u001B[36m"),
    /**
     * Console color
     */
    RED('c', "\033[0;38;2;255;0;0m", "\u001B[31m"),
    /**
     * Console color
     */
    PURPLE('d', "\033[0;38;2;255;0;255m", "\u001B[35m"),
    /**
     * Console color
     */
    YELLOW('e', "\033[0;38;2;255;255;0m", "\u001B[33m"),
    /**
     * Console color
     */
    WHITE('f', "\033[0;38;2;255;255;255m", "\u001B[37m"),
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

    @Getter
    private final char code;
    @Getter
    private final String unixCode;
    @Getter
    private final String winCode;

    private final static Map<Character, ConsoleColor> codes = new ConcurrentHashMap<>();

    static {
        for (ConsoleColor color : ConsoleColor.values()) codes.put(color.code, color);
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
        return String.valueOf(id + code);
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
}
