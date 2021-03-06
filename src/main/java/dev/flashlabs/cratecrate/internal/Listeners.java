package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.effect.Effect;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class Listeners {

    @Listener
    public void onInteractBlockPrimary(InteractBlockEvent.Primary event, @Root Player player) {
        event.getTargetBlock().getLocation().flatMap(l -> preInteract(event, player, l)).ifPresent(t -> {
            if (!player.hasPermission("cratecrate.crates." + t.getFirst().id() + ".preview")) {
                CrateCrate.get().sendMessage(player, "interact.crates.preview.no-permission",
                    "crate", t.getFirst().name(Optional.empty())
                );
                t.getFirst().effects().get(Effect.Action.REJECT).forEach(e -> e.getFirst().give(player, t.getSecond(), e.getSecond()));
            } else {
                Utils.preview(t.getFirst(), Inventory.CLOSE).open(player);
                t.getFirst().effects().get(Effect.Action.PREVIEW).forEach(e -> e.getFirst().give(player, t.getSecond(), e.getSecond()));
            }
        });
    }

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary event, @Root Player player) {
        event.getTargetBlock().getLocation().flatMap(l -> preInteract(event, player, l)).ifPresent(t -> {
            if (Utils.checkKeys(player, t.getFirst())) {
                Utils.confirm(t).open(player);
            } else {
                t.getFirst().effects().get(Effect.Action.REJECT).forEach(e -> e.getFirst().give(player, t.getSecond(), e.getSecond()));
            }
        });
    }

    private static <T extends HandInteractEvent> Optional<Tuple<Crate, Location<World>>> preInteract(T event, Player player, Location<World> location) {
        return Optional.ofNullable(Storage.LOCATIONS.get(location)).flatMap(o -> {
            event.setCancelled(true);
            if (!o.isPresent()) {
                CrateCrate.get().sendMessage(player, "interact.crates.unavailable");
            } else if (!player.hasPermission("cratecrate.crates." + o.get().crate().id() + ".base")) {
                CrateCrate.get().sendMessage(player, "interact.crates.no-permission",
                    "crate", o.get().crate().name(Optional.empty())
                );
                o.get().crate().effects().get(Effect.Action.REJECT).forEach(e -> e.getFirst().give(player, location, e.getSecond()));
            } else {
                return o
                    .filter(r -> event.getHandType() == HandTypes.MAIN_HAND)
                    .map(r -> Tuple.of(r.crate(), location.add(0.5, 0.5, 0.5)));
            }
            return Optional.empty();
        });
    }

}
