package es.karmadev.api.spigot.core;

import es.karmadev.api.core.CoreModule;
import es.karmadev.api.core.DefaultRuntime;
import es.karmadev.api.core.KarmaAPI;
import es.karmadev.api.spigot.core.permission.RawPermissionManager;
import es.karmadev.api.spigot.core.scheduler.SpigotTaskScheduler;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.database.DatabaseManager;
import es.karmadev.api.database.model.JsonDatabase;
import es.karmadev.api.database.model.json.JsonConnection;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.strings.StringUtils;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;
import es.karmadev.api.strings.placeholder.engine.SimpleEngine;
import es.karmadev.api.strings.placeholder.engine.SimplePlaceholder;
import es.karmadev.api.version.Version;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KarmaAPI spigot karma source
 */
public abstract class KarmaPlugin extends JavaPlugin implements APISource {

    static {
        try {
            KarmaAPI.setup();
        } catch (URISyntaxException ex) {
            throw new RuntimeException();
        }
    }

    /**
     * -- GETTER --
     *  Get the main KarmaPlugin instance
     */
    @Getter
    private static KarmaPlugin instance;
    private static boolean principalSet = false;
    private static boolean securityWarning = false;

    private final SourceLogger logger;
    private String pluginIdentifier = StringUtils.shuffle(StringUtils.generateSplit(18, '\0'), UUID.randomUUID().toString().replaceAll("-", ""));

    private final Map<CoreModule, Path> modules = new ConcurrentHashMap<>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, PlaceholderEngine> engines = new ConcurrentHashMap<>();
    private final Map<String, SpigotTaskScheduler> schedulers = new ConcurrentHashMap<>();
    private SourceRuntime runtime;

    @SuppressWarnings("unused")
    public KarmaPlugin() {
        this(false);
    }

    public KarmaPlugin(final boolean protect) {
        this(protect, false);
    }

    public KarmaPlugin(final boolean protect, final boolean registerPrincipal) {
        if (instance == null) instance = this;

        setNaggable(false);
        try {
            SourceManager.register(this, protect);
        } catch (AlreadyRegisteredException ex) {
            throw new RuntimeException(ex);
        }
        logger = LogManager.getLogger(this).overrideLogFunction((string) -> {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', string));
            return null;
        });

        saveIdentifier();

        if (registerPrincipal && principalSet) return; //We will simply ignore the register principal task if we already have a principal source
        if (registerPrincipal) {
            try {
                Class<SourceManager> managerClass = SourceManager.class;
                Field principalField = managerClass.getDeclaredField("principal");
                principalField.setAccessible(true);
                principalField.set(managerClass, sourceName());
                principalField.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }

            principalSet = true;
        }

        runtime = new DefaultRuntime(this);
        modules.put(new RawPermissionManager(this), runtime.getFile());

        SimpleEngine engine = new SimpleEngine();
        engine.protect();
        engine.register(new SimplePlaceholder<>("version", KarmaAPI.VERSION).asProtected());
        engine.register(new SimplePlaceholder<>("build", KarmaAPI.BUILD).asProtected());
        engine.register(new SimplePlaceholder<>("date", KarmaAPI.COMPILE_DATE).asProtected());
        engines.put("default", engine);
    }

    /**
     * Enable the plugin
     */
    public abstract void enable();

    /**
     * Disable the plugin
     */
    public abstract void disable();

    @Override
    public final void onEnable() {
        schedulers.put("async", new SpigotTaskScheduler(100, this, 10, 5));
        enable();
    }

    @Override
    public final void onDisable() {
        disable();
        SourceManager.remove(this);
        logger.log(LogLevel.INFO, "Shutting down...");
    }

    /**
     * Get the source identifier
     *
     * @return the source identifier
     */
    @Override
    public final @NotNull String identifier() {
        return pluginIdentifier;
    }

    /**
     * Get the source name
     *
     * @return the source name
     */
    @Override
    public final @NotNull String sourceName() {
        return getName();
    }

    /**
     * Get the source version
     *
     * @return the source version
     */
    @Override
    public final @NotNull Version sourceVersion() {
        return Version.parse(getDescription().getVersion());
    }

    /**
     * Get the source description
     *
     * @return the source description
     */
    @Override
    public final @NotNull String sourceDescription() {
        String rawDescription = getDescription().getDescription();
        if (ObjectUtils.isNullOrEmpty(rawDescription)) return "KarmaPlugin implementation for " + getName();
        assert rawDescription != null;

        return rawDescription;
    }

    /**
     * Get the source authors
     *
     * @return the source authors
     */
    @Override
    public final @NotNull String[] sourceAuthors() {
        return getDescription().getAuthors().toArray(new String[0]);
    }

    /**
     * Load an identifier
     *
     * @param name the identifier name
     */
    @Override
    public final void loadIdentifier(final String name) {
        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(new JsonDatabase());
        JsonConnection connection = database.grabConnection("identifiers");

        String stored = connection.getString(name);
        if (stored != null) pluginIdentifier = stored;
    }

    /**
     * Generate and save an identifier
     *
     * @param name the identifier name
     */
    @Override
    public final void saveIdentifier(final String name) {
        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(new JsonDatabase());
        JsonConnection connection = database.grabConnection("identifiers");

        String stored = connection.getString(name);
        if (stored != null) {
            pluginIdentifier = stored;
            return;
        }

        if (ObjectUtils.isNullOrEmpty(pluginIdentifier)) {
            pluginIdentifier = StringUtils.generateSplit(18, '\0');
        }

        connection.set(name, pluginIdentifier);
        connection.save();
    }

    /**
     * Get a module by name
     *
     * @param name the module name
     * @return the module
     */
    @Override
    public final @Nullable CoreModule getModule(final String name) {
        return modules.keySet().stream().filter((mod) -> mod.getName().equals(name)).findAny().orElse(null);
    }

    /**
     * Load an identifier
     */
    @Override
    public final void loadIdentifier() {
        APISource.super.loadIdentifier();
    }

    /**
     * Generate and save an identifier
     */
    @Override
    public final void saveIdentifier() {
        APISource.super.saveIdentifier();
    }

    /**
     * Get the source update URI
     *
     * @return the source update URI
     */
    @Override
    public @Nullable URI sourceUpdateURI() {
        return null;
    }

    /**
     * Get the source runtime
     *
     * @return the source runtime
     */
    @Override
    public final @NotNull SourceRuntime runtime() {
        return runtime;
    }

    /**
     * Get the source placeholder engine.
     * This will never return null, as if the engine does
     * not exist, a new one will be created
     *
     * @param name the placeholder engine name
     * @return the source placeholder engine
     */
    @Override
    public final @NotNull PlaceholderEngine placeholderEngine(final String name) {
        return engines.computeIfAbsent(name, (engine) -> new SimpleEngine());
    }

    /**
     * Get the source task scheduler.
     * This will never return null, as if the scheduler does
     * not exist, a new one will be created
     *
     * @param name the scheduler name
     * @return the task scheduler
     */
    @Override
    public final @NotNull SpigotTaskScheduler scheduler(final String name) {
        return schedulers.computeIfAbsent(name, (s) -> new SpigotTaskScheduler(100, this, 10, 5));
    }

    /**
     * Get the source working directory
     *
     * @return the working directory
     */
    @Override
    public final @NotNull Path workingDirectory() {
        return getDataFolder().toPath();
    }

    /**
     * Navigate to the specified file
     *
     * @param fileName the file name
     * @param route    the file route starting from {@link APISource#workingDirectory()}
     * @return the file
     */
    @Override
    public final @NotNull Path navigate(final String fileName, final String... route) {
        Path file = workingDirectory();
        for (String str : route) file = file.resolve(str);

        return file.resolve(fileName);
    }

    /**
     * Get the source logger.
     * If the source is not ready, an {@link UnboundedLogger unbounded logger} will
     * be return, which can only print to console or log if is lately bind to a source
     *
     * @return the logger
     */
    @Override
    public final SourceLogger logger() {
        return logger;
    }

    /**
     * Register a module
     *
     * @param module the module to register
     * @return if the module was able to be registered
     */
    @Override
    public final boolean registerModule(final CoreModule module) {
        Optional<CoreModule> mod = modules.keySet().stream().filter((m) -> m.getName().equals(module.getName())).findAny();
        if (mod.isPresent()) {
            CoreModule registered = mod.get();
            Path registrar = modules.get(registered);

            if (registered.isProtected()) {
                if (!KarmaAPI.isTestMode()) {
                    try {
                        Class<?> callerClass = runtime.getCallerClass();
                        Path callerFile = runtime.getFileFrom(callerClass);

                        if (callerFile == null || !callerFile.equals(registrar)) return false;
                    } catch (ClassNotFoundException ex) {
                        return false;
                    }
                } else {
                    if (!securityWarning) {
                        logger.send(LogLevel.WARNING, "({0}) Skipping security check because we are on test-unit. Aren't we?", KarmaPlugin.class);
                        securityWarning = true;
                    }
                }
            }

            modules.remove(registered);
            modules.put(module, registrar);
            return true;
        }

        try {
            Class<?> callerClass = runtime.getCallerClass();
            Path callerFile = runtime.getFileFrom(callerClass);

            if (callerFile == null) return false;
            modules.put(module, callerFile);
            return true;
        } catch (ClassNotFoundException ignored) {}

        return false;
    }
}
