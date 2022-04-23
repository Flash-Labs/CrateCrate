package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.SerializationException;
import dev.flashlabs.cratecrate.internal.Serializers;
import ninja.leaping.configurate.ConfigurationNode;
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
    private final ImmutableList<Tuple<? extends Reward, BigDecimal>> rewards;

    private Crate(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        ImmutableList<Tuple<? extends Key, Integer>> keys,
        ImmutableList<Tuple<? extends Reward, BigDecimal>> rewards
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

    public ImmutableList<Tuple<? extends Reward, BigDecimal>> rewards() {
        return rewards;
    }

    public boolean open(Player player, Location<World> location) {
        return give(player, location, roll(player));
    }

    public boolean give(Player player, Location<World> location, Tuple<? extends Reward, BigDecimal> reward) {
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
            super("Crate", CrateCrate.getContainer());
        }

        @Override
        public boolean matches(ConfigurationNode node) {
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
        public Crate deserializeComponent(ConfigurationNode node) throws SerializationException {
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
            ImmutableList<Tuple<? extends Key, Integer>> keys = node.getNode("keys").getChildrenList().stream()
                .map(n -> {
                    ConfigurationNode component = n.isList() ? n.getNode(0) : n;
                    List<? extends ConfigurationNode> values = n.getChildrenList().subList(n.isList() ? 1 : 0, n.getChildrenList().size());
                    return Config.resolveKeyType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            ImmutableList<Tuple<? extends Reward, BigDecimal>> rewards = node.getNode("rewards").getChildrenList().stream()
                .map(n -> {
                    ConfigurationNode component = n.isList() ? n.getNode(0) : n;
                    List<? extends ConfigurationNode> values = n.getChildrenList().subList(n.isList() ? 1 : 0, n.getChildrenList().size());
                    return Config.resolveRewardType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            return new Crate(String.valueOf(node.getKey()), name, lore, icon, ImmutableList.copyOf(keys), ImmutableList.copyOf(rewards));
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, Crate component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public Tuple<Crate, Void> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) {
            throw new AssertionError("Crates cannot be referenced.");
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<Crate, Void> reference) {
            throw new AssertionError("Crates cannot be referenced.");
        }

    }

}
