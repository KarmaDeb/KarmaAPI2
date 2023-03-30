package es.karmadev.api.core.source.runtime;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Source runtime
 */
public interface SourceRuntime {

    /**
     * Get the source file
     *
     * @return the source file
     */
    Path getFile();

    /**
     * Get the class that is calling the current method
     *
     * @return the method caller
     */
    default Collection<Path> getMethodCallers() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Set<Path> files = new LinkedHashSet<>();

        for (StackTraceElement element : stackTrace) {
            String name = element.getClassName();
            try {
                Class<?> clazz = Class.forName(name);
                URL url = clazz.getResource('/' + name.replace('.', '/') + ".class");
                if (url != null) {
                    Path path = Paths.get(url.toURI());
                    files.add(path);
                }
            } catch (ClassNotFoundException | URISyntaxException ignored) {}
        }
        files.remove(getFile());

        return files;
    }
}
