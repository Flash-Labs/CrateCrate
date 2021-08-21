package dev.flashlabs.cratecrate.component;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.prize.Prize;
import org.spongepowered.api.entity.living.player.User;
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

    @Override
    public net.kyori.adventure.text.Component getName(Optional<Integer> value) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public List<net.kyori.adventure.text.Component> getLore(Optional<Integer> value) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public ItemStack getIcon(Optional<Integer> value) {
        throw new UnsupportedOperationException(); //TODO
    }

    public ImmutableList<Tuple<? extends Prize, ?>> getPrizes() {
        return prizes;
    }

    public void give(User user) {
        throw new UnsupportedOperationException(); //TODO
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
