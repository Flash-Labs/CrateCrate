package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public final class Crate extends Component<Void> {

    public static final CrateType TYPE = new CrateType();
    public static final Map<String, Type<? extends Crate, ?>> TYPES = Maps.newHashMap();

    private static final Random RANDOM = new Random();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final ImmutableList<Tuple<? extends Key, Integer>> keys;
    private final ImmutableList<Tuple<Reward, BigDecimal>> rewards;

    private Crate(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        ImmutableList<Tuple<? extends Key, Integer>> keys,
        ImmutableList<Tuple<Reward, BigDecimal>> rewards
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.keys = keys;
        this.rewards = rewards;
    }

    /**
     * Returns the name of this crate, defaulting to the id.
     */
    @Override
    public Text name(Optional<Void> ignored) {
        return name
            .map(TextSerializers.FORMATTING_CODE::deserialize)
            .orElseGet(() -> Text.of(id));
    }

    /**
     * Returns the lore of this crate, defaulting to an empty list.
     */
    @Override
    public List<Text> lore(Optional<Void> ignored) {
        return lore.orElse(ImmutableList.of()).stream()
            .map(TextSerializers.FORMATTING_CODE::deserialize)
            .collect(Collectors.toList());
    }

    /**
     * Returns the icon of this crate, defaulting to a chest. If the icon does
     * not have a defined display name or lore, it is set to this crate's
     * name/lore.
     */
    @Override
    public ItemStack icon(Optional<Void> ignored) {
        ItemStack base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.CHEST, 1));
        if (!base.get(Keys.DISPLAY_NAME).isPresent()) {
            base.offer(Keys.DISPLAY_NAME, name(ignored));
        }
        if (lore.isPresent() && !base.get(Keys.ITEM_LORE).isPresent()) {
            base.offer(Keys.ITEM_LORE, lore(ignored));
        }
        return base;
    }

    public ImmutableList<Tuple<? extends Key, Integer>> keys() {
        return keys;
    }

    public ImmutableList<Tuple<Reward, BigDecimal>> rewards() {
        return rewards;
    }

    public boolean open(Player player, Location<World> location) {
        return give(player, roll(player), location);
    }

    public boolean give(Player player, Tuple<? extends Reward, BigDecimal> reward, Location<World> location) {
        return reward.getFirst().give(player);
    }

    /**
     * Returns a random reward rolled from this crate. Currently, rewards are
     * not dependent on the player but this is likely to change in the future.
     */
    public Tuple<? extends Reward, BigDecimal> roll(Player player) {
        BigDecimal sum = rewards.stream().map(Tuple::getSecond).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal selection = BigDecimal.valueOf(RANDOM.nextDouble()).multiply(sum);
        for (Tuple<? extends Reward, BigDecimal> reward : rewards) {
            selection = selection.subtract(reward.getSecond());
            if (selection.compareTo(BigDecimal.ZERO) <= 0) {
                return reward;
            }
        }
        //TODO: Handle properly for player dependent rewards
        throw new AssertionError("No available rewards.");
    }

    public static final class CrateType extends Type<Crate, Void> {

        private CrateType() {
            super("Crate", CrateCrate.get().getContainer());
        }

        @Override
        public boolean matches(Node node) {
            return true;
        }

        /**
         * Deserializes a crate, defined as:
         *
         * <pre>{@code
         * Reward:
         *     name: Optional<String>
         *     lore: Optional<List<String>>
         *     icon: Optional<ItemStack>
         *     keys: List<KeyReference>
         *     rewards: List<RewardReference>
         * }</pre>
         */
        @Override
        public Crate deserializeComponent(Node node) throws SerializationException {
            Optional<String> name = node.get("name", Storm.STRING.optional());
            Optional<ImmutableList<String>> lore = node.get("lore", Storm.LIST.of(Storm.STRING).optional()).map(ImmutableList::copyOf);
            Optional<ItemStackSnapshot> icon = node.get("icon", Serializers.ITEM_STACK.optional()).map(ItemStack::createSnapshot);
            ImmutableList<Tuple<? extends Key, Integer>> keys = node.get("keys", Storm.LIST.of(n -> n).optional(ImmutableList.of())).stream()
                .map(n -> {
                    Node component = n.getType() == Node.Type.ARRAY ? n.resolve(0) : n;
                    List<Node> values = n.getType() == Node.Type.ARRAY ? n.getList().subList(1, n.getList().size()) : ImmutableList.of();
                    return Config.resolveKeyType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            ImmutableList<Tuple<Reward, BigDecimal>> rewards = node.get("rewards", Storm.LIST.of(n -> n).optional(ImmutableList.of())).stream()
                .map(n -> {
                    Node component = n.getType() == Node.Type.ARRAY ? n.resolve(0) : n;
                    List<Node> values = n.getType() == Node.Type.ARRAY ? n.getList().subList(1, n.getList().size()) : ImmutableList.of();
                    return Config.resolveRewardType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            return new Crate(String.valueOf(node.getKey()), name, lore, icon, ImmutableList.copyOf(keys), ImmutableList.copyOf(rewards));
        }

        @Override
        public void reserializeComponent(Node node, Crate component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public Tuple<Crate, Void> deserializeReference(Node node, List<? extends Node> values) {
            throw new AssertionError("Crates cannot be referenced.");
        }

        @Override
        public void reserializeReference(Node node, Tuple<Crate, Void> reference) {
            throw new AssertionError("Crates cannot be referenced.");
        }

    }

}
