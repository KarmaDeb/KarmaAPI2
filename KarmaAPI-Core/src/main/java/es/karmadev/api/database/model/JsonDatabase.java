package es.karmadev.api.database.model;

import es.karmadev.api.core.KarmaAPI;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.database.DatabaseEngine;
import es.karmadev.api.database.model.json.JsonConnection;
import es.karmadev.api.logger.log.console.LogLevel;

import java.io.File;
import java.nio.file.Path;

/**
 * Json database model
 */
public final class JsonDatabase implements DatabaseEngine {

    private static boolean securityWarning = false;

    /**
     * Initialize the json database
     *
     * @throws IllegalStateException if the database fails to create
     * @throws SecurityException if the database is tried to be created
     * out of the API
     */
    public JsonDatabase() throws IllegalStateException, SecurityException {
        APISource kore = KarmaKore.INSTANCE();
        if (kore == null) throw new IllegalStateException("Cannot create a json database without main kore");

        SourceRuntime runtime = kore.runtime();
        try {
            Class<?> callerClass = runtime.getCallerClass();
            if (callerClass == null) throw new IllegalStateException("Cannot create a json database without a valid caller class (Try updating java)");

            Path source = runtime.getFile();
            Path caller = runtime.getFileFrom(callerClass);

            if (source == null) throw new IllegalStateException("Cannot create a json database without a valid source file");
            if (caller == null) throw new IllegalStateException("Cannot create a json database using a virtual JVM");

            if (KarmaAPI.isTestMode()) {
                if (!securityWarning) {
                    kore.logger().send(LogLevel.WARNING, "({0}) Skipping security check because we are on test-unit. Aren't we?", JsonDatabase.class);
                    securityWarning = true;
                }

                return; //We are in test unit
            }

            if (!source.equals(caller)) throw new SecurityException("Cannot create json database out of KarmaAPI");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get if the engine is protected
     *
     * @return if the engine is protected
     */
    @Override
    public boolean isProtected() {
        return true;
    }

    /**
     * Get the engine name
     *
     * @return the engine name
     */
    @Override
    public String getName() {
        return "json";
    }

    /**
     * Grab a connection from the engine
     * connection pool (if any)
     *
     * @param name the connection name
     * @return a database connection
     */
    @Override
    public JsonConnection grabConnection(final String name) {
        APISource source = KarmaKore.INSTANCE();
        if (source == null) throw new IllegalStateException("Cannot grab a json connection without the main kore");

        Path file = source.workingDirectory().resolve("databases");
        if (name.contains(File.pathSeparator)) {
            String[] data = name.split(File.pathSeparator);
            for (String str : data) {
                file = file.resolve(str);
            }
        } else {
            file = file.resolve(name + ".json");
        }

        return new JsonConnection(file, null, null);
    }
}
