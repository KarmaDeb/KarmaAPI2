package es.karmadev.api.web.minecraft.response.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.strings.StringUtils;
import es.karmadev.api.web.minecraft.response.ImageContainer;
import es.karmadev.api.web.minecraft.response.JsonContainer;
import es.karmadev.api.web.minecraft.response.data.component.SkinComponent;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

/**
 * Online KarmaAPI head request
 */
@Builder @Deprecated
public class OKAHeadRequest implements ImageContainer, JsonContainer {

    @Nullable
    @Getter
    URI uri;

    @Getter
    long id;

    @Getter
    int size;

    @Getter @NonNull
    SkinComponent texture;

    @Getter @NonNull
    String head;

    @NonNull
    String json;

    /**
     * Parse the image base64 to a
     * buffered image
     *
     * @return the image
     * @throws IOException as part of {@link ImageIO#read(InputStream)}
     */
    @Override
    public BufferedImage toImage() throws IOException {
        if (head.contains(",")) {
            String[] data = head.split(",");
            String base = data[1];

            byte[] imageBytes = Base64.getDecoder().decode(base);
            try (ByteArrayInputStream stream = new ByteArrayInputStream(imageBytes)) {
                return ImageIO.read(stream);
            }
        }

        return null;
    }

    /**
     * Export the head image into a file
     *
     * @param paths the destination file
     * @return if the head image could be exported
     * @throws IOException if the file failed to write or as part of {@link OKAHeadRequest#toImage()}
     */
    @Override
    public Path export(final String... paths) throws IOException {
        BufferedImage image = toImage();

        Path file;
        switch (paths.length) {
            case 0:
                String path = StringUtils.generateSplit('\0') + ".png";
                if (uri != null) path = uri.getPath() + ".png";
                file = Paths.get(path);
                break;
            case 1:
                file = Paths.get(paths[0]);
                break;
            default:
                file = Paths.get(paths[0], Arrays.copyOfRange(paths, 1, paths.length));
                break;
        }

        PathUtilities.createPath(file);

        ImageIO.write(image, "png", Files.newOutputStream(file));
        return file;
    }

    /**
     * Parse the response to json
     *
     * @param pretty prettify the output
     * @return the json response
     */
    @Override
    public String toJson(final boolean pretty) {
        if (pretty) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create(); //Not compatible
                JsonElement element = gson.fromJson(json, JsonElement.class);

                return gson.toJson(element);
            } catch (Throwable ignored) {}
        }

        return json;
    }

    /**
     * Build an empty head request
     *
     * @return the empty head request
     */
    public static OKAHeadRequest empty() {
        return empty("{}");
    }

    /**
     * Build an empty head request
     *
     * @param json the json request
     * @return the empty head request
     */
    public static OKAHeadRequest empty(final String json) {
        return OKAHeadRequest.builder().texture(SkinComponent.empty()).head("").json((json != null ? json : "{}")).build();
    }
}
