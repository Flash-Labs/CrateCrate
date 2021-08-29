package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.component.Crate;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public final class Listeners {

    @Listener
    public void onInteractBlockPrimary(InteractBlockEvent.Primary.Start event, @Root ServerPlayer player) {
        event.block().location().ifPresent(l -> preInteract(event, l)); //TODO: Preview
    }

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary event, @Root ServerPlayer player) {
        event.block().location().flatMap(l -> preInteract(event, l)).ifPresent(t -> {
            t.first().open(player, t.second()); //TODO: Keys
        });
    }

    private <T extends InteractEvent & Cancellable> Optional<Tuple<Crate, ServerLocation>> preInteract(T event, ServerLocation location) {
        location = location.withBlockPosition(location.blockPosition());
        if (Storage.LOCATIONS.containsKey(location)) {
            event.setCancelled(true);
            var crate = Storage.LOCATIONS.get(location);
            if (event.context().get(EventContextKeys.USED_HAND).map(h -> h.equals(HandTypes.MAIN_HAND.get())).orElse(false)) {
                return Optional.of(Tuple.of(crate, location.add(0.5, 0.5, 0.5)));
            }
        }
        return Optional.empty();
    }

}
