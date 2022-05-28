package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.effect.Effect;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.component.opener.Opener;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.flashlabs.flashlibs.message.MessageTemplate;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public final class Crate extends Component<Void> {

    public static final CrateType TYPE = new CrateType();
    public static final Map<String, Type<? extends Crate, ?>> TYPES = Maps.newHashMap();

    private static final Random RANDOM = new Random();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final Optional<String> message;
    private final Optional<String> broadcast;
    private final Optional<Opener> opener;
    private final ImmutableList<Tuple<? extends Key, Integer>> keys;
    private final ImmutableMap<Effect.Action, ImmutableList<Tuple<? extends Effect, ?>>> effects;
    private final ImmutableList<Tuple<Reward, BigDecimal>> rewards;

    private Crate(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        Optional<String> message,
        Optional<String> broadcast,
        Optional<Opener> opener,
        ImmutableList<Tuple<? extends Key, Integer>> keys,
        ImmutableMap<Effect.Action, ImmutableList<Tuple<? extends Effect, ?>>> effects,
        ImmutableList<Tuple<Reward, BigDecimal>> rewards
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.message = message;
        this.broadcast = broadcast;
        this.opener = opener;
        this.keys = keys;
        this.effects = effects;
        this.rewards = rewards;
    }

    /**
     * Returns the name of this crate, defaulting to the capitalized id.
     */
    @Override
    public Text name(Optional<Void> ignored) {
        return name
            .map(n -> TextSerializers.FORMATTING_CODE.deserialize("&f" + n))
            .orElseGet(() -> Text.of(TextColors.WHITE, WordUtils.capitalize(id.replace("-", " "))));
    }

    /**
     * Returns the lore of this crate, defaulting to an empty list.
     */
    @Override
    public List<Text> lore(Optional<Void> ignored) {
        return lore.orElse(ImmutableList.of()).stream()
            .map(l -> TextSerializers.FORMATTING_CODE.deserialize("&f" + l))
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
        if (!base.get(Keys.ITEM_LORE).isPresent()) {
            base.offer(Keys.ITEM_LORE, lore(ignored));
        }
        return base;
    }

    public Optional<String> message() {
        return message;
    }

    public Optional<String> broadcast() {
        return broadcast;
    }

    public ImmutableList<Tuple<? extends Key, Integer>> keys() {
        return keys;
    }

    public ImmutableMap<Effect.Action, ImmutableList<Tuple<? extends Effect, ?>>> effects() {
        return effects;
    }

    public ImmutableList<Tuple<Reward, BigDecimal>> rewards() {
        return rewards;
    }

    public boolean open(Player player, Location<World> location) {
        effects.get(Effect.Action.OPEN).forEach(e -> e.getFirst().give(player, location, e.getSecond()));
        return opener
            .map(o -> o.open(player, this, location))
            .orElseGet(() -> give(player, roll(player), location));
    }

    /**
     * Returns a random reward rolled from this crate. Currently, rewards are
     * not dependent on the player but this is likely to change in the future.
     */
    public Tuple<Reward, BigDecimal> roll(Player player) {
        BigDecimal sum = rewards.stream().map(Tuple::getSecond).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal selection = BigDecimal.valueOf(RANDOM.nextDouble()).multiply(sum);
        for (Tuple<Reward, BigDecimal> reward : rewards) {
            selection = selection.subtract(reward.getSecond());
            if (selection.compareTo(BigDecimal.ZERO) <= 0) {
                return reward;
            }
        }
        //TODO: Handle properly for player dependent rewards
        throw new AssertionError("No available rewards.");
    }

    public boolean give(Player player, Tuple<Reward, BigDecimal> reward, Location<World> location) {
        Optional.ofNullable(reward.getFirst().message().orElse(message.orElse(null)))
            .filter(m -> !m.isEmpty() && !message.orElse("x").isEmpty())
            .ifPresent(m -> player.sendMessage(MessageTemplate.of(m).get(
                "player", player.getName(),
                "crate", name(Optional.empty()),
                "reward", reward.getFirst().name(Optional.of(reward.getSecond()))
            )));
        Optional.ofNullable(reward.getFirst().broadcast().orElse(broadcast.orElse(null)))
            .filter(m -> !m.isEmpty() && !broadcast.orElse("x").isEmpty())
            .ifPresent(m -> Sponge.getServer().getBroadcastChannel().send(MessageTemplate.of(m).get(
                "player", player.getName(),
                "crate", name(Optional.empty()),
                "reward", reward.getFirst().name(Optional.of(reward.getSecond()))
            )));
        effects.get(Effect.Action.GIVE).forEach(e -> e.getFirst().give(player, location, e.getSecond()));
        return reward.getFirst().give(player);
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
         *     message: Optional<String>
         *     broadcast: Optional<String>
         *     keys: List<KeyReference>
         *     rewards: List<RewardReference>
         * }</pre>
         */
        @Override
        public Crate deserializeComponent(Node node) throws SerializationException {
            Optional<String> name = node.get("name", Storm.STRING.optional());
            Optional<ImmutableList<String>> lore = node.get("lore", Storm.LIST.of(Storm.STRING).optional()).map(ImmutableList::copyOf);
            Optional<ItemStackSnapshot> icon = node.get("icon", Serializers.ITEM_STACK.optional()).map(ItemStack::createSnapshot);
            Optional<String> message = node.get("message", Storm.STRING.optional());
            Optional<String> broadcast = node.get("broadcast", Storm.STRING.optional());
            Optional<Opener> opener = node.get("opener").getType() != Node.Type.UNDEFINED
                ? Optional.of(Opener.deserialize(node.get("opener")))
                : Optional.empty();
            ImmutableList<Tuple<? extends Key, Integer>> keys = node.get("keys", Storm.LIST.of(n -> n).optional(ImmutableList.of())).stream()
                .map(n -> {
                    Node component = n.getType() == Node.Type.ARRAY ? n.resolve(0) : n;
                    List<Node> values = n.getType() == Node.Type.ARRAY ? n.getList().subList(1, n.getList().size()) : ImmutableList.of();
                    return Config.resolveKeyType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            ImmutableMap<Effect.Action, ImmutableList<Tuple<? extends Effect, ?>>> effects = Arrays.stream(Effect.Action.values())
                .collect(ImmutableMap.toImmutableMap(a -> a, a -> node.resolve("effects", a.name().toLowerCase())
                    .get(Storm.LIST.of(n -> n).optional(ImmutableList.of())).stream()
                    .map(n -> {
                        Node component = n.getType() == Node.Type.ARRAY ? n.resolve(0) : n;
                        List<Node> values = n.getType() == Node.Type.ARRAY ? n.getList().subList(1, n.getList().size()) : ImmutableList.of();
                        return Config.resolveEffectType(component).deserializeReference(component, values);
                    })
                    .collect(ImmutableList.toImmutableList())));
            ImmutableList<Tuple<Reward, BigDecimal>> rewards = node.get("rewards", Storm.LIST.of(n -> n).optional(ImmutableList.of())).stream()
                .map(n -> {
                    Node component = n.getType() == Node.Type.ARRAY ? n.resolve(0) : n;
                    List<Node> values = n.getType() == Node.Type.ARRAY ? n.getList().subList(1, n.getList().size()) : ImmutableList.of();
                    return Config.resolveRewardType(component).deserializeReference(component, values);
                })
                .collect(ImmutableList.toImmutableList());
            return new Crate(String.valueOf(node.getKey()), name, lore, icon, message, broadcast, opener, keys, effects, rewards);
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
