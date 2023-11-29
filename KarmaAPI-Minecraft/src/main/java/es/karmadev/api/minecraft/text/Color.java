package es.karmadev.api.minecraft.text;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
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
        return colorMap.values().stream()
                .filter((cl) -> cl.name.equalsIgnoreCase(color))
                .findAny().orElse(null);
    }

    /**
     * Get a color by color code
     *
     * @param code the color code
     * @return the color
     */
    @Nullable
    public static Color getByCode(final char code) {
        return colorMap.values().stream()
                .filter((cl) -> cl.code == code)
                .findAny().orElse(null);
    }

    /**
     * Get a color by hex code
     *
     * @param hex the hex code
     * @return the color
     */
    @Nullable
    public static Color getByHex(final int hex) {
        return colorMap.values().stream()
                .filter((cl) -> cl.hex == hex)
                .findAny().orElse(null);
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
            return colorMap.values().stream()
                    .filter((cl) -> cl.hex == code)
                    .findAny().orElse(null);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private final static Map<Integer, Color> colorMap = new HashMap<>();
    static {
        colorMap.put(0x000000, BLACK);
        colorMap.put(0x0000AA, DARK_BLUE);
        colorMap.put(0x00AA00, DARK_GREEN);
        colorMap.put(0x00AAAA, DARK_AQUA);
        colorMap.put(0xAA0000, DARK_RED);
        colorMap.put(0xAA00AA, DARK_PURPLE);
        colorMap.put(0xFFAA00, GOLD);
        colorMap.put(0xAAAAAA, GRAY);
        colorMap.put(0x555555, DARK_GRAY);
        colorMap.put(0x5555FF, BLUE);
        colorMap.put(0x55FF55, GREEN);
        colorMap.put(0x55FFFF, AQUA);
        colorMap.put(0xFF5555, RED);
        colorMap.put(0xFF55FF, PURPLE);
        colorMap.put(0xFFFF55, YELLOW);
        colorMap.put(0xFFFFFF, WHITE);
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
}
