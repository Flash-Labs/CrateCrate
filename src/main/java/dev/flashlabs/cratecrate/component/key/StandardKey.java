package dev.flashlabs.cratecrate.component.key;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.flashlabs.cratecrate.internal.Storage;
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

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StandardKey extends Key {

    public static Type<StandardKey, Integer> TYPE = new StandardKeyType();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;

    public StandardKey(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
    }

    /**
     * Returns the name of this key, defaulting to the id followed by the
     * quantity in parenthesis (if present). If a reference value is given, it
     * replaces {@code ${quantity}}.
     */
    @Override
    public net.kyori.adventure.text.Component name(Optional<Integer> quantity) {
        return name.map(s -> {
            s = s.replaceAll("\\$\\{quantity}", quantity.map(String::valueOf).orElse("${quantity}"));
            return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
        }).orElseGet(() -> net.kyori.adventure.text.Component.text(id + quantity.map(q -> " (" + q + ")").orElse("")));
    }

    /**
     * Returns the lore of this key, defaulting to an empty list.
     */
    @Override
    public List<net.kyori.adventure.text.Component> lore(Optional<Integer> quantity) {
        return lore.map(l -> l.stream().map(s -> {
            s = s.replaceAll("\\$\\{quantity}", quantity.map(String::valueOf).orElse("${quantity}"));
            return LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent();
        }).toList()).orElseGet(List::of);
    }

    /**
     * Returns the icon of this key, defaulting to a tripwire hook. If the icon
     * does not have a defined display name or lore, it is set to this key's
     * name/lore.
     */
    @Override
    public ItemStack icon(Optional<Integer> quantity) {
        var base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.TRIPWIRE_HOOK, 1));
        if (base.get(Keys.CUSTOM_NAME).isEmpty()) {
            base.offer(Keys.CUSTOM_NAME, name(quantity));
        }
        //TODO: Replace with base.get(Keys.LORE).isAbsent(); see SpongePowered/Sponge#3512
        if (lore.isPresent() && !base.toContainer().contains(DataQuery.of("UnsafeData", "display", "Lore"))) {
            base.offer(Keys.LORE, lore(quantity));
        }
        return base;
    }

    @Override
    public Optional<Integer> quantity(User user) {
        try {
            return Optional.of(Storage.queryKeyQuantity(user, this));
        } catch (SQLException e) {
            CrateCrate.container().logger().error("Error getting key quantity.", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean check(User user, Integer value) {
        return quantity(user).map(i -> i >= value).orElse(false);
    }

    @Override
    public boolean give(User user, Integer value) {
        return update(user, value);
    }

    @Override
    public boolean take(User user, Integer value) {
        return update(user, -value);
    }

    private boolean update(User user, int delta) {
        try {
            Storage.updateKeyQuantity(user, this, delta);
            return true;
        } catch (SQLException e) {
            CrateCrate.container().logger().error("Error getting key quantity.", e);
            return false;
        }
    }

    private static final class StandardKeyType extends Type<StandardKey, Integer> {

        private StandardKeyType() {
            super("Standard", CrateCrate.container());
        }

        @Override
        public boolean matches(ConfigurationNode node) {
            return true;
        }

        @Override
        public StandardKey deserializeComponent(ConfigurationNode node) throws SerializationException {
            var name = Optional.ofNullable(node.node("name").get(String.class));
            var lore = node.node("lore").isList()
                ? Optional.ofNullable(node.node("lore").getList(String.class)).map(ImmutableList::copyOf)
                : Optional.<ImmutableList<String>>empty();
            var icon = node.hasChild("icon")
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.node("icon")).createSnapshot())
                : Optional.<ItemStackSnapshot>empty();
            return new StandardKey(String.valueOf(node.key()), name, lore, icon);
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, StandardKey component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        @Override
        public Tuple<StandardKey, Integer> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            StandardKey key;
            if (node.isMap()) {
                key = deserializeComponent(node);
                key = new StandardKey("StandardKey@" + node.path(), key.name, key.lore, key.icon);
                Config.KEYS.put(key.id, key);
            } else {
                var identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.KEYS.containsKey(identifier)) {
                    key = (StandardKey) Config.KEYS.get(identifier);
                } else {
                    key = new StandardKey(identifier, Optional.empty(), Optional.empty(), Optional.empty());
                }
            }
            int quantity = (!values.isEmpty() ? values.get(0) : node.node("quantity")).getInt(1);
            return Tuple.of(key, quantity);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<StandardKey, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
