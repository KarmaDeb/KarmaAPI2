package es.karmadev.api.minecraft.component;

import lombok.*;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@SuppressWarnings("unused")
public class Color {

    public final static Color BLACK = new Color("black",  '0', 0x000000, "\u001B[30m");
    public final static Color DARK_BLUE = new Color("dark_blue", '1', 0x0000AA, "\u001B[34m");
    public final static Color DARK_GREEN = new Color("dark_green", '2', 0x00AA00, "\u001B[32m");
    public final static Color DARK_AQUA = new Color("dark_aqua", '3', 0x00AAAA, "\u001B[36m");
    public final static Color DARK_RED = new Color("dark_red", '4', 0xAA0000, "\u001B[31m");
    public final static Color DARK_PURPLE = new Color("dark_purple", '5', 0xAA00AA, "\u001B[35m");
    public final static Color GOLD = new Color("gold", '6', 0xFFAA00, "\u001B[33m");
    public final static Color GRAY = new Color("gray", '7', 0xAAAAAA, "\u001B[37m");
    public final static Color DARK_GRAY = new Color("dark_gray", '8', 0x555555, "\u001B[90m");
    public final static Color BLUE = new Color("blue", '9', 0x5555FF, "\u001B[94m");
    public final static Color GREEN = new Color("green", 'a', 0x55FF55, "\u001B[92m");
    public final static Color AQUA = new Color("aqua", 'b', 0x55FFFF, "\u001B[96m");
    public final static Color RED = new Color("red", 'c', 0xFF5555, "\u001B[91m");
    public final static Color PURPLE = new Color("purple", 'd', 0xFF55FF, "\u001B[95m");
    public final static Color YELLOW = new Color("yellow", 'e', 0xFFFF55, "\u001B[93m");
    public final static Color WHITE = new Color("white", 'f', 0xFFFFFF, "\u001B[97m");
    public final static Color RESET = new Color("reset", 'r', Integer.MIN_VALUE, "\u001B[0m");

    public static Color[] values() {
        return new Color[]{
                BLACK,
                DARK_BLUE,
                DARK_GREEN,
                DARK_AQUA,
                DARK_RED,
                DARK_PURPLE,
                GOLD,
                GRAY,
                DARK_GRAY,
                BLUE,
                GREEN,
                AQUA,
                RED,
                PURPLE,
                YELLOW,
                WHITE,
                RESET
        };
    }

    /**
     * Create a new value from the
     * specified rgb values
     *
     * @param red the red alpha
     * @param green the green alpha
     * @param blue the blue alpha
     * @return the color
     */
    public static Color fromRGB(final int red, final int green, final int blue) {
        String hex = String.format("#%02X%02X%02X", red, green, blue);
        return fromHexString(hex);
    }

    /**
     * Create a new color out of a
     * hex string
     *
     * @param hex the hex code
     * @return the color
     */
    public static Color fromHexString(final String hex) {
        Color existing = getByHex(hex);
        if (existing != null) return existing;

        String hexString = hex.toLowerCase();
        if (hexString.startsWith("#")) {
            hexString = hexString.substring(1);
        }

        try {
            return new Color("custom", 'q', Integer.parseInt(hexString, 16), null);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    /**
     * Create a new color out of
     * a hex code
     *
     * @param hex the hex code
     * @return the color
     */
    public static Color fromHex(final int hex) {
        Color existing = getByHex(hex);
        if (existing != null) return existing;

        return new Color("custom", 'q', hex, null);
    }

    /**
     * Get a color by name
     *
     * @param color the color name
     * @return the color
     */
    @Nullable
    public static Color getByName(final String color) {
        return colorNameMap.get(color.toLowerCase());
    }

    /**
     * Get a color by color code
     *
     * @param code the color code
     * @return the color
     */
    @Nullable
    public static Color getByCode(final char code) {
        return colorCharMap.get(Character.toLowerCase(code));
    }

    /**
     * Get a color by hex code
     *
     * @param hex the hex code
     * @return the color
     */
    @Nullable
    public static Color getByHex(final int hex) {
        return colorHexMap.get(hex);
    }

    /**
     * Get a color by hex string
     *
     * @param hex the hex code
     * @return the color
     */
    @Nullable
    public static Color getByHex(final String hex) {
        String hexString = hex.toLowerCase();
        if (hexString.startsWith("#")) {
            hexString = hexString.substring(1);
        }

        try {
            int code = Integer.parseInt(hexString, 16);
            return colorHexMap.get(code);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private final static Map<Integer, Color> colorHexMap = new HashMap<>();
    private final static Map<String, Color> colorNameMap = new HashMap<>();
    private final static Map<Character, Color> colorCharMap = new HashMap<>();

    static {
        colorHexMap.put(0x000000, BLACK);
        colorHexMap.put(0x0000AA, DARK_BLUE);
        colorHexMap.put(0x00AA00, DARK_GREEN);
        colorHexMap.put(0x00AAAA, DARK_AQUA);
        colorHexMap.put(0xAA0000, DARK_RED);
        colorHexMap.put(0xAA00AA, DARK_PURPLE);
        colorHexMap.put(0xFFAA00, GOLD);
        colorHexMap.put(0xAAAAAA, GRAY);
        colorHexMap.put(0x555555, DARK_GRAY);
        colorHexMap.put(0x5555FF, BLUE);
        colorHexMap.put(0x55FF55, GREEN);
        colorHexMap.put(0x55FFFF, AQUA);
        colorHexMap.put(0xFF5555, RED);
        colorHexMap.put(0xFF55FF, PURPLE);
        colorHexMap.put(0xFFFF55, YELLOW);
        colorHexMap.put(0xFFFFFF, WHITE);
        colorHexMap.put(Integer.MIN_VALUE, RESET);

        colorNameMap.put("black", BLACK);
        colorNameMap.put("dark_blue", DARK_BLUE);
        colorNameMap.put("dark_green", DARK_GREEN);
        colorNameMap.put("dark_aqua", DARK_AQUA);
        colorNameMap.put("dark_red", DARK_RED);
        colorNameMap.put("dark_purple", DARK_PURPLE);
        colorNameMap.put("gold", GOLD);
        colorNameMap.put("gray", GRAY);
        colorNameMap.put("dark_gray", DARK_GRAY);
        colorNameMap.put("blue", BLUE);
        colorNameMap.put("green", GREEN);
        colorNameMap.put("aqua", AQUA);
        colorNameMap.put("red", RED);
        colorNameMap.put("purple", PURPLE);
        colorNameMap.put("yellow", YELLOW);
        colorNameMap.put("white", WHITE);
        colorNameMap.put("reset", RESET);

        colorCharMap.put('0', BLACK);
        colorCharMap.put('1', DARK_BLUE);
        colorCharMap.put('2', DARK_GREEN);
        colorCharMap.put('3', DARK_AQUA);
        colorCharMap.put('4', DARK_RED);
        colorCharMap.put('5', DARK_PURPLE);
        colorCharMap.put('6', GOLD);
        colorCharMap.put('7', GRAY);
        colorCharMap.put('8', DARK_GRAY);
        colorCharMap.put('9', BLUE);
        colorCharMap.put('a', GREEN);
        colorCharMap.put('b', AQUA);
        colorCharMap.put('c', RED);
        colorCharMap.put('d', PURPLE);
        colorCharMap.put('e', YELLOW);
        colorCharMap.put('f', WHITE);
        colorCharMap.put('r', RESET);
    }

    private final String name;
    private final char code;
    private final int hex;
    private final String ascii;

    /**
     * Get if this is a custom
     * color
     *
     * @return if the current color
     * is custom
     */
    public final boolean isCustom() {
        return name.equalsIgnoreCase("custom");
    }

    /**
     * Get the parsed color
     *
     * @return the parsed color
     */
    public String getParsed() {
        if (name.equalsIgnoreCase("custom")) {
            String hexString = Integer.toHexString(hex);
            int length = hexString.length();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                char character;
                if (i >= length) {
                    character = 'f';
                } else {
                    character = hexString.charAt(i);
                }

                builder.append("§").append(character);
            }

            return builder.toString();
        }

        return String.valueOf(code);
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String result = getParsed();
        return "§" + result;
    }
}
