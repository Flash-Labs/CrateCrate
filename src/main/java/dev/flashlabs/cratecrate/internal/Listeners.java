package dev.flashlabs.cratecrate.internal;

import com.google.common.collect.Lists;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.key.Key;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Listeners {

    @Listener
    public void onInteractBlockPrimary(InteractBlockEvent.Primary event, @Root Player player) {
        event.getTargetBlock().getLocation().ifPresent(l -> preInteract(event, l)); //TODO: Preview
    }

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary event, @Root Player player) {
        event.getTargetBlock().getLocation().flatMap(l -> preInteract(event, l)).ifPresent(t -> {
            List<Tuple<? extends Key, Integer>> missing = t.getFirst().keys().stream()
                .filter(k -> !k.getFirst().check(player, k.getSecond()))
                .collect(Collectors.toList());
            if (missing.isEmpty()) {
                List<Tuple<? extends Key, Integer>> taken = Lists.newArrayList();
                for (Tuple<? extends Key, Integer> key : t.getFirst().keys()) {
                    if (!key.getFirst().take(player, key.getSecond())) {
                        if (taken.isEmpty()) {
                            player.sendMessage(Text.of("One of the keys could not be taken. No keys were taken from you."));
                        } else {
                            CrateCrate.getContainer().getLogger().error("Incomplete transaction for player " + player.getName() + ": " + taken.stream()
                                .map(k -> k.getFirst().id() + " (x" + k.getSecond() + ")")
                                .collect(Collectors.joining(", ")));
                            player.sendMessage(Text.of(
                                "One of the keys could not be taken. Please contact an admin to restore the following keys: ",
                                Text.joinWith(Text.of(", "), taken.stream()
                                    .map(k -> k.getFirst().name(Optional.of(k.getSecond())))
                                    .collect(Collectors.toList())
                                )
                            ));
                        }
                        return;
                    }
                    taken.add(key);
                }
                t.getFirst().open(player, t.getSecond());
            } else {
                player.sendMessage(Text.of(
                    "Missing the following keys: ",
                    Text.joinWith(Text.of(", "), missing.stream()
                        .map(k -> k.getFirst().name(Optional.of(k.getSecond())))
                        .collect(Collectors.toList())
                    )
                ));
            }
        });
    }

    private <T extends HandInteractEvent> Optional<Tuple<Crate, Location<World>>> preInteract(T event, Location<World> location) {
        return Optional.ofNullable(Storage.LOCATIONS.get(location)).flatMap(o -> {
            event.setCancelled(true);
            //TODO: User message for unavailable crate
            return o
                .filter(c -> event.getHandType() == HandTypes.MAIN_HAND)
                .map(c -> Tuple.of(c, location.add(0.5, 0.5, 0.5)));
        });
    }

}
