package dev.flashlabs.cratecrate;

import com.google.inject.Inject;
import dev.flashlabs.cratecrate.command.Base;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.effect.*;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.component.key.StandardKey;
import dev.flashlabs.cratecrate.component.prize.CommandPrize;
import dev.flashlabs.cratecrate.component.prize.ItemPrize;
import dev.flashlabs.cratecrate.component.prize.MoneyPrize;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Listeners;
import dev.flashlabs.cratecrate.internal.Storage;
import dev.flashlabs.flashlibs.plugin.PluginInstance;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

@Plugin(id = "cratecrate")
public final class CrateCrate extends PluginInstance {

    private static CrateCrate instance;

    @Inject
    private CrateCrate(PluginContainer container) {
        super(container);
        instance = this;
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
        Effect.TYPES.put(FireworkEffect.TYPE.name(), FireworkEffect.TYPE);
        Effect.TYPES.put(FireworkEffect.class.getName(), FireworkEffect.TYPE);
        Effect.TYPES.put(ParticleEffect.TYPE.name(), ParticleEffect.TYPE);
        Effect.TYPES.put(ParticleEffect.class.getName(), ParticleEffect.TYPE);
        Effect.TYPES.put(PotionEffect.TYPE.name(), PotionEffect.TYPE);
        Effect.TYPES.put(PotionEffect.class.getName(), PotionEffect.TYPE);
        Effect.TYPES.put(SoundEffect.TYPE.name(), SoundEffect.TYPE);
        Effect.TYPES.put(SoundEffect.class.getName(), SoundEffect.TYPE);
        Sponge.getEventManager().registerListeners(container, new Listeners());
        commands.register(Base.class);
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
        commands.reload();
        messages.reload();
    }

    public static CrateCrate get() {
        return instance;
    }

}
