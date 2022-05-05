package dev.flashlabs.cratecrate.internal;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
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
                CrateCrate.get().sendMessage(player, "interact.crates.preview.no-permission");
            } else {
                Utils.preview(t.getFirst(), Inventory.CLOSE).open(player);
            }
        });
    }

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary event, @Root Player player) {
        event.getTargetBlock().getLocation().flatMap(l -> preInteract(event, player, l)).ifPresent(t -> {
            if (Utils.checkKeys(player, t.getFirst())) {
                Utils.confirm(t).open(player);
            }
        });
    }

    private static <T extends HandInteractEvent> Optional<Tuple<Crate, Location<World>>> preInteract(T event, Player player, Location<World> location) {
        return Optional.ofNullable(Storage.LOCATIONS.get(location)).flatMap(o -> {
            event.setCancelled(true);
            if (!o.isPresent()) {
                CrateCrate.get().sendMessage(player, "interact.crates.unavailable");
            } else if (!player.hasPermission("cratecrate.crates." + o.get().id() + ".base")) {
                CrateCrate.get().sendMessage(player, "interact.crates.no-permission");
            } else {
                return o
                    .filter(c -> event.getHandType() == HandTypes.MAIN_HAND)
                    .map(c -> Tuple.of(c, location.add(0.5, 0.5, 0.5)));
            }
            return Optional.empty();
        });
    }

}
