package dev.flashlabs.cratecrate;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.Base;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.component.key.StandardKey;
import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.ItemPrize;
import dev.flashlabs.cratecrate.component.prize.MoneyPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Listeners;
import dev.flashlabs.cratecrate.internal.Storage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

@Plugin(id = "cratecrate")
public final class CrateCrate {

    private static CrateCrate instance;

    private final PluginContainer container;

    @Inject
    private CrateCrate(PluginContainer container) {
        instance = this;
        this.container = container;
    }

    @Listener
    public void onInitialize(GameInitializationEvent event) {
        Crate.TYPES.put(Crate.TYPE.name(), Crate.TYPE);
        Crate.TYPES.put(Crate.class.getName(), Crate.TYPE);
        Reward.TYPES.put(Reward.TYPE.name(), Reward.TYPE);
        Reward.TYPES.put(Reward.class.getName(), Reward.TYPE);
        Prize.TYPES.put(CommandPrize.TYPE.name(), CommandPrize.TYPE);
        Prize.TYPES.put(CommandPrize.class.getName(), CommandPrize.TYPE);
        Prize.TYPES.put(ItemPrize.TYPE.name(), ItemPrize.TYPE);
        Prize.TYPES.put(ItemPrize.class.getName(), ItemPrize.TYPE);
        Prize.TYPES.put(MoneyPrize.TYPE.name(), MoneyPrize.TYPE);
        Prize.TYPES.put(MoneyPrize.class.getName(), MoneyPrize.TYPE);
        Key.TYPES.put(StandardKey.TYPE.name(), StandardKey.TYPE);
        Key.TYPES.put(StandardKey.class.getName(), StandardKey.TYPE);
        Sponge.getEventManager().registerListeners(container, new Listeners());
        Sponge.getCommandManager().register(container, Base.COMMAND, "cratecrate", "crate");
    }

    @Listener
    public void onLoaded(GameStartedServerEvent event) {
        Config.load();
        Storage.load();
    }

    @Listener
    public void onRefresh(GameReloadEvent event) {
        Config.load();
        Storage.load();
    }

    public static PluginContainer getContainer() {
        return instance.container;
    }

}
