package es.karmadev.api.file.yaml.handler;

import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.file.yaml.YamlFileHandler;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yaml file parser
 */
@SuppressWarnings("unused")
public class YamlHandler implements ResourceLoader {

    public final static ResourceLoader DEFAULT_RESOURCE_LOADER = new YamlHandler();

    /**
     * Initialize the yaml parser
     *
     */
    private YamlHandler() {}

    /**
     * Create a yaml file
     *
     * @param file the yaml file
     * @return a new empty yaml file
     */
    public static YamlFileHandler create(final Path file) {
        return new SimpleYamlHandler(file, new ConcurrentHashMap<>());
    }

    /**
     * Load a yaml file from a resource file
     *
     * @param path the resource path
     * @param loader the resource loader
     * @return the yaml file handler
     * @throws IOException if there's some problem while loading
     * the yaml or closing the original stream
     * @throws NullPointerException if the resource is null
     */
    public static YamlFileHandler loadFromResource(final String path, final ResourceLoader loader) throws IOException, NullPointerException {
        InputStream resource = loader.loadResource(path);
        if (resource == null) throw new NullPointerException("Cannot create yaml file handler from null resource");

        InputStream clone = StreamUtils.clone(resource);
        resource.close();

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(clone);
        if (data == null) data = new LinkedHashMap<>();

        return new SimpleYamlHandler(data);
    }

    /**
     * Load a yaml file
     *
     * @param raw the raw yaml file
     * @return the yaml file handler
     */
    public static YamlFileHandler load(final String raw) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(raw);
        if (data == null) data = new LinkedHashMap<>();

        return new SimpleYamlHandler(data);
    }

    /**
     * Load a yaml file
     *
     * @param raw the raw yaml file
     * @return the yaml file handler
     * @throws IOException as part of {@link YamlReader#YamlReader(InputStream)}
     */
    public static YamlFileHandler load(final InputStream raw) throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(raw);
        if (data == null) data = new LinkedHashMap<>();

        return new SimpleYamlHandler(null, data, new YamlReader(raw));
    }

    /**
     * Load a yaml file
     *
     * @param file the yaml file
     * @return the yaml file handler
     * @throws IOException if there's a problem opening the file
     */
    public static YamlFileHandler load(final Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            if (data == null) data = new LinkedHashMap<>();

            return new SimpleYamlHandler(file, data);
        }
    }

    /**
     * Load a yaml file
     *
     * @param file the yaml file
     * @param source the yaml source
     * @return the yaml file handler
     * @throws IOException if there's a problem opening the file
     */
    public static YamlFileHandler load(final Path file, final YamlReader source) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            StringBuilder lineBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) lineBuilder.append(line).append("\n");

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(lineBuilder.toString());
            if (data == null) data = new LinkedHashMap<>();

            return new SimpleYamlHandler(file, data, source);
        }
    }

    /**
     * Load a template
     *
     * @param name the template name
     * @return the loaded template
     */
    @Override
    public InputStream loadResource(final String name) {
        ClassLoader loader = YamlHandler.class.getClassLoader();
        return loader.getResourceAsStream(name + ".yml");
    }
}
