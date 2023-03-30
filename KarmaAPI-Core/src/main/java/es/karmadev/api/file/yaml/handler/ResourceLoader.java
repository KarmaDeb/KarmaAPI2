package es.karmadev.api.file.yaml.handler;

import java.io.InputStream;

/**
 * Resource loader
 */
public interface ResourceLoader {

    /**
     * Load a template
     *
     * @param name the template name
     * @return the loaded template
     */
    InputStream loadResource(final String name);
}
