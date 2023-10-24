package es.karmadev.api.core;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.security.LockedProperties;
import es.karmadev.api.web.WebDownloader;
import es.karmadev.api.web.url.URLUtilities;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;

import java.io.File;
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

    public final static String VERSION = properties.getProperty("version", "2.0.0");
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
        Class<?> clazz = KarmaAPI.class;
        ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain == null) return;

        CodeSource code = domain.getCodeSource();
        if (code == null) return;

        URL location = code.getLocation();
        if (location == null) return;

        URI uri = location.toURI();
        Path source = Paths.get(uri);

        Path workingDirectory = source.getParent().resolve("KarmaAPI");
        Path thirdparty = workingDirectory.resolve("thirdparty");
        Path runtime = thirdparty.resolve("runtime");
        Path library = thirdparty.resolve("library");
        Path relocated = library.resolve("relocation");

        Path burningWave = runtime.resolve("BurningWave.jar");
        Path jvmDriver = runtime.resolve("JVMDriver.jar");

        Path relocatorASM = library.resolve("ASM.jar");
        Path asmTree = library.resolve("ASMTree.jar");
        Path asmAnalysis = library.resolve("ASMAnalysis.jar");
        Path asmCommons = library.resolve("ASMCommons.jar");

        Path lz4 = library.resolve("LZ4.jar");
        Path zstd = library.resolve("ZSTD.jar");

        Path schemaValidator = library.resolve("SchemaValidator.jar");
        Path schemaJson = library.resolve("JSON.jar");

        Path snakeYaml = library.resolve("SnakeYaml.jar");
        Path relocatedSnakeYaml = relocated.resolve("snake_yaml.jar");

        Path javaScript = library.resolve("MozillaScript.jar");

        Path reflectionAPI = library.resolve("ReflectionAPI.jar");
        Path relocatedReflection = relocated.resolve("reflection_api.jar");

        Path googleGson = library.resolve("GSON.jar");
        Path relocatedGson = relocated.resolve("google_gson.jar");

        String downloadURL = properties.getProperty("download_server", "https://karmadev.es/karma-repository/v2/");
        URL downloadRoot = URLUtilities.fromString(downloadURL);
        assert downloadRoot != null;

        if (!Files.exists(jvmDriver)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "jvm_driver.jar"));
            try {
                downloader.download(jvmDriver);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(burningWave)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "burning_wave.jar"));
            try {
                downloader.download(burningWave);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(relocatorASM)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "relocator_asm.jar"));
            try {
                downloader.download(relocatorASM);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(asmTree)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "asm_tree.jar"));
            try {
                downloader.download(asmTree);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(asmAnalysis)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "asm_analysis.jar"));
            try {
                downloader.download(asmAnalysis);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(asmCommons)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "asm_commons.jar"));
            try {
                downloader.download(asmCommons);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(lz4)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "compressor_lz4.jar"));
            try {
                downloader.download(lz4);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(zstd)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "compressor_zstd.jar"));
            try {
                downloader.download(zstd);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(schemaValidator)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "schema_validator.jar"));
            try {
                downloader.download(schemaValidator);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(schemaJson)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "schema_json.jar"));
            try {
                downloader.download(schemaJson);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(snakeYaml)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "snake_yaml.jar"));
            try {
                downloader.download(snakeYaml);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(javaScript)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "mozilla_javascript.jar"));
            try {
                downloader.download(javaScript);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(reflectionAPI)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "reflection_api.jar"));
            try {
                downloader.download(reflectionAPI);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(googleGson)) {
            WebDownloader downloader = new WebDownloader(URLUtilities.append(downloadRoot, "google_gson.jar"));
            try {
                downloader.download(googleGson);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }

        ClassLoader tmp = null;
        try {
            APISource principal = SourceManager.getPrincipal();
            if (principal != null) {
                tmp = principal.getClass().getClassLoader();
            }
        } catch (ClassCastException | UnknownProviderException ignored) {}
        if (tmp == null) tmp = Thread.currentThread().getContextClassLoader();

        try {
            URL[] urls = {
                    new URL("jar:file:" + burningWave + "!/"),
                    new URL("jar:file:" + jvmDriver + "!/")};

            try (URLClassLoader cl = new URLClassLoader(urls, tmp)) {
                Class<?> loader = cl.loadClass("org.burningwave.core.assembler.StaticComponentContainer");

                Field modules = loader.getDeclaredField("Modules");
                Object module = modules.get(null);
                if (module != null) {
                    Class<?> modClass = module.getClass();

                    Method exportAllToAll = modClass.getDeclaredMethod("exportAllToAll");
                    exportAllToAll.setAccessible(true);

                    exportAllToAll.invoke(module);
                    //logger.send(LogLevel.SUCCESS, "Successfully injected with BurningWave");
                }
            }
        } catch (ClassNotFoundException |
                 NoSuchFieldException |
                 IllegalAccessException |
                 NoSuchMethodException |
                 InvocationTargetException |
                 IOException ex) {
            //logger.send(ex, "Failed to inject BurningWave");
            throw new RuntimeException(ex);
        }

        inject(relocatorASM, tmp);
        inject(asmTree, tmp);
        inject(asmAnalysis, tmp);
        inject(asmCommons, tmp);

        //logger.send(LogLevel.INFO, "Preparing to relocate");

        Set<Relocation> relocations = new HashSet<>();
        //relocations.add(new Relocation(buildPackage("org", "github", "fge"), "es.karmadev.api.shaded.fge"));
        relocations.add(new Relocation(buildPackage("org", "yaml", "snakeyaml"), "es.karmadev.api.shaded.snakeyaml"));
        relocations.add(new Relocation(buildPackage("com", "github", "yeetmanlord", "reflection_api"), "es.karmadev.forked.reflection_api"));
        relocations.add(new Relocation(buildPackage("com", "google", "gson"), "es.karmadev.api.shaded.google.gson"));

        PathUtilities.createPath(relocatedSnakeYaml);
        PathUtilities.createPath(relocatedReflection);
        PathUtilities.createPath(relocatedGson);

        File snakeSource = snakeYaml.toFile();
        File snakeRelocated = relocatedSnakeYaml.toFile();
        JarRelocator snakeRelocator = new JarRelocator(snakeSource, snakeRelocated, relocations);

        File reflectionSource = reflectionAPI.toFile();
        File reflectionRelocated = relocatedReflection.toFile();
        JarRelocator reflectionRelocator = new JarRelocator(reflectionSource, reflectionRelocated, relocations);

        File gsonSource = googleGson.toFile();
        File gsonRelocated = relocatedGson.toFile();
        JarRelocator gsonRelocator = new JarRelocator(gsonSource, gsonRelocated, relocations);

        try {
            snakeRelocator.run();
            reflectionRelocator.run();
            gsonRelocator.run();

            //logger.send(LogLevel.INFO, "Successfully relocated 3 libraries [snake_yaml, reflection_api, google_gson]");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        inject(schemaJson, tmp);
        inject(schemaValidator, tmp);
        inject(relocatedGson, tmp);
        inject(relocatedSnakeYaml, tmp);
        inject(javaScript, tmp);

        inject(lz4, tmp);
        inject(zstd, tmp);

        inject(relocatedReflection, tmp);
    }

    private static String buildPackage(final String... parts) {
        StringBuilder builder = new StringBuilder();

        int index = 0;
        for (String part : parts) {
            builder.append(part);
            if (index++ < parts.length - 1) {
                builder.append(".");
            }
        }

        return builder.toString();
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
