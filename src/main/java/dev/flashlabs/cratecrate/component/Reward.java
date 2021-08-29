package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
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
    public net.kyori.adventure.text.Component name(Optional<Integer> weight) {
        if (name.isPresent()) {
            var replaced = name.get().replaceAll("\\$\\{value}", weight.map(String::valueOf).orElse("${value}"));
            return LegacyComponentSerializer.legacyAmpersand().deserialize(replaced);
        } else if (prizes.size() == 1) {
            return prizes.get(0).first().name(Optional.of(prizes.get(0).second()));
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
    public List<net.kyori.adventure.text.Component> lore(Optional<Integer> weight) {
        if (lore.isPresent()) {
            return lore.get().stream().map(s -> {
                s = s.replaceAll("\\$\\{value}", weight.map(String::valueOf).orElse("${value}"));
                return LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent();
            }).toList();
        } else if (prizes.size() == 1) {
            return prizes.get(0).first().lore(Optional.of(prizes.get(0).second()));
        } else {
            return prizes.stream().map(p -> p.first().name(Optional.of(p.second()))).toList();
        }
    }

    /**
     * Returns the icon of this reward, defaulting to either the icon of the
     * first prize (if only one prize exists) or a book (if multiple prizes
     * exist). If the icon does not have a defined display name or lore, it is
     * set to this reward's name/lore.
     */
    @Override
    public ItemStack icon(Optional<Integer> weight) {
        var base = icon.map(ItemStackSnapshot::createStack).orElseGet(() -> {
            if (prizes.size() == 1) {
                return prizes.get(0).first().icon(Optional.of(prizes.get(0).second()));
            } else {
                return ItemStack.of(ItemTypes.BOOK, 1);
            }
        });
        if (base.get(Keys.CUSTOM_NAME).isEmpty()) {
            base.offer(Keys.CUSTOM_NAME, name(weight));
        }
        //TODO: Replace with base.get(Keys.LORE).isAbsent(); see SpongePowered/Sponge#3512
        if (!base.toContainer().contains(DataQuery.of("UnsafeData", "display", "Lore"))) {
            base.offer(Keys.LORE, lore(weight));
        }
        return base;
    }

    public ImmutableList<Tuple<? extends Prize, ?>> prizes() {
        return prizes;
    }

    public void give(User user) {
        prizes.forEach(p -> p.first().give(user, p.second()));
    }

    public static final class RewardType extends Type<Reward, Integer> {

        public RewardType() {
            super("Reward", CrateCrate.container());
        }

        @Override
        public boolean matches(ConfigurationNode node) {
            return true;
        }

        /**
         * Deserializes a reward, defined as:
         *
         * <pre>{@code
         * Reward:
         *     name: Optional<String>
         *     lore: Optional<List<String>>
         *     icon: Optional<ItemType>
         *     prizes: List<PrizeReference>
         * }</pre>
         */
        @Override
        public Reward deserializeComponent(ConfigurationNode node) throws SerializationException {
            var name = Optional.ofNullable(node.node("name").get(String.class));
            var lore = node.node("lore").isList()
                ? Optional.ofNullable(node.node("lore").getList(String.class)).map(ImmutableList::copyOf)
                : Optional.<ImmutableList<String>>empty();
            var icon = node.hasChild("icon")
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.node("icon")).createSnapshot())
                : Optional.<ItemStackSnapshot>empty();
            var prizes = new ArrayList<Tuple<? extends Prize, ?>>();
            for (ConfigurationNode prize : node.node("prizes").childrenList()) {
                var component = prize.isList() ? prize.node(0) : prize;
                var values = prize.childrenList().subList(prize.isList() ? 1 : 0, prize.childrenList().size());
                prizes.add(Config.resolvePrizeType(component).deserializeReference(component, values));
            }
            return new Reward(String.valueOf(node.key()), name, lore, icon, ImmutableList.copyOf(prizes));
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, Reward component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserialize a reward reference, defined as:
         *
         * <pre>{@code
         * RewardReference:
         *     node: Reward | String (Reward id) | PrizeReference (map/string)
         *        weight: Integer (required for Reward, required for
         *            PrizeReference when reference value is not defined)
         *     values: [
         *        list... (limit 1 for Reward/String),
         *        Optional<Integer> (required for String, required for
         *            PrizeReference when weight is not defined)
         *     ]
         * }</pre>
         */
        @Override
        public Tuple<Reward, Integer> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            Reward reward;
            if (node.isMap()) {
                if (node.hasChild("prizes")) {
                    reward = deserializeComponent(node);
                    reward = new Reward("Reward@" + node.path(), reward.name, reward.lore, reward.icon, reward.prizes);
                } else {
                    var prize = Config.resolvePrizeType(node).deserializeReference(node, values.subList(0, values.isEmpty() ? 0 : values.size() - 1));
                    reward = new Reward("Reward@" + node.path(), Optional.empty(), Optional.empty(), Optional.empty(), ImmutableList.of(prize));
                }
                Config.REWARDS.put(reward.id, reward);
            } else {
                var identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.REWARDS.containsKey(identifier)) {
                    reward = Config.REWARDS.get(identifier);
                } else {
                    var prize = Config.resolvePrizeType(node).deserializeReference(node, values.subList(0, values.isEmpty() ? 0 : values.size() - 1));
                    reward = new Reward(identifier, Optional.empty(), Optional.empty(), Optional.empty(), ImmutableList.of(prize));
                    Config.REWARDS.put(reward.id, reward);
                }
            }
            //TODO: Validate reference value counts and existence
            var value = (!values.isEmpty() ? values.get(0) : node.node("weight")).getInt();
            return Tuple.of(reward, value);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<Reward, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
