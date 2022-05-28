package dev.flashlabs.cratecrate.component.opener;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Inventory;
import dev.flashlabs.cratecrate.internal.Utils;
import dev.flashlabs.flashlibs.inventory.Element;
import dev.flashlabs.flashlibs.inventory.Layout;
import dev.flashlabs.flashlibs.inventory.View;
import dev.willbanders.storm.config.Node;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Optional;

public final class GuiOpener extends Opener {

    private static final GuiOpener INSTANCE = new GuiOpener();

    @Override
    public boolean open(Player player, Crate crate, Location<World> location) {
        Tuple<Reward, BigDecimal> reward = crate.roll(player);
        View.builder(InventoryArchetypes.DISPENSER)
            .title(crate.name(Optional.empty()))
            .build(CrateCrate.get().getContainer())
            .define(Layout.builder(3, 3)
                .set(Element.of(reward.getFirst().icon(Optional.of(reward.getSecond())), a -> a.callback(v -> {
                    Utils.preview(reward, Element.of(crate.icon(Optional.empty()), a2 -> a2.callback(v2 -> {
                        v.open(a2.getPlayer());
                    }))).open(a.getPlayer());
                })), 4)
                .set(Element.of(Inventory.pane(DyeColors.YELLOW)), 0, 2, 6, 8)
                .set(Element.of(Inventory.pane(DyeColors.ORANGE)), 1, 3, 5, 7)
                .build())
            .open(player);
        return crate.give(player, reward, location);
    }

    public static GuiOpener deserialize(Node node) {
        return INSTANCE;
    }

}
