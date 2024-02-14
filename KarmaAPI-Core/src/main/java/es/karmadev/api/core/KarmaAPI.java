package es.karmadev.api.core;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.dependency.Dependency;
import es.karmadev.api.dependency.DependencyCollection;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.security.LockedProperties;
import es.karmadev.api.web.WebDownloader;
import es.karmadev.api.web.url.URLUtilities;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * KarmaAPI information
 */
@SuppressWarnings("unused")
public class KarmaAPI {

    public final static Properties properties = new LockedProperties();

    private static boolean negateSetup = false;
    private static boolean installing = false;

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
     * Set the API setup negation status
     *
     * @param negate the negate status
     */
    public static void setNegateSetup(final boolean negate) {
        KarmaAPI.negateSetup = negate;
    }

    /**
     * Setup the API
     * @throws URISyntaxException if the application fails to find install location
     */
    public static void setup() throws URISyntaxException {
        if (installing || negateSetup) return;

        installing = true;
        UnboundedLogger unbounded = new UnboundedLogger();
        try (InputStream stream = KarmaAPI.class.getResourceAsStream("/dependencies.json")) {
            if (stream == null) return;

            JsonInstance instance = JsonReader.read(stream);
            if (instance == null || !instance.isObjectType()) return;

            JsonObject object = instance.asObject();
            JsonInstance dependencies = object.getChild("dependencies");

            if (!dependencies.isArrayType()) return;
            JsonArray array = dependencies.asArray();

            Set<Dependency> dependencySet = new HashSet<>();
            for (JsonInstance element : array) {
                if (!element.isObjectType()) continue;
                JsonObject elementObj = element.asObject();

                Dependency dependency = new Dependency(elementObj);
                dependencySet.add(dependency);
            }

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
            Path thirdParty = workingDirectory.resolve("third-party");
            Path runtime = thirdParty.resolve("runtime");
            Path library = thirdParty.resolve("library");
            Path relocated = library.resolve("relocation");

            DependencyCollection collection = DependencyCollection.wrap(dependencySet);
            AtomicReference<ClassLoader> loaderAtom = new AtomicReference<>();

            AtomicBoolean jvm = new AtomicBoolean(false);
            AtomicBoolean bw = new AtomicBoolean(false);

            unbounded.send(LogLevel.INFO, "Preparing to download {0} KarmaAPI dependencies, please wait...", dependencySet.size());
            collection.process((dependency) -> {
                if (!dependency.platformSupported()) return;

                String id = dependency.getId();
                String name = dependency.getName();

                Path target;
                if (id.equals("burning_wave") || id.equals("jvm_driver")) {
                    target = thirdParty.resolve(String.format("%s.jar", id));
                    if (id.equals("jvm_driver")) {
                        jvm.set(true);
                    }
                    if (id.equals("burning_wave")) {
                        bw.set(true);
                    }
                } else {
                    target = library.resolve(String.format("%s.jar", id));
                }

                URL download = dependency.getDownloadURL();
                if (download == null) {
                    if (!Files.exists(target))
                        unbounded.send(LogLevel.ERROR, "Failed to download dependency {0} because download URL is not valid", name);

                    return;
                }

                if (!Files.exists(target)) {
                    WebDownloader downloader = new WebDownloader(URLUtilities.append(download, String.format("%s.jar", id)));
                    try {
                        downloader.download(target);
                    } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (bw.get() && jvm.get()) {
                    bw.set(false);
                    jvm.set(false);

                    loaderAtom.set(detectClassLoader(thirdParty.resolve("burning_wave.jar"), thirdParty.resolve("jvm_driver.jar")));
                    return;
                }

                ClassLoader loader = loaderAtom.get();
                if (loader == null) return;

                Map<String, String> relocationMap = dependency.getRelocations();
                if (!relocationMap.isEmpty()) {
                    Set<Relocation> relocations = new HashSet<>();
                    for (String rK : relocationMap.keySet()) {
                        String rV = relocationMap.get(rK);

                        Relocation relocation = new Relocation(rK, rV);
                        relocations.add(relocation);
                    }

                    Path targetRelocation = relocated.resolve(String.format("%s.jar", id));
                    if (Files.exists(targetRelocation)) {
                        inject(targetRelocation, loader);
                        return;
                    }

                    PathUtilities.createPath(targetRelocation);

                    JarRelocator relocator = new JarRelocator(target.toFile(), targetRelocation.toFile(), relocations);
                    try {
                        relocator.run();
                        inject(targetRelocation, loader);
                    } catch (IOException ex) {
                        unbounded.send(ex, "Failed to relocate dependency {0}", name);
                    }

                    return;
                }

                inject(target, loader);
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ClassLoader detectClassLoader(Path burningWave, Path jvmDriver) {
        ClassLoader tmp = null;
        try {
            APISource principal = SourceManager.getPrincipal();
            if (principal != null) {
                tmp = principal.getClass().getClassLoader();
            }
        } catch (ClassCastException | UnknownProviderException ignored) {}
        if (tmp == null) tmp = KarmaAPI.class.getClassLoader();

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
        return tmp;
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
            return inject(file.toUri().toURL(), loader);
        } catch (MalformedURLException ex) {
            ExceptionCollector.catchException(KarmaAPI.class, ex);
        }
        return false;
    }

    /**
     * Inject a file
     *
     * @param file the file to inject
     * @param loader the loader to inject at
     */
    public static boolean inject(final File file, final ClassLoader loader) {
        try {
            return inject(file.toURI().toURL(), loader);
        } catch (MalformedURLException ignored) {}
        return false;
    }

    /**
     * Inject a file
     *
     * @param url    the file url to inject
     * @param loader the loader to inject at
     */
    public static boolean inject(final URL url, final ClassLoader loader) {
        try {
            Method method;
            if (loader instanceof URLClassLoader) {
                method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(loader, url);
            } else {
                method = loader.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                method.setAccessible(true);
                method.invoke(loader, url.getFile());
            }

            return true;
        } catch (NoSuchMethodException |
                 InvocationTargetException |
                 IllegalAccessException ex) {
            ExceptionCollector.catchException(KarmaAPI.class, ex);
        }

        return false;
    }
}
