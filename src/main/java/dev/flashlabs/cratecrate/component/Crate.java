package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.key.Key;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Crate extends Component<Void> {

    public static final CrateType TYPE = new CrateType();
    public static final Map<String, Type<? extends Crate, ?>> TYPES = Maps.newHashMap();

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

    @Override
    public net.kyori.adventure.text.Component getName(Optional<Void> value) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public List<net.kyori.adventure.text.Component> getLore(Optional<Void> value) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public ItemStack getIcon(Optional<Void> value) {
        throw new UnsupportedOperationException(); //TODO
    }

    public ImmutableList<Tuple<? extends Key, Integer>> getKeys() {
        return keys;
    }

    public ImmutableList<Tuple<? extends Reward, Integer>> getRewards() {
        return rewards;
    }

    public void open(Player player, Location<?, ?> location) {
        throw new UnsupportedOperationException(); //TODO
    }

    public void give(Player player, Location<?, ?> location, Tuple<? extends Reward, Integer> reward) {
        throw new UnsupportedOperationException(); //TODO
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
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, Crate component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public Tuple<Crate, Void> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<Crate, Void> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
