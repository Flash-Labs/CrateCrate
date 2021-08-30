package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.key.Key;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
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

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Listeners {

    @Listener
    public void onInteractBlockPrimary(InteractBlockEvent.Primary.Start event, @Root ServerPlayer player) {
        event.block().location().ifPresent(l -> preInteract(event, l)); //TODO: Preview
    }

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary event, @Root ServerPlayer player) {
        event.block().location().flatMap(l -> preInteract(event, l)).ifPresent(t -> {
            var missing = t.first().keys().stream()
                .filter(k -> !k.first().check(player.user(), k.second()))
                .toList();
            if (missing.isEmpty()) {
                var taken = new ArrayList<Tuple<? extends Key, Integer>>();
                for (var key : t.first().keys()) {
                    if (!key.first().take(player.user(), key.second())) {
                        if (taken.isEmpty()) {
                            player.sendMessage(Component.text("One of the keys could not be taken. No keys were taken from you."));
                        } else {
                            CrateCrate.container().logger().error("Incomplete transaction for player " + player.name() + ": " + taken.stream()
                                .map(k -> k.first().id() + " (x" + k.second() + ")")
                                .collect(Collectors.joining(", ")));
                            player.sendMessage(Component.text("One of the keys could not be taken. Please contact an admin to restore the following keys: ")
                                .append(Component.join(Component.text(", "), taken.stream()
                                    .map(k -> k.first().name(Optional.of(k.second())))
                                    .toList())));
                        }
                        return;
                    }
                    taken.add(key);
                }
                t.first().open(player, t.second());
            } else {
                player.sendMessage(Identity.nil(), Component.text("Missing the following keys: ")
                    .append(Component.join(Component.text(", "), missing.stream()
                        .map(k -> k.first().name(Optional.of(k.second())))
                        .toList())));
            }
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
