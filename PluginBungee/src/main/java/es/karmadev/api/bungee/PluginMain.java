package es.karmadev.api.bungee;

import es.karmadev.api.bungee.core.KarmaPlugin;
import es.karmadev.api.core.source.exception.AlreadyRegisteredException;

public final class PluginMain extends KarmaPlugin {

    public PluginMain() throws NoSuchFieldException, IllegalAccessException, AlreadyRegisteredException {
        super(true, true);
    }

    /**
     * Enable the plugin
     */
    @Override
    public void enable() {

    }

    /**
     * Disable the plugin
     */
    @Override
    public void disable() {

    }
}
