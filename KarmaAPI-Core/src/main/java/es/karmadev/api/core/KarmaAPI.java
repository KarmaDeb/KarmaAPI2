package es.karmadev.api.core;

import es.karmadev.api.JavaVirtualMachine;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.security.LockedProperties;
import es.karmadev.api.version.Version;
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
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
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

        Path workingDirectory = source.getParent();
        Path thirdparty = workingDirectory.resolve("thirdparty");
        Path runtime = thirdparty.resolve("runtime");
        Path library = thirdparty.resolve("library");

        Path burningWave = runtime.resolve("BurningWave.jar"); //Done
        Path jvmDriver = runtime.resolve("JVMDriver.jar"); //Done

        Path lz4 = library.resolve("LZ4.jar"); //Done
        Path zstd = library.resolve("ZSTD.jar"); //Done
        Path schemaValidator = library.resolve("SchemaValidator.jar"); //Done
        Path snakeYaml = library.resolve("SnakeYaml.jar"); //Done

        Map<Path, String> checkClasses = new HashMap<>();
        checkClasses.put(burningWave, "es.karmadev.api.shaded.burningwave.core.Strings");
        checkClasses.put(jvmDriver, "io.github.toolfactory.jvm.Driver");
        checkClasses.put(schemaValidator, "es.karmadev.api.shaded.jsonschema.main.JsonSchema");
        checkClasses.put(snakeYaml, "es.karmadev.api.shaded.snakeyaml.Yaml");

        String downloadURL = properties.getProperty("download_server", "https://karmadev.es/download/");
        String checksumURL = properties.getProperty("checksum_server", "https://karmadev.es/download/");

        URL downloadRoot = URLUtilities.fromString(downloadURL);
        URL checksumRoot = URLUtilities.fromString(checksumURL);

        Version jvmVersion = JavaVirtualMachine.jvmVersion();
        int mayor = jvmVersion.getMayor();
        int minor = jvmVersion.getMinor();

        if (mayor == 1) mayor = minor;

        URLClassLoader tmp = null;
        try {
            APISource principal = SourceManager.getPrincipal();
            if (principal != null) {
                tmp = (URLClassLoader) principal.getClass().getClassLoader();
            }
        } catch (ClassCastException | UnknownProviderException ignored) {}
        if (tmp == null) tmp = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        unbounded.send(LogLevel.INFO, "Setting up for jvm: {0}", jvmVersion);
        if (mayor >= 9) {
            validateChecksum(downloadRoot, checksumRoot, checkClasses, jvmDriver, burningWave); //We make sure that burningwave and jvm driver gets downloaded first
            try {
                URL[] urls = {
                        new URL("jar:file:" + burningWave + "!/"),
                        new URL("jar:file:" + jvmDriver + "!/")};

                URLClassLoader cl = new URLClassLoader(urls, tmp);
                Class<?> loader = cl.loadClass("org.burningwave.core.assembler.StaticComponentContainer");

                Field modules = loader.getDeclaredField("Modules");
                Object module = modules.get(null);
                Class<?> modClass = module.getClass();

                Method exportAllToAll = modClass.getDeclaredMethod("exportAllToAll");
                exportAllToAll.setAccessible(true);

                exportAllToAll.invoke(module);
            } catch (ClassNotFoundException |
                     NoSuchFieldException |
                     IllegalAccessException |
                     NoSuchMethodException |
                     InvocationTargetException |
                     IOException ex) {
                unbounded.send(ex, "Failed to inject BurningWave");
            }
        }

        validateChecksum(downloadRoot, checksumRoot, checkClasses, lz4, zstd, schemaValidator, snakeYaml);
        inject(lz4, tmp);
        inject(zstd, tmp);
        inject(schemaValidator, tmp);
        inject(snakeYaml, tmp);
    }

    private static void validateChecksum(final URL downloadURL, final URL checksumURL, final Map<Path, String> checks, final Path... files) {
        boolean download = true;
        UnboundedLogger unbounded = new UnboundedLogger();

        Map<URL, Path> filesToDownload = new HashMap<>();
        for (Path file : files) {
            if (checks.containsKey(file)) {
                String checkClass = checks.get(file);
                try {
                    /*We don't want our application to download
                    a dependency if another application already has
                    it. It's a waste of time and disk usage
                     */
                    Class.forName(checkClass);
                    continue;
                } catch (ClassNotFoundException ignored) {}
            }

            String name = file.getFileName().toString();
            byte[] fileBytes = PathUtilities.readBytes(file);
            String sha = hash(fileBytes);

            URL check = URLUtilities.append(checksumURL, "?file=" + name + "&sha=" + sha);

            String response = URLUtilities.get(check);
            boolean valid = Boolean.parseBoolean(response);

            if (!valid) {
                unbounded.send(LogLevel.WARNING, "Preparing to {0} dependency: {1}", (Files.exists(file) ? "update" : "download"), name);
                filesToDownload.put(URLUtilities.append(downloadURL, "?file=" + name), file);
            }
        }

        for (URL url : filesToDownload.keySet()) {
            Path file = filesToDownload.get(url);
            String name = file.getFileName().toString();

            unbounded.send(LogLevel.WARNING, "{0} dependency: {1} from {2}", (Files.exists(file) ? "Updating" : "Downloading"), name, url);

            boolean update = Files.exists(file);
            try (InputStream resource = url.openStream()) {
                byte[] streamBytes = StreamUtils.read(resource);
                PathUtilities.write(file, streamBytes);

                unbounded.send(LogLevel.WARNING, "{0} dependency: {1}", (update ? "Updated" : "Downloaded"), name);
            } catch (IOException ex) {
                unbounded.send(ex, "An error occurred while {0} dependency: {1}", (update ? "updating" : "downloading"), name);
            }
        }
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
