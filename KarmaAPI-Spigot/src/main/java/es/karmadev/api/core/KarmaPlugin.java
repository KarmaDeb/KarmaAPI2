package es.karmadev.api.core;

import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.SourceManager;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;
import es.karmadev.api.database.DatabaseManager;
import es.karmadev.api.database.model.JsonDatabase;
import es.karmadev.api.database.model.json.JsonConnection;
import es.karmadev.api.logger.log.UnboundedLogger;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.strings.StringUtils;
import es.karmadev.api.version.Version;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * KarmaAPI spigot karma source
 */
public abstract class KarmaPlugin extends JavaPlugin implements APISource {

    private static boolean principalSet = false;

    private final UnboundedLogger logger;
    private String pluginIdentifier = StringUtils.shuffle(StringUtils.generateSplit(18, '\0'), UUID.randomUUID().toString().replaceAll("-", ""));

    public KarmaPlugin() throws NoSuchFieldException, IllegalAccessException, AlreadyRegisteredException {
        this(false);
    }

    public KarmaPlugin(final boolean registerPrincipal) throws NoSuchFieldException, IllegalAccessException, AlreadyRegisteredException {
        setNaggable(false); //PaperMC naggable warnings bypass
        SourceManager.register(this);
        logger = new UnboundedLogger();
        saveIdentifier();

        if (registerPrincipal && principalSet) return; //We will simply ignore the register principal task if we already have a principal source
        if (registerPrincipal) {
            Class<SourceManager> managerClass = SourceManager.class;
            Field principalField = managerClass.getDeclaredField("principal");
            principalField.setAccessible(true);
            principalField.set(managerClass, name());
            principalField.setAccessible(false);

            principalSet = true;
        }
    }

    @Override
    public final void onEnable() {
        logger.bind(this);
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
    public final @NotNull String name() {
        return getName();
    }

    /**
     * Get the source version
     *
     * @return the source version
     */
    @Override
    public final @NotNull Version version() {
        return Version.parse(getDescription().getVersion());
    }

    /**
     * Get the source description
     *
     * @return the source description
     */
    @Override
    public final @NotNull String description() {
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
    public final @NotNull String[] authors() {
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
}
