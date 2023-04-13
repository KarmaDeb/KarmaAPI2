package es.karmadev.api.core;

import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.core.source.exception.UnknownProviderException;
import es.karmadev.api.logger.LogManager;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.version.Version;

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
        return "https://karmadev.es/tests/api2.versions.json";
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
