package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.prize.Prize;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Reward extends Component<BigDecimal> {

    public static final RewardType TYPE = new RewardType();
    public static final Map<String, Type<? extends Reward, ?>> TYPES = new HashMap<>();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final Optional<String> message;
    private final Optional<String> broadcast;
    private final ImmutableList<Tuple<? extends Prize, ?>> prizes;

    private Reward(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        Optional<String> message,
        Optional<String> broadcast,
        ImmutableList<Tuple<? extends Prize, ?>> prizes
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.message = message;
        this.broadcast = broadcast;
        this.prizes = prizes;
    }

    /**
     * Returns the name of this reward, defaulting to either the name of the
     * first prize (if only one prize exists) or this reward's capitalized id
     * (if multiple prizes exist). The reference value is currently unused.
     */
    @Override
    public Text name(Optional<BigDecimal> unused) {
        if (name.isPresent()) {
            return TextSerializers.FORMATTING_CODE.deserialize("&f" + name.get());
        } else if (prizes.size() == 1) {
            return prizes.get(0).getFirst().name(Optional.of(prizes.get(0).getSecond()));
        } else {
            return Text.of(TextColors.WHITE, WordUtils.capitalize(id.replace("-", " ")));
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
                    return TextSerializers.FORMATTING_CODE.deserialize("&f" + s);
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

    public Optional<String> message() {
        return message;
    }

    public Optional<String> broadcast() {
        return broadcast;
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
        public boolean matches(Node node) {
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
         *     message: Optional<String>
         *     broadcast: Optional<String>
         *     prizes: List<PrizeReference>
         * }</pre>
         */
        @Override
        public Reward deserializeComponent(Node node) throws SerializationException {
            Optional<String> name = node.get("name", Storm.STRING.optional());
            Optional<ImmutableList<String>> lore = node.get("lore", Storm.LIST.of(Storm.STRING).optional()).map(ImmutableList::copyOf);
            Optional<ItemStackSnapshot> icon = node.get("icon", Serializers.ITEM_STACK.optional()).map(ItemStack::createSnapshot);
            Optional<String> message = node.get("message", Storm.STRING.optional());
            Optional<String> broadcast = node.get("broadcast", Storm.STRING.optional());
            ImmutableList<Tuple<? extends Prize, ?>> prizes = node.get("prizes", Storm.LIST.of(n -> n).optional(ImmutableList.of())).stream()
                .map(n -> {
                    Node component = n.getType() == Node.Type.ARRAY ? n.resolve(0) : n;
                    List<Node> values = n.getType() == Node.Type.ARRAY ? n.getList().subList(1, n.getList().size()) : ImmutableList.of();
                    return Config.resolvePrizeType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            return new Reward(String.valueOf(node.getKey()), name, lore, icon, message, broadcast, ImmutableList.copyOf(prizes));
        }

        @Override
        public void reserializeComponent(Node node, Reward component) throws SerializationException {
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
        public Tuple<Reward, BigDecimal> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            Reward reward;
            if (node.getType() == Node.Type.OBJECT) {
                if (node.get("prizes").getType() != Node.Type.UNDEFINED) {
                    reward = deserializeComponent(node);
                    reward = new Reward("Reward@" + node.getPath(), reward.name, reward.lore, reward.icon, reward.message, reward.broadcast, reward.prizes);
                } else {
                    Tuple<? extends Prize, ?> prize = Config.resolvePrizeType(node).deserializeReference(node, values.subList(0, values.isEmpty() ? 0 : values.size() - 1));
                    reward = new Reward("Reward@" + node.getPath(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), ImmutableList.of(prize));
                }
                Config.REWARDS.put(reward.id, reward);
            } else {
                String identifier = node.get(Storm.STRING);
                if (Config.REWARDS.containsKey(identifier)) {
                    reward = Config.REWARDS.get(identifier);
                } else {
                    Tuple<? extends Prize, ?> prize = Config.resolvePrizeType(node).deserializeReference(node, values.subList(0, values.isEmpty() ? 0 : values.size() - 1));
                    reward = new Reward(identifier, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), ImmutableList.of(prize));
                    Config.REWARDS.put(reward.id, reward);
                }
            }
            if (values.isEmpty() && node.get("weight").getType() == Node.Type.UNDEFINED) {
                throw new SerializationException(node, "Expected a value for the weight.");
            }
            BigDecimal weight = (!values.isEmpty() ? values.get(values.size() - 1) : node.get("weight"))
                .get(Storm.BIG_DECIMAL.range(Range.greaterThan(BigDecimal.ZERO)));
            return Tuple.of(reward, weight);
        }

        @Override
        public void reserializeReference(Node node, Tuple<Reward, BigDecimal> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
