package dev.flashlabs.cratecrate.component.key;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.SerializationException;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.flashlabs.cratecrate.internal.Storage;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StandardKey extends Key {

    public static final Type<StandardKey, Integer> TYPE = new StandardKeyType();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;

    private StandardKey(
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
     * Returns the name of this key, defaulting to the id. If a reference value
     * is given, it is appended to the name in the form {@code (x#)}.
     */
    @Override
    public Text name(Optional<Integer> quantity) {
        return Text.of(
            TextSerializers.FORMATTING_CODE.deserialize(name.orElse(id)),
            quantity.map(q -> " (x" + q + ")").orElse("")
        );
    }

    /**
     * Returns the lore of this key, defaulting to an empty list. The reference
     * value is currently unused.
     */
    @Override
    public List<Text> lore(Optional<Integer> unused) {
        return lore.orElse(ImmutableList.of()).stream()
            .map(TextSerializers.FORMATTING_CODE::deserialize)
            .collect(Collectors.toList());
    }

    /**
     * Returns the icon of this key, defaulting to a tripwire hook. If the icon
     * does not have a defined display name or lore, it is set to this key's
     * name/lore with the quantity if it is larger than the max stack size. The
     * quantity of the icon is set to the given quantity if it is no more
     * than the max stack size, else it is {@code 1}.
     */
    @Override
    public ItemStack icon(Optional<Integer> quantity) {
        ItemStack base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.TRIPWIRE_HOOK, 1));
        if (!base.get(Keys.DISPLAY_NAME).isPresent()) {
            base.offer(Keys.DISPLAY_NAME, name(quantity.filter(q -> q > base.getMaxStackQuantity())));
        }
        if (lore.isPresent() && !base.get(Keys.ITEM_LORE).isPresent()) {
            base.offer(Keys.ITEM_LORE, lore(Optional.empty()));
        }
        base.setQuantity(quantity.filter(q -> q <= base.getMaxStackQuantity()).orElse(1));
        return base;
    }

    @Override
    public Optional<Integer> quantity(User user) {
        try {
            return Optional.of(Storage.queryKeyQuantity(user, this));
        } catch (SQLException e) {
            CrateCrate.getContainer().getLogger().error("Error getting key quantity.", e);
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
            CrateCrate.getContainer().getLogger().error("Error getting key quantity.", e);
            return false;
        }
    }

    private static final class StandardKeyType extends Type<StandardKey, Integer> {

        private StandardKeyType() {
            super("Standard", CrateCrate.getContainer());
        }

        @Override
        public boolean matches(ConfigurationNode node) {
            return true;
        }

        /**
         * Deserializes a standard key, defined as:
         *
         * <pre>{@code
         * StandardKey:
         *     name: Optional<String>
         *     lore: Optional<List<String>>
         *     icon: Optional<ItemStack>
         * }</pre>
         */
        @Override
        public StandardKey deserializeComponent(ConfigurationNode node) throws SerializationException {
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
            return new StandardKey(String.valueOf(node.getKey()), name, lore, icon);
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, StandardKey component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a standard key reference, defined as:
         *
         * <pre>{@code
         * StandardKeyReference:
         *     node:
         *        StandardKey |
         *        String (StandardKey id or any string)
         *     values: [
         *        Optional<Integer> (defaults to 1)
         *     ]
         * }</pre>
         */
        @Override
        public Tuple<StandardKey, Integer> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            StandardKey key;
            if (node.isMap()) {
                key = deserializeComponent(node);
                key = new StandardKey("StandardKey@" + Arrays.toString(node.getPath()), key.name, key.lore, key.icon);
                Config.KEYS.put(key.id, key);
            } else {
                String identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.KEYS.containsKey(identifier)) {
                    key = (StandardKey) Config.KEYS.get(identifier);
                } else {
                    key = new StandardKey(identifier, Optional.empty(), Optional.empty(), Optional.empty());
                    Config.KEYS.put(key.id, key);
                }
            }
            int quantity = (!values.isEmpty() ? values.get(0) : node.getNode("quantity")).getInt(1);
            return Tuple.of(key, quantity);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<StandardKey, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
