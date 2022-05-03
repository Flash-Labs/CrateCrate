package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.SerializationException;
import dev.flashlabs.cratecrate.internal.Serializers;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public final class Reward extends Component<BigDecimal> {

    public static final RewardType TYPE = new RewardType();
    public static final Map<String, Type<? extends Reward, ?>> TYPES = new HashMap<>();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final ImmutableList<Tuple<? extends Prize, ?>> prizes;

    private Reward(
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
     * prizes exist). The reference value is currently unused.
     */
    @Override
    public Text name(Optional<BigDecimal> unused) {
        if (name.isPresent()) {
            return TextSerializers.FORMATTING_CODE.deserialize(name.get());
        } else if (prizes.size() == 1) {
            return prizes.get(0).getFirst().name(Optional.of(prizes.get(0).getSecond()));
        } else {
            return Text.of(id);
        }
    }

    /**
     * Returns the lore of this reward, defaulting to either the lore of the
     * first prize (if only one prize exists) or the names of all prizes (if
     * multiple prizes exist). If a reference value is provided, it replaces
     * {@code ${weight}}.
     */
    @Override
    public List<Text> lore(Optional<BigDecimal> weight) {
        if (lore.isPresent()) {
            return lore.get().stream()
                .map(s -> {
                    s = s.replaceAll("\\$\\{weight}", weight.map(String::valueOf).orElse("${weight}"));
                    return TextSerializers.FORMATTING_CODE.deserialize(s);
                })
                .collect(Collectors.toList());
        } else if (prizes.size() == 1) {
            return prizes.get(0).getFirst().lore(Optional.of(prizes.get(0).getSecond()));
        } else {
            return prizes.stream()
                .map(p -> p.getFirst().name(Optional.of(p.getSecond())))
                .collect(Collectors.toList());
        }
    }

    /**
     * Returns the icon of this reward, defaulting to either the icon of the
     * first prize (if only one prize exists) or a book (if multiple prizes
     * exist). If the icon does not have a defined display name or lore, it is
     * set to this reward's name/lore.
     */
    @Override
    public ItemStack icon(Optional<BigDecimal> weight) {
        ItemStack base = icon.map(ItemStackSnapshot::createStack).orElseGet(() -> {
            if (prizes.size() == 1) {
                return prizes.get(0).getFirst().icon(Optional.of(prizes.get(0).getSecond()));
            } else {
                return ItemStack.of(ItemTypes.BOOK, 1);
            }
        });
        if (!base.get(Keys.DISPLAY_NAME).isPresent()) {
            base.offer(Keys.DISPLAY_NAME, name(weight));
        }
        if (!base.get(Keys.ITEM_LORE).isPresent()) {
            base.offer(Keys.ITEM_LORE, lore(weight));
        }
        return base;
    }

    public ImmutableList<Tuple<? extends Prize, ?>> prizes() {
        return prizes;
    }

    public boolean give(User user) {
        //TODO: Error handling
        return prizes.stream().allMatch(p -> p.getFirst().give(user, p.getSecond()));
    }

    public static final class RewardType extends Type<Reward, BigDecimal> {

        public RewardType() {
            super("Reward", CrateCrate.get().getContainer());
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
         *     icon: Optional<ItemStack>
         *     prizes: List<PrizeReference>
         * }</pre>
         */
        @Override
        public Reward deserializeComponent(ConfigurationNode node) throws SerializationException {
            Optional<String> name = Optional.ofNullable(node.getNode("name").getString());
            Optional<ImmutableList<String>> lore = node.getNode("lore").isList()
                ? Optional.of(node.getChildrenList().stream()
                    .map(s -> s.getString(""))
                    .collect(ImmutableList.toImmutableList())
                )
                : Optional.empty();
            Optional<ItemStackSnapshot> icon = !node.getNode("icon").isVirtual()
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.getNode("icon")).createSnapshot())
                : Optional.empty();
            ImmutableList<Tuple<? extends Prize, ?>> prizes = node.getNode("prizes").getChildrenList().stream()
                .map(n -> {
                    ConfigurationNode component = n.isList() ? n.getNode(0) : n;
                    List<? extends ConfigurationNode> values = n.getChildrenList().subList(n.isList() ? 1 : 0, n.getChildrenList().size());
                    return Config.resolvePrizeType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            return new Reward(String.valueOf(node.getKey()), name, lore, icon, ImmutableList.copyOf(prizes));
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, Reward component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a reward reference, defined as:
         *
         * <pre>{@code
         * RewardReference:
         *     node: Reward | String (Reward id) | PrizeReference (map/string)
         *        weight: BigDecimal (required for Reward, required for
         *            PrizeReference when reference value is not defined)
         *     values: [
         *        list... (limit 1 for Reward/String),
         *        Optional<BigDecimal> (required for String, required for
         *            PrizeReference when weight is not defined)
         *     ]
         * }</pre>
         */
        @Override
        public Tuple<Reward, BigDecimal> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            Reward reward;
            if (node.isMap()) {
                if (!node.getNode("prizes").isVirtual()) {
                    reward = deserializeComponent(node);
                    reward = new Reward("Reward@" + Arrays.toString(node.getPath()), reward.name, reward.lore, reward.icon, reward.prizes);
                } else {
                    Tuple<? extends Prize, ?> prize = Config.resolvePrizeType(node).deserializeReference(node, values.subList(0, values.isEmpty() ? 0 : values.size() - 1));
                    reward = new Reward("Reward@" + Arrays.toString(node.getPath()), Optional.empty(), Optional.empty(), Optional.empty(), ImmutableList.of(prize));
                }
                Config.REWARDS.put(reward.id, reward);
            } else {
                String identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.REWARDS.containsKey(identifier)) {
                    reward = Config.REWARDS.get(identifier);
                } else {
                    Tuple<? extends Prize, ?> prize = Config.resolvePrizeType(node).deserializeReference(node, values.subList(0, values.isEmpty() ? 0 : values.size() - 1));
                    reward = new Reward(identifier, Optional.empty(), Optional.empty(), Optional.empty(), ImmutableList.of(prize));
                    Config.REWARDS.put(reward.id, reward);
                }
            }
            //TODO: Validate reference value counts and existence
            BigDecimal value = new BigDecimal((!values.isEmpty() ? values.get(0) : node.getNode("weight")).getString());
            return Tuple.of(reward, value);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<Reward, BigDecimal> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
