package es.karmadev.api.web.minecraft.response.data.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.net.URL;

/**
 * Skin data
 */
@Value(staticConstructor = "of")
@AllArgsConstructor(staticName = "of")
public class SkinComponent {

    @Getter
    URL url;
    @Getter
    String value;
    @Getter
    String signature;
    @Getter
    String data;

    /**
     * Create an empty skin data
     *
     * @return an empty skin data
     */
    public static SkinComponent empty() {
        return SkinComponent.of(null, null, null, null);
    }
}
