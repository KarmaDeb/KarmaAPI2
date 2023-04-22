package es.karmadev.api.core;

import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.schedule.runner.TaskRunner;
import es.karmadev.api.schedule.runner.async.AsyncTaskExecutor;
import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.version.Version;
import es.karmadev.api.version.checker.VersionChecker;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * KarmaKore
 */
public final class KarmaKore extends KarmaSource {

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
     * Get the source update URL
     *
     * @return the source update URL
     */
    @Override
    public String updateURL() {
        return "https://karmadev.es/updater/karmaapi.json";
    }

    /**
     * Initialize the main KarmaSource
     *
     * @return if the source was able to be initialized
     */
    public static KarmaKore INSTANCE() {
        try {
            return SourceManager.getProvider(KarmaKore.class);
        } catch (UnknownProviderException ex) {
            ExceptionCollector.catchException(KarmaSource.class, ex);
            try {
                KarmaKore kore = new KarmaKore();
                kore.start();

                return kore;
            } catch (AlreadyRegisteredException ex2) {
                ExceptionCollector.catchException(KarmaSource.class, ex2);
            }
        }

        return null;
    }
}
