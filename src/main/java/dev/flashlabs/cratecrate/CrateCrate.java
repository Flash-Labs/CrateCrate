package dev.flashlabs.cratecrate;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("cratecrate")
public final class CrateCrate {

    private static CrateCrate instance;

    private final PluginContainer container;

    @Inject
    public CrateCrate(PluginContainer container) {
        instance = this;
        this.container = container;
    }

    @Listener
    public void onLoaded(LoadedGameEvent event) {
        Config.load();
    }

    @Listener
    public void onRefresh(RefreshGameEvent event) {
        Config.load();
    }

    public static PluginContainer getContainer() {
        return instance.container;
    }

}
