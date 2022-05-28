package dev.flashlabs.cratecrate.component.opener;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Crate;
import dev.flashlabs.cratecrate.component.Reward;
import dev.flashlabs.cratecrate.internal.Inventory;
import dev.flashlabs.cratecrate.internal.Utils;
import dev.flashlabs.flashlibs.inventory.Element;
import dev.flashlabs.flashlibs.inventory.Layout;
import dev.flashlabs.flashlibs.inventory.View;
import dev.willbanders.storm.config.Node;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RouletteOpener extends Opener {

    private static final RouletteOpener INSTANCE = new RouletteOpener();
    private static final ImmutableList<Element> PANES = ImmutableList.of(
        Element.of(Inventory.pane(DyeColors.RED)),
        Element.of(Inventory.pane(DyeColors.ORANGE)),
        Element.of(Inventory.pane(DyeColors.YELLOW)),
        Element.of(Inventory.pane(DyeColors.LIME)),
        Element.of(Inventory.pane(DyeColors.LIGHT_BLUE)),
        Element.of(Inventory.pane(DyeColors.BLUE)),
        Element.of(Inventory.pane(DyeColors.PURPLE)),
        Element.of(Inventory.pane(DyeColors.MAGENTA)),
        Element.of(Inventory.pane(DyeColors.PINK))
    );
    private static final ImmutableSet<Integer> TIMES = ImmutableSet.of(
        14, 16, 18, 20, 22, 25, 28, 31, 35,
        39, 44, 49, 55, 62, 70, 79, 89, 100
    );
    private static final Random RANDOM = new Random();

    @Override
    public boolean open(Player player, Crate crate, Location<World> location) {
        AtomicBoolean received = new AtomicBoolean(false);
        View view = View.builder(InventoryArchetypes.CHEST)
            .title(crate.name(Optional.empty()))
            .onClose(a -> a.getEvent().setCancelled(!received.get()))
            .build(CrateCrate.get().getContainer());
        Task.builder()
            .execute(new Consumer<Task>() {

                private int frame = 0;
                private final int selection = RANDOM.nextInt(9);
                private final List<Element> panes = Lists.newArrayList(PANES);
                private final List<Tuple<Reward, BigDecimal>> rewards = IntStream.range(0, 9)
                    .mapToObj(i -> crate.roll(player))
                    .collect(Collectors.toList());
                private final List<Element> icons = rewards.stream()
                    .map(r -> Element.of(r.getFirst().icon(Optional.of(r.getSecond())), a -> a.callback(v -> {
                        if (received.get()) {
                            Utils.preview(r, Element.of(crate.icon(Optional.empty()), a2 -> a2.callback(v2 -> {
                                v.open(a2.getPlayer());
                            }))).open(a.getPlayer());
                        }
                    })))
                    .collect(Collectors.toList());

                @Override
                public void accept(Task task) {
                    Layout.Builder builder = Layout.builder(3, 9);
                    for (int i = 0; i < 9; i++) {
                        Element pane = i != 4 ? panes.get(i) : Element.of(ItemStack.builder()
                            .fromSnapshot(panes.get(i).getItem())
                            .add(Keys.ITEM_ENCHANTMENTS, ImmutableList.of(Enchantment.of(EnchantmentTypes.POWER, 1)))
                            .build());
                        builder.set(pane, i, i + 18);
                    }
                    Collections.rotate(panes, -1);
                    if (frame++ + selection <= 13 || TIMES.contains(frame + selection)) {
                        Collections.rotate(icons, 1);
                        for (int i = 0; i < 9; i++) {
                            builder.set(icons.get(i), i + 9);
                        }
                        if (frame + selection == 100) {
                            crate.give(player, rewards.get(selection), location);
                            received.getAndSet(true);
                            task.cancel();
                        }
                    }
                    view.update(builder.build());
                }

            })
            .intervalTicks(1)
            .submit(CrateCrate.get().getContainer());
        view.open(player);
        return true;
    }

    public static RouletteOpener deserialize(Node node) {
        return INSTANCE;
    }

}
