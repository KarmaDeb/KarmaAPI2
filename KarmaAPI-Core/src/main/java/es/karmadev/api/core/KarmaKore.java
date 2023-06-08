package es.karmadev.api.core;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.database.DatabaseManager;
import es.karmadev.api.database.exception.ProtectedEngineException;
import es.karmadev.api.database.model.JsonDatabase;
import es.karmadev.api.database.model.json.JsonConnection;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.schedule.runner.TaskRunner;
import es.karmadev.api.schedule.runner.async.AsyncTaskExecutor;
import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.security.LockedProperties;
import es.karmadev.api.security.PermissionManager;
import es.karmadev.api.security.permission.PermissionFactory;
import es.karmadev.api.security.permission.PermissionNode;
import es.karmadev.api.strings.StringUtils;
import es.karmadev.api.strings.placeholder.PlaceholderEngine;
import es.karmadev.api.strings.placeholder.engine.SimpleEngine;
import es.karmadev.api.strings.placeholder.engine.SimplePlaceholder;
import es.karmadev.api.version.Version;
import es.karmadev.api.version.checker.VersionChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * KarmaKore
 */
public final class KarmaKore extends KarmaSource {

    private final Map<String, PlaceholderEngine> engines = new ConcurrentHashMap<>();
    private final Map<String, CoreModule> modules = new ConcurrentHashMap<>();
    private String koreIdentifier = StringUtils.shuffle(StringUtils.generateSplit(18, '\0'), UUID.randomUUID().toString().replaceAll("-", ""));

    /**
     * Initialize the KarmaAPI core
     *
     * @throws AlreadyRegisteredException if the core has been
     * already initialized previously
     */
    private KarmaKore() throws AlreadyRegisteredException {
        super("KarmaSource",
                Version.parse(KarmaAPI.VERSION, KarmaAPI.BUILD),
                "KarmaAPI is an API that helps in the development of java applications",
                "KarmaDev");

        SourceManager.register(this);
        SimpleEngine engine = new SimpleEngine();
        engine.protect();
        engine.register(new SimplePlaceholder<>("version", KarmaAPI.VERSION).asProtected());
        engine.register(new SimplePlaceholder<>("build", KarmaAPI.BUILD).asProtected());
        engine.register(new SimplePlaceholder<>("date", KarmaAPI.COMPILE_DATE).asProtected());

        engines.put("default", engine);
        modules.put("permissions", new PermissionManager<PermissionNode<Object>, Object>() {
            /**
             * Get if the module is protected
             *
             * @return if this is a protected module
             */
            @Override
            public boolean isProtected() {
                return false;
            }

            /**
             * Get the permission factory
             *
             * @return the safe permission factory
             */
            @Override
            public PermissionFactory getFactory() {
                return null;
            }

            @Override
            public boolean register(final PermissionNode<Object> permission) {
                return false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public PermissionNode<Object>[] getByName(final String name) {
                return new PermissionNode[0];
            }

            @Override
            public PermissionNode<Object> getByIndex(final int index) {
                return null;
            }

            /**
             * Get if the holder has the specified permission
             *
             * @param o    the permission holder
             * @param node the permission node
             * @return if the holder has the permission
             */
            @Override
            public boolean hasPermission(Object o, PermissionNode<Object> node) {
                return false;
            }

            /**
             * Grant a permission
             *
             * @param o    the permission holder
             * @param node the permission node
             */
            @Override
            public void grantPermission(Object o, PermissionNode<Object> node) {

            }

            /**
             * Revoke a permission
             *
             * @param o    the permission holder
             * @param node the permission node
             */
            @Override
            public void revokePermission(Object o, PermissionNode<Object> node) {

            }
        });
    }

    /**
     * Start the source
     */
    @Override
    public void start() {
        if (directory == null) directory = runtime.getFile().getParent().resolve(name);
        if (console instanceof UnboundedLogger) {
            UnboundedLogger unbound = (UnboundedLogger) console;
            unbound.bind(this);

            unbound.log("Beep!");

            TaskRunner runner = new AsyncTaskExecutor(5, 10, TimeUnit.MINUTES);
            runner.setRepeating(true);

            runner.on(TaskEvent.RESTART, () -> {
                unbound.send("Checking for KarmaAPI updates...", LogLevel.DEBUG);
                VersionChecker checker = new VersionChecker(this);
                checker.check().onComplete((task) -> {
                    Throwable error = task.error();
                    if (error == null) {
                        Version latest = checker.getVersion();
                        if (latest != null && !latest.equals(version) && latest.compareTo(version) > 0) {
                            String[] changelog = checker.getChangelog();
                            unbound.send("KarmaAPI is out of date! A new version has been found ({0}). Current is: {1}", latest, version);
                            unbound.send("&7--&b CHANGELOG&7 --");
                            for (String line : changelog) unbound.send(line);

                            URL[] updateURLs = checker.getUpdateURLs();
                            unbound.send("&7--&b UPDATE&7 --");
                            for (URL url : updateURLs) unbound.send("&e" + url);
                        }
                    } else {
                        unbound.log(error, "An error occurred while checking for updates");
                    }
                });
            });
            runner.start();
        }

        try {
            DatabaseManager.register(new JsonDatabase());
        } catch (ProtectedEngineException ex) {
            ExceptionCollector.catchException(KarmaKore.class, ex);
        }

        saveIdentifier();
        LockedProperties locked = (LockedProperties) KarmaAPI.properties;
        locked.setProtected("identifier", koreIdentifier);
    }

    /**
     * Kill the source
     */
    @Override
    public void kill() {
        SourceLogger logger = LogManager.getLogger(this);
        logger.log(LogLevel.INFO, "Boop!");
    }

    /**
     * Initialize the main KarmaSource
     *
     * @return if the source was able to be initialized
     */
    public static APISource INSTANCE() {
        try {
            APISource source = SourceManager.getPrincipal();
            if (source == null) {
                source = new KarmaKore();
                ((KarmaKore) source).start();

                try {
                    Class<SourceManager> manager = SourceManager.class;
                    Field PRINCIPAL = manager.getDeclaredField("principal");
                    PRINCIPAL.setAccessible(true);
                    PRINCIPAL.set(manager, "KarmaSource");
                    PRINCIPAL.setAccessible(false);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    ExceptionCollector.catchException(KarmaKore.class, ex);
                }

                SourceManager.register(source);
            }

            return source;
        } catch (UnknownProviderException | AlreadyRegisteredException ex2) {
            ExceptionCollector.catchException(KarmaKore.class, ex2);
        }

        return null;
    }

    /**
     * Get the source identifier
     *
     * @return the source identifier
     */
    @Override
    public @NotNull String identifier() {
        return koreIdentifier;
    }

    /**
     * Get the source update URI
     *
     * @return the source update URI
     */
    @Override
    public @Nullable URI updateURI() {
        return null;
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
    public @NotNull PlaceholderEngine placeholderEngine(final String name) {
        return engines.computeIfAbsent(name, (engine) -> new SimpleEngine());
    }

    /**
     * Get a module by name
     *
     * @param name the module name
     * @return the module
     */
    @Override
    public @Nullable CoreModule getModule(final String name) {
        return modules.getOrDefault(name, null);
    }

    /**
     * Register a module
     *
     * @param module the module to register
     * @return if the module was able to be registered
     */
    @Override
    public boolean registerModule(final CoreModule module) {
        if (modules.containsKey(module.getName())) return false;
        return modules.put(module.getName(), module) == null;
    }

    /**
     * Load an identifier
     *
     * @param name the identifier name
     */
    @Override
    public void loadIdentifier(final String name) {
        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(new JsonDatabase());
        JsonConnection connection = database.grabConnection("identifiers");

        String stored = connection.getString(name);
        if (stored != null) koreIdentifier = stored;
    }

    /**
     * Generate and save an identifier
     *
     * @param name the identifier name
     */
    @Override
    public void saveIdentifier(final String name) {
        JsonDatabase database = (JsonDatabase) DatabaseManager.getEngine("json").orElse(new JsonDatabase());
        JsonConnection connection = database.grabConnection("identifiers");

        String stored = connection.getString(name);
        if (stored != null) {
            koreIdentifier = stored;
            return;
        }

        if (ObjectUtils.isNullOrEmpty(koreIdentifier)) {
            koreIdentifier = StringUtils.generateSplit(18, '\0');
        }

        connection.set(name, koreIdentifier);
        connection.save();
    }
}
