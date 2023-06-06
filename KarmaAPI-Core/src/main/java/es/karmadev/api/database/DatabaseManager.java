package es.karmadev.api.database;

import es.karmadev.api.database.exception.ProtectedEngineException;
import es.karmadev.api.object.ObjectUtils;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KarmaAPI database manager
 */
public class DatabaseManager {

    private final static Set<DatabaseEngine> engines = ConcurrentHashMap.newKeySet();

    /**
     * Register a new database engine
     *
     * @param engine the database engine
     * @throws ProtectedEngineException if the engine is already registered
     * and is protected
     * @throws NullPointerException if the engine name is null or empty
     */
    public static void register(final DatabaseEngine engine) throws ProtectedEngineException, NullPointerException {
        if (engine == null) throw new NullPointerException("Cannot register null database engine");
        String eName = engine.getName();
        ObjectUtils.assertNullOrEmpty(eName, "Cannot register null-named database engine");

        Optional<DatabaseEngine> existingEngine = engines.stream().filter((dbEngine) -> dbEngine.getName().equals(eName)).findAny();
        if (existingEngine.isPresent()) {
            DatabaseEngine e1 = existingEngine.get();
            if (e1.isProtected()) throw new ProtectedEngineException(e1, engine);

            engines.remove(e1);
        }

        engines.add(engine);
    }

    /**
     * Get a database engine by its name
     *
     * @param name the engine name
     * @return the engine
     */
    public static Optional<DatabaseEngine> getEngine(final String name) {
        return engines.stream().filter((dbEngine) -> dbEngine.getName().equals(name)).findAny();
    }

    /**
     * Get all the available engines
     *
     * @return the engines
     */
    public static String[] getEngines() {
        String[] names = new String[engines.size()];

        int index = 0;
        for (DatabaseEngine engine : engines) names[index++] = engine.getName();

        return names;
    }
}
