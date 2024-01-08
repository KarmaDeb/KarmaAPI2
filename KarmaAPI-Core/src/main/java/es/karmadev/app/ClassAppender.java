package es.karmadev.app;

import es.karmadev.api.file.util.PathUtilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Initialize the class appender
 */
public class ClassAppender extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    /**
     * Initialize the class appender
     *
     * @param internalPath the initial appender URLs
     * @param parent the loader parent
     */
    public ClassAppender(final ClassLoader parent, final String internalPath) {
        super(new URL[]{extract(parent, internalPath)}, parent);
    }

    /**
     * Append a URL to the appender
     *
     * @param url the url to append
     */
    public void append(final URL url) {
        super.addURL(url);
    }

    /**
     * Append a URI to the appender
     *
     * @param uri the uri to append
     * @return if the uri as appended
     */
    public boolean append(final URI uri) {
        try {
            super.addURL(uri.toURL());
            return true;
        } catch (MalformedURLException ignored) {}

        return false;
    }

    /**
     * Append a file to the appender
     *
     * @param file the file to append
     * @return if the file as appended
     */
    public boolean append(final File file) {
        if (file.isDirectory()) return false;
        String extension = PathUtilities.getExtension(file.getName());
        if (!extension.equalsIgnoreCase("jar") &&
                !extension.equalsIgnoreCase("class")) return false;

        return append(file.toURI());
    }

    /**
     * Append a file to the appender
     *
     * @param file the file to append
     * @return if the file as appended
     */
    public boolean append(final Path file) {
        if (Files.isDirectory(file)) return false;
        String extension = PathUtilities.getExtension(file);
        if (!extension.equalsIgnoreCase("jar") &&
                !extension.equalsIgnoreCase("class")) return false;

        return append(file.toUri());
    }

    /**
     * Instantiate the bootstrap
     *
     * @param bootstrapCass the bootstrap class
     * @return the bootstrap
     */
    public Bootstrap instantiate(final String bootstrapCass) {
        try {
            Class<? extends Bootstrap> bootClass = loadClass(bootstrapCass)
                    .asSubclass(Bootstrap.class);

            return bootClass.getConstructor(ClassAppender.class)
                    .newInstance(this);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static URL extract(final ClassLoader loader, final String jarPath) {
        URL jar = loader.getResource(jarPath);
        if (jar == null) throw new IllegalStateException("Cannot extract " + jarPath);

        Path path;
        try {
            path = Files.createTempFile("class-appender", ".jar.tmp");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        path.toFile().deleteOnExit();

        try(InputStream is = jar.openStream()) {
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            return path.toUri().toURL();
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}