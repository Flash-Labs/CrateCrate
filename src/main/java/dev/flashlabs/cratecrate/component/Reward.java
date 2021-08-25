package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.prize.Prize;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Reward extends Component<Integer> {

    public static final RewardType TYPE = new RewardType();
    public static final Map<String, Type<? extends Reward, ?>> TYPES = new HashMap<>();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final ImmutableList<Tuple<? extends Prize, ?>> prizes;

    public Reward(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        ImmutableList<Tuple<? extends Prize, ?>> prizes
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.prizes = prizes;
    }

    /**
     * Returns the name of this reward, defaulting to either the name of the
     * first prize (if only one prize exists) or this reward's id (if multiple
     * prizes exist). If a reference value is provided, it replaces
     * {@code ${weight}}.
     */
    @Override
    public net.kyori.adventure.text.Component getName(Optional<Integer> weight) {
        if (name.isPresent()) {
            var replaced = name.get().replaceAll("\\$\\{value}", weight.map(String::valueOf).orElse("${value}"));
            return LegacyComponentSerializer.legacyAmpersand().deserialize(replaced);
        } else if (prizes.size() == 1) {
            return prizes.get(0).first().getName(Optional.of(prizes.get(0).second()));
        } else {
            return net.kyori.adventure.text.Component.text(id);
        }
    }

    /**
     * Returns the lore of this reward, defaulting to either the lore of the
     * first prize (if only one prize exists) or the names of all prizes (if
     * multiple prizes exist). If a reference value is provided, it replaces
     * {@code ${weight}}.
     */
    @Override
    public List<net.kyori.adventure.text.Component> getLore(Optional<Integer> weight) {
        if (lore.isPresent()) {
            return lore.get().stream().map(s -> {
                s = s.replaceAll("\\$\\{value}", weight.map(String::valueOf).orElse("${value}"));
                return LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent();
            }).toList();
        } else if (prizes.size() == 1) {
            return prizes.get(0).first().getLore(Optional.of(prizes.get(0).second()));
        } else {
            return prizes.stream().map(p -> p.first().getName(Optional.of(p.second()))).toList();
        }
    }

    /**
     * Returns the icon of this reward, defaulting to either the icon of the
     * first prize (if only one prize exists) or a book (if multiple prizes
     * exist). If the icon does not have a defined display name or lore, it is
     * set to this reward's name/lore.
     */
    @Override
    public ItemStack getIcon(Optional<Integer> weight) {
        var base = icon.map(ItemStackSnapshot::createStack).orElseGet(() -> {
            if (prizes.size() == 1) {
                return prizes.get(0).first().getIcon(Optional.of(prizes.get(0).second()));
            } else {
                return ItemStack.of(ItemTypes.BOOK, 1);
            }
        });
        if (base.get(Keys.DISPLAY_NAME).isEmpty()) {
            base.offer(Keys.DISPLAY_NAME, getName(weight));
        }
        if (base.get(Keys.LORE).isEmpty()) {
            base.offer(Keys.LORE, getLore(weight));
        }
        return base;
    }

    public ImmutableList<Tuple<? extends Prize, ?>> getPrizes() {
        return prizes;
    }

    public void give(User user) {
        prizes.forEach(p -> p.first().give(user, p.second()));
    }

    public static final class RewardType extends Type<Reward, Integer> {

        public RewardType() {
            super("Reward", CrateCrate.getContainer());
        }

        @Override
        public boolean matches(ConfigurationNode node) {
            return true;
        }

        @Override
        public Reward deserializeComponent(ConfigurationNode node) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, Reward component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public Tuple<Reward, Integer> deserializeReference(ConfigurationNode node, List<ConfigurationNode> values) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<Reward, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
