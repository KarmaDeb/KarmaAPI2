package es.karmadev.api.core;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.security.LockedProperties;
import es.karmadev.api.web.WebDownloader;
import es.karmadev.api.web.url.URLUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * KarmaAPI information
 */
@SuppressWarnings("unused")
public class KarmaAPI {

    public final static Properties properties = new LockedProperties();

    static {
        try(InputStream stream = KarmaAPI.class.getResourceAsStream("/api.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (NullPointerException | IllegalArgumentException | IOException ignored) {}
    }

    public final static String VERSION = properties.getProperty("version", "1.0.0");
    public final static String BUILD = properties.getProperty("build", "1");
    public final static String FULL_VERSION = properties.getProperty("full", "2.0.0-1");
    public final static String COMPILER = properties.getProperty("java", "8");
    public final static String COMPILE_DATE = properties.getProperty("date", "01-01-1999 00:00:00");
    public final static Supplier<String> USER_AGENT = () ->
            String.format("KarmaAPI/%s Build/%s Java/%s Identifier/%s", VERSION, BUILD, COMPILER, properties.getProperty("identifier"));

    public static boolean isTestMode() {
        String password = System.getenv("TEST_PASSWORD");
        if (password == null) return false;

        String hash = hash(password.getBytes());
        return hash.equals("43cbb23abe19f41edfd064dc50321ec15c3f385fa3584abfa187024237b48f783f34c1a00a694f74559f13634cd6f2d034b0d8ca03dd4a057a71063e923d8b38");
    }

    private static String hash(final byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(data);

            byte[] rs = digest.digest();
            StringBuilder hexBuilder = new StringBuilder();

            for (byte b : rs) {
                String hex = String.format("%02x", b);
                hexBuilder.append(hex);
            }

            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException ex) {
            return "";
        }
    }

    /**
     * Set up the API
     * @throws URISyntaxException if the application fails to find the install location
     */
    public static void setup() throws URISyntaxException {
        UnboundedLogger unbounded = new UnboundedLogger();

        Class<?> clazz = KarmaAPI.class;
        ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain == null) throw new IllegalStateException("Cannot setup source because the protected domain is null");

        CodeSource code = domain.getCodeSource();
        if (code == null) throw new IllegalStateException("Cannot setup source because the code source is null");

        URL location = code.getLocation();
        if (location == null) throw new IllegalStateException("Cannot setup source because the code location is null");

        URI uri = location.toURI();
        Path source = Paths.get(uri);

        Path workingDirectory = source.getParent().resolve("KarmaAPI");
        Path thirdparty = workingDirectory.resolve("thirdparty");
        Path runtime = thirdparty.resolve("runtime");
        Path library = thirdparty.resolve("library");

        Path burningWave = runtime.resolve("BurningWave.jar"); //Done
        Path jvmDriver = runtime.resolve("JVMDriver.jar"); //Done

        Path lz4 = library.resolve("LZ4.jar"); //Done
        Path zstd = library.resolve("ZSTD.jar"); //Done
        Path schemaValidator = library.resolve("SchemaValidator.jar"); //Done
        Path snakeYaml = library.resolve("SnakeYaml.jar"); //Done

        String downloadURL = properties.getProperty("download_server", "https://reddo.es/repository/karma/");
        URL downloadRoot = URLUtilities.fromString(downloadURL);
        assert downloadRoot != null;

        if (!Files.exists(jvmDriver)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "JVMDriver.jar"));
            try {
                downloader.download(jvmDriver);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(burningWave)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "BurningWave.jar"));
            try {
                downloader.download(burningWave);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(lz4)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "LZ4.jar"));
            try {
                downloader.download(lz4);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(zstd)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "ZSTD.jar"));
            try {
                downloader.download(zstd);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(schemaValidator)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "SchemaValidator.jar"));
            try {
                downloader.download(schemaValidator);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(snakeYaml)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "SnakeYaml.jar"));
            try {
                downloader.download(snakeYaml);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }

        URLClassLoader tmp = null;
        try {
            APISource principal = SourceManager.getPrincipal();
            if (principal != null) {
                tmp = (URLClassLoader) principal.getClass().getClassLoader();
            }
        } catch (ClassCastException | UnknownProviderException ignored) {}
        if (tmp == null) tmp = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        try {
            URL[] urls = {
                    new URL("jar:file:" + burningWave + "!/"),
                    new URL("jar:file:" + jvmDriver + "!/")};

            URLClassLoader cl = new URLClassLoader(urls, tmp);
            Class<?> loader = cl.loadClass("org.burningwave.core.assembler.StaticComponentContainer");

            Field modules = loader.getDeclaredField("Modules");
            Object module = modules.get(null);
            if (module != null) {
                Class<?> modClass = module.getClass();

                Method exportAllToAll = modClass.getDeclaredMethod("exportAllToAll");
                exportAllToAll.setAccessible(true);

                exportAllToAll.invoke(module);
                unbounded.send(LogLevel.SUCCESS, "Successfully injected with BurningWave");
            }
        } catch (ClassNotFoundException |
                 NoSuchFieldException |
                 IllegalAccessException |
                 NoSuchMethodException |
                 InvocationTargetException |
                 IOException ex) {
            unbounded.send(ex, "Failed to inject BurningWave");
        }

        inject(lz4, tmp);
        inject(zstd, tmp);
        inject(schemaValidator, tmp);
        inject(snakeYaml, tmp);
    }

    /**
     * Inject a file
     *
     * @param file the file to inject
     * @param loader the loader to inject at
     */
    public static boolean inject(final Path file, final ClassLoader loader) {
        try {
            Method method;
            if (loader instanceof URLClassLoader) {
                method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(loader, file.toUri().toURL());
            } else {
                method = ClassLoader.class.getDeclaredMethod("addClass", Class.class);
                Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                Method load = ClassLoader.class.getMethod("loadClassData", String.class);
                method.setAccessible(true);
                define.setAccessible(true);
                load.setAccessible(true);

                try (JarFile jarFile = new JarFile(file.toFile())) {
                    Enumeration<JarEntry> e = jarFile.entries();

                    URL[] urls = {new URL("jar:file:" + PathUtilities.pathString(file, '/') + "!/")};
                    try (URLClassLoader cl = URLClassLoader.newInstance(urls)) {
                        while (e.hasMoreElements()) {
                            JarEntry je = e.nextElement();
                            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                                continue;
                            }

                            String className = je.getName().substring(0, je.getName().length() - 6);
                            if (!className.endsWith("module-info")) {
                                Class<?> clazz = cl.loadClass(className);
                                method.invoke(loader, clazz);
                                byte[] data = (byte[]) load.invoke(loader, className);
                                define.invoke(loader, className, data, 0, data.length);
                            }
                        }

                        load.setAccessible(false);
                        define.setAccessible(false);
                    }
                }
            }

            return true;
        } catch (NoSuchMethodException |
                 InvocationTargetException |
                 ClassNotFoundException |
                 IllegalAccessException |
                 IOException ex) {
            ExceptionCollector.catchException(KarmaAPI.class, ex);
        }

        return false;
    }
}
