package dev.flashlabs.cratecrate;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.Base;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
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
    public void onConstruct(ConstructPluginEvent event) {
        Crate.TYPES.put(Crate.TYPE.name, Crate.TYPE);
        Reward.TYPES.put(Reward.TYPE.name, Reward.TYPE);
        Prize.TYPES.put(CommandPrize.TYPE.name, CommandPrize.TYPE);
    }

    @Listener
    public void onLoaded(LoadedGameEvent event) {
        Config.load();
    }

    @Listener
    public void onRefresh(RefreshGameEvent event) {
        Config.load();
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event) {
        event.register(CrateCrate.getContainer(), Base.COMMAND, "cratecrate", "crate");
    }

    public static PluginContainer getContainer() {
        return instance.container;
    }

}
