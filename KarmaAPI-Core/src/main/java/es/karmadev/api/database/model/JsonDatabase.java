package es.karmadev.api.database.model;

import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.database.DatabaseEngine;
import es.karmadev.api.database.model.json.JsonConnection;

import java.io.File;
import java.nio.file.Path;

/**
 * Json database model
 */
public final class JsonDatabase implements DatabaseEngine {

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
            int index = 0;
            for (String str : data) {
                if (index + 1 == data.length) {
                    if (!str.endsWith(".json")) {
                        str += ".json";
                    }
                }

                file = file.resolve(str);
            }
        } else {
            if (name.endsWith(".json")) {
                file = file.resolve(name);
            } else {
                file = file.resolve(name + ".json");
            }
        }

        return new JsonConnection(file, null, null);
    }
}
