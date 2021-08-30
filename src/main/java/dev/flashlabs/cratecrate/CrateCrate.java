package dev.flashlabs.cratecrate;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.Base;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.component.key.StandardKey;
import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.ItemPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Listeners;
import dev.flashlabs.cratecrate.internal.Storage;
import org.spongepowered.api.Sponge;
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
    private CrateCrate(PluginContainer container) {
        instance = this;
        this.container = container;
    }

    @Listener
    public void onConstruct(ConstructPluginEvent event) {
        Crate.TYPES.put(Crate.TYPE.name(), Crate.TYPE);
        Crate.TYPES.put(Crate.class.getName(), Crate.TYPE);
        Reward.TYPES.put(Reward.TYPE.name(), Reward.TYPE);
        Reward.TYPES.put(Reward.class.getName(), Reward.TYPE);
        Prize.TYPES.put(CommandPrize.TYPE.name(), CommandPrize.TYPE);
        Prize.TYPES.put(CommandPrize.class.getName(), CommandPrize.TYPE);
        Prize.TYPES.put(ItemPrize.TYPE.name(), ItemPrize.TYPE);
        Prize.TYPES.put(ItemPrize.class.getName(), ItemPrize.TYPE);
        Key.TYPES.put(StandardKey.TYPE.name(), StandardKey.TYPE);
        Key.TYPES.put(StandardKey.class.getName(), StandardKey.TYPE);
        Sponge.eventManager().registerListeners(container, new Listeners());
    }

    @Listener
    public void onLoaded(LoadedGameEvent event) {
        Config.load();
        Storage.load();
    }

    @Listener
    public void onRefresh(RefreshGameEvent event) {
        Config.load();
        Storage.load();
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event) {
        event.register(CrateCrate.container(), Base.COMMAND, "cratecrate", "crate");
    }

    public static PluginContainer container() {
        return instance.container;
    }

}
