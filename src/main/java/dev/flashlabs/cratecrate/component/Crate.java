package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.key.Key;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public final class Crate extends Component<Void> {

    public static final CrateType TYPE = new CrateType();
    public static final Map<String, Type<? extends Crate, ?>> TYPES = Maps.newHashMap();

    private static final Random RANDOM = new Random();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final ImmutableList<Tuple<? extends Key, Integer>> keys;
    private final ImmutableList<Tuple<? extends Reward, Integer>> rewards;

    private Crate(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        ImmutableList<Tuple<? extends Key, Integer>> keys,
        ImmutableList<Tuple<? extends Reward, Integer>> rewards
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
    public net.kyori.adventure.text.Component getName(Optional<Void> ignored) {
        return name.map(s -> LegacyComponentSerializer.legacyAmpersand().deserialize(s))
            .orElseGet(() -> net.kyori.adventure.text.Component.text(id));
    }

    /**
     * Returns the lore of this crate, defaulting to an empty list.
     */
    @Override
    public List<net.kyori.adventure.text.Component> getLore(Optional<Void> ignored) {
        return lore
            .map(l -> l.stream()
                .map(s -> LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent())
                .toList())
            .orElseGet(List::of);
    }

    /**
     * Returns the icon of this crate, defaulting to a chest. If the icon does
     * not have a defined display name or lore, it is set to this crate's
     * name/lore.
     */
    @Override
    public ItemStack getIcon(Optional<Void> ignored) {
        var base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.CHEST, 1));
        if (base.get(Keys.CUSTOM_NAME).isEmpty()) {
            base.offer(Keys.CUSTOM_NAME, getName(Optional.empty()));
        }
        if (base.get(Keys.LORE).isEmpty()) {
            base.offer(Keys.LORE, getLore(Optional.empty()));
        }
        return base;
    }

    public ImmutableList<Tuple<? extends Key, Integer>> getKeys() {
        return keys;
    }

    public ImmutableList<Tuple<? extends Reward, Integer>> getRewards() {
        return rewards;
    }

    public void open(ServerPlayer player, Location<?, ?> location) {
        give(player, location, roll(player));
    }

    public void give(ServerPlayer player, Location<?, ?> location, Tuple<? extends Reward, Integer> reward) {
        reward.first().give(player.user());
    }

    /**
     * Returns a random reward rolled from this crate. Currently, rewards are
     * not dependent on the player but this is likely to change in the future.
     */
    public Tuple<? extends Reward, Integer> roll(ServerPlayer player) {
        int sum = rewards.stream().mapToInt(Tuple::second).sum();
        int selection = RANDOM.nextInt(sum);
        for (Tuple<? extends Reward, Integer> reward : rewards) {
            selection -= reward.second();
            if (selection < 0) {
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

        @Override
        public Crate deserializeComponent(ConfigurationNode node) throws SerializationException {
            var name = Optional.ofNullable(node.node("name").get(String.class));
            var lore = node.node("lore").isList()
                ? Optional.ofNullable(node.node("lore").getList(String.class)).map(ImmutableList::copyOf)
                : Optional.<ImmutableList<String>>empty();
            var icon = node.hasChild("icon")
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.node("icon")).createSnapshot())
                : Optional.<ItemStackSnapshot>empty();
            var keys = new ArrayList<Tuple<? extends Key, Integer>>();
            for (ConfigurationNode key : node.node("keys").childrenList()) {
                var component = key.isList() ? key.node(0) : key;
                var values = key.childrenList().subList(key.isList() ? 1 : 0, key.childrenList().size());
                keys.add(Config.resolveKeyType(component).deserializeReference(component, values));
            }
            var rewards = new ArrayList<Tuple<? extends Reward, Integer>>();
            for (ConfigurationNode reward : node.node("rewards").childrenList()) {
                var component = reward.isList() ? reward.node(0) : reward;
                var values = reward.childrenList().subList(reward.isList() ? 1 : 0, reward.childrenList().size());
                rewards.add(Config.resolveRewardType(component).deserializeReference(component, values));
            }
            return new Crate(String.valueOf(node.key()), name, lore, icon, ImmutableList.copyOf(keys), ImmutableList.copyOf(rewards));
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
