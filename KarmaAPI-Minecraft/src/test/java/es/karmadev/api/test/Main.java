package es.karmadev.api.test;

import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.web.url.URLUtilities;
import net.md_5.bungee.api.ChatColor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Main {

    public static void main(String[] args) throws Throwable {
        /*System.out.println("Trying to get UUID of KarmaDev");
        UUID onlineId = UUIDFetcher.fetchUUID("KarmaDev", UUIDType.OFFLINE);
        System.out.println(onlineId);*/

        URL url = URLUtilities.fromString("https://visage.surgeplay.com/face/8/KarmaDev.png");
        if (url == null) {
            return;
        }

        System.out.println(url);
        BufferedImage image = ImageIO.read(url);
        image = resize(image, 8, 8);

        StringBuilder builder = new StringBuilder();
        for (int w = 0; w < image.getWidth(); w++) {
            for (int h = 0; h < image.getHeight(); h++) {
                int pixel = image.getRGB(w, h);
                String hexColor = pixelToHex(pixel);

                ChatColor hex = ChatColor.of(hexColor);
                builder.append(ConsoleColor.parse(hex.toString().substring(2, 4))).append('⬛');
            }
            builder.append('\n');
        }

        System.out.println(builder);
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        if (img == null) return null;

        int w = img.getWidth();
        int h = img.getHeight();
        if (w == newW && h == newH) {
            return img;
        }

        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }

    private static String pixelToHex(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;

        String redHex = Integer.toHexString(red);
        String greenHex = Integer.toHexString(green);
        String blueHex = Integer.toHexString(blue);

        if (redHex.length() == 1) {
            redHex = "0" + redHex;
        }
        if (greenHex.length() == 1) {
            greenHex = "0" + greenHex;
        }
        if (blueHex.length() == 1) {
            blueHex = "0" + blueHex;
        }

        return "#" + redHex + greenHex + blueHex;
    }
}
