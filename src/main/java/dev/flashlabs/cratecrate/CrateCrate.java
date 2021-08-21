package dev.flashlabs.cratecrate;

import com.google.inject.Inject;
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

    public static PluginContainer getContainer() {
        return instance.container;
    }

}
