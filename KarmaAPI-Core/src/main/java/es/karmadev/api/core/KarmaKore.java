package es.karmadev.api.core;

import es.karmadev.api.logger.LogLevel;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.console.ConsoleLogger;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.core.version.Version;

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
                Version.of(2, 0, 0),
                "KarmaAPI is an API that helps in the development of java applications",
                "KarmaDev");

        SourceManager.register(this);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConsoleLogger logger = LogManager.getLogger(this);
            logger.log(LogLevel.INFO, "Shutting down KarmaAPI");
        }));
    }

    /**
     * Start the source
     */
    @Override
    public void start() {
        if (directory == null) directory = runtime.getFile().getParent().resolve(name);
        if (console == null) console = LogManager.getLogger(this);
    }

    /**
     * Kill the source
     */
    @Override
    public void kill() {

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