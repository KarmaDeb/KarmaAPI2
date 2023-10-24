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
@Deprecated
public class CapeComponent {

    @Getter
    URL url;
    @Getter
    String data;

    /**
     * Create an empty cape data
     *
     * @return an empty cape data
     */
    public static CapeComponent empty() {
        return CapeComponent.of(null, null);
    }
}
