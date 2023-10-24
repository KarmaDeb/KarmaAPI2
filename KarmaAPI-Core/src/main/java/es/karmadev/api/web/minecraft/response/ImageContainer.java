package es.karmadev.api.web.minecraft.response;

import es.karmadev.api.file.util.PathUtilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Image response container
 */
@SuppressWarnings("unused") @Deprecated
public interface ImageContainer {

    /**
     * Parse the image base64 to a
     * buffered image
     *
     * @return the image
     * @throws IOException as part of {@link javax.imageio.ImageIO#read(InputStream)}
     */
    BufferedImage toImage() throws IOException;

    /**
     * Export the head image into a file
     *
     * @param file the destination file
     * @throws IOException if the file failed to write or as part of {@link es.karmadev.api.web.minecraft.response.data.OKAHeadRequest#toImage()}
     */
    default void export(final Path file) throws IOException {
        BufferedImage image = toImage();
        PathUtilities.createPath(file);

        ImageIO.write(image, "png", Files.newOutputStream(file));
    }

    /**
     * Export the head image into a file
     *
     * @param file the destination file
     * @throws IOException if the file failed to write or as part of {@link es.karmadev.api.web.minecraft.response.data.OKAHeadRequest#toImage()}
     */
    default void export(final File file) throws IOException {
        BufferedImage image = toImage();
        ImageIO.write(image, "png", file);
    }

    /**
     * Export the head image into a file
     *
     * @param stream the destination stream
     * @throws IOException if the file failed to write or as part of {@link es.karmadev.api.web.minecraft.response.data.OKAHeadRequest#toImage()}
     */
    default void export(final OutputStream stream) throws IOException {
        BufferedImage image = toImage();
        ImageIO.write(image, "png", stream);
    }

    /**
     * Export the head image into a file
     *
     * @param paths the destination file
     * @return if the head image could be exported
     * @throws IOException if the file failed to write or as part of {@link es.karmadev.api.web.minecraft.response.data.OKAHeadRequest#toImage()}
     */
    Path export(final String... paths) throws IOException;
}
