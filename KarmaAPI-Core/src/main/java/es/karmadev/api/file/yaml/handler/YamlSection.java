package es.karmadev.api.file.yaml.handler;

import es.karmadev.api.file.yaml.YamlFileHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Yaml section
 */
class YamlSection extends SimpleYamlHandler {

    /**
     * Initialize the yaml handler
     *
     * @param data the yaml data
     */
    YamlSection(Map<String, Object> data) {
        super(data);
    }

    /**
     * Save the current yaml file handler
     * to the specified file
     *
     * @param path the yaml file path
     * @return the saved yaml file
     * @throws IOException if the file fails to save
     */
    @Override
    public YamlFileHandler saveTo(final Path path) throws IOException {
        throw new IOException("Cannot save yaml section");
    }

    /**
     * Save the yaml file
     *
     * @throws IOException if the file fails to save
     */
    @Override
    public void save() throws IOException {
        throw new IOException("Cannot save yaml section");
    }
}
