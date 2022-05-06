package dev.flashlabs.cratecrate.internal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.flashlibs.inventory.Element;
import dev.flashlabs.flashlibs.inventory.Page;
import dev.flashlabs.flashlibs.inventory.View;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Utils {

    public static Page preview(Crate crate, Element back) {
        return Inventory.page(
            crate.name(Optional.empty()),
            crate.rewards().stream()
                .map(r -> Element.of(r.getFirst().icon(Optional.of(r.getSecond())), a -> a.callback(v -> {
                    preview(r, Element.of(r.getFirst().icon(Optional.empty()), a2 -> a2.callback(v2 -> {
                        v.open(a2.getPlayer());
                    }))).open(a.getPlayer());
                })))
                .collect(Collectors.toList()),
            back
        );
    }

    public static Page preview(Tuple<? extends Reward, BigDecimal> reward, Element back) {
        return Inventory.page(
            reward.getFirst().name(Optional.of(reward.getSecond())),
            reward.getFirst().prizes().stream()
                .map(p -> Element.of(p.getFirst().icon(Optional.of(p.getSecond()))))
                .collect(Collectors.toList()),
            back
        );
    }

    public static View confirm(Tuple<Crate, Location<World>> crate) {
        return Inventory.menu(Text.of(crate.getFirst().name(Optional.empty())), ImmutableMap.of(
            10, Element.of(Inventory.item(ItemTypes.SLIME_BALL, Text.of("Confirm")), a -> a.callback(v -> {
                a.getPlayer().closeInventory();
                if (checkKeys(a.getPlayer(), crate.getFirst()) && takeKeys(a.getPlayer(), crate.getFirst())) {
                    crate.getFirst().open(a.getPlayer(), crate.getSecond());
                }
            })),
            13, Element.of(crate.getFirst().icon(Optional.empty()), a -> a.callback(v -> {
                preview(crate.getFirst(), Element.of(crate.getFirst().icon(Optional.empty()), a2 -> a2.callback(v2 -> {
                    v.open(a2.getPlayer());
                }))).open(a.getPlayer());
            })),
            16, Element.of(Inventory.item(ItemTypes.MAGMA_CREAM, Text.of("Cancel")), a -> a.callback(v -> {
                a.getPlayer().closeInventory();
            }))
        ));
    }

    public static boolean checkKeys(Player player, Crate crate) {
        List<Tuple<? extends Key, Integer>> missing = crate.keys().stream()
            .filter(k -> !k.getFirst().check(player, k.getSecond()))
            .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            CrateCrate.get().sendMessage(player, "interact.keys.missing",
                "keys", Text.joinWith(Text.of(", "), missing.stream()
                    .map(k -> k.getFirst().name(Optional.of(k.getSecond())))
                    .collect(Collectors.toList())
                )
            );
        }
        return missing.isEmpty();
    }

    public static boolean takeKeys(Player player, Crate crate) {
        List<Tuple<? extends Key, Integer>> taken = Lists.newArrayList();
        for (Tuple<? extends Key, Integer> key : crate.keys()) {
            if (!key.getFirst().take(player, key.getSecond())) {
                if (taken.isEmpty()) {
                    CrateCrate.get().sendMessage(player, "interact.keys.take.failure",
                        "key", key.getFirst().name(Optional.of(key.getSecond()))
                    );
                } else {
                    CrateCrate.get().getContainer().getLogger().error("Incomplete transaction for player " + player.getName() + ": " + taken.stream()
                        .map(k -> k.getFirst().id() + " (x" + k.getSecond() + ")")
                        .collect(Collectors.joining(", ")));
                    CrateCrate.get().sendMessage(player, "interact.keys.take.incomplete",
                        "key", key.getFirst().name(Optional.of(key.getSecond())),
                        "keys", Text.joinWith(Text.of(", "), taken.stream()
                            .map(k -> k.getFirst().name(Optional.of(k.getSecond())))
                            .collect(Collectors.toList())
                        )
                    );
                }
                return false;
            }
            taken.add(key);
        }
        return true;
    }

}
