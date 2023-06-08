package es.karmadev.api.web.minecraft.response.data.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 * Server srv record
 */
@Value(staticConstructor = "of")
@AllArgsConstructor(staticName = "of")
public class ServerComponent {

    @Getter
    String host;

    @Getter
    int port;

    @Getter
    long latency;

    /**
     * Create an empty server record
     *
     * @return the record
     */
    public static ServerComponent empty(final int port) {
        return new ServerComponent("127.0.0.1", port, 0);
    }
}
