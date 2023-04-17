package es.karmadev.api.web.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Request head entry
 */
@AllArgsConstructor
public class HeadEntry {

    @Getter
    private final String key;
    @Getter
    private final String value;
}
