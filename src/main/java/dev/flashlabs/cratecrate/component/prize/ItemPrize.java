package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Optional;

public final class ItemPrize extends Prize<Integer> {

    public static final Type<ItemPrize, Integer> TYPE = new ItemPrizeType();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final ItemStackSnapshot item;

    private ItemPrize(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        ItemStackSnapshot item
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.item = item;
    }

    /**
     * Returns the name of this key, defaulting to the id. If a reference value
     * is given, it is appended to the name in the form {@code (x#)}.
     */
    @Override
    public net.kyori.adventure.text.Component name(Optional<Integer> quantity) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(name.orElse(id))
            .append(Component.text(quantity.map(q -> " (x" + q + ")").orElse("")));
    }

    /**
     * Returns the lore of this key, defaulting to an empty list. The reference
     * value is currently unused.
     */
    @Override
    public List<net.kyori.adventure.text.Component> lore(Optional<Integer> unused) {
        return lore.orElseGet(ImmutableList::of).stream()
            .map(s -> LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent())
            .toList();
    }

    /**
     * Returns the icon of this prize, defaulting to the item. If the icon
     * does not have a defined display name or lore, it is set to this prize's
     * name/lore with the quantity if it is larger than the max stack size. The
     * quantity of the icon is set to the given quantity if it is no more
     * than the max stack size, else it is {@code 1}.
     */
    @Override
    public ItemStack icon(Optional<Integer> quantity) {
        var base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(item::createStack);
        if (base.get(Keys.CUSTOM_NAME).isEmpty()) {
            base.offer(Keys.CUSTOM_NAME, name(quantity.filter(q -> q > base.maxStackQuantity())));
        }
        if (lore.isPresent() && base.get(Keys.LORE).isEmpty()) {
            base.offer(Keys.LORE, lore(Optional.empty()));
        }
        base.setQuantity(quantity.filter(q -> q <= base.maxStackQuantity()).orElse(1));
        return base;
    }

    @Override
    public boolean give(User user, Integer quantity) {
        var result = user.inventory().offer(ItemStack.builder()
            .fromSnapshot(item)
            .quantity(quantity)
            .build());
        if (result.type() == InventoryTransactionResult.Type.SUCCESS) {
            return true;
        } else {
            CrateCrate.container().logger().error("Failed to give item: " + result.type().name());
            return false;
        }
    }

    private static final class ItemPrizeType extends Type<ItemPrize, Integer> {

        private ItemPrizeType() {
            super("Item", CrateCrate.container());
        }

        /**
         * Matches nodes having a {@code item} child or identifying an item
         * type.
         */
        @Override
        public boolean matches(ConfigurationNode node) {
            return node.hasChild("item") || Optional.ofNullable(node.getString())
                .map(s -> {
                    //TODO: Consider validating ResourceKey format via regex
                    try {
                        return RegistryTypes.ITEM_TYPE.get().findValue(ResourceKey.resolve(s)).isPresent();
                    } catch (Exception ignored) {
                        return false;
                    }
                })
                .orElse(false);
        }

        /**
         * Deserializes an item prize, defined as:
         *
         * <pre>{@code
         * CommandPrize:
         *     name: Optional<String>
         *     lore: Optional<List<String>>
         *     icon: Optional<ItemStack>
         *     item: ItemStack
         * }</pre>
         */
        @Override
        public ItemPrize deserializeComponent(ConfigurationNode node) throws SerializationException {
            var name = Optional.ofNullable(node.node("name").get(String.class));
            var lore = node.node("lore").isList()
                ? Optional.ofNullable(node.node("lore").getList(String.class)).map(ImmutableList::copyOf)
                : Optional.<ImmutableList<String>>empty();
            var icon = node.hasChild("icon")
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.node("icon")).createSnapshot())
                : Optional.<ItemStackSnapshot>empty();
            var item = Serializers.ITEM_STACK.deserialize(node.node("item")).createSnapshot();
            return new ItemPrize(String.valueOf(node.key()), name, lore, icon, item);
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, ItemPrize component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes an item prize reference, defined as:
         *
         * <pre>{@code
         * ItemPrizeReference:
         *     node:
         *        ItemPrize |
         *        String (ItemPrize id or ItemType)
         *     values: [
         *        Optional<Integer> (defaults to 1)
         *     ]
         * }</pre>
         */
        @Override
        public Tuple<ItemPrize, Integer> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            ItemPrize prize;
            if (node.isMap()) {
                prize = deserializeComponent(node);
                prize = new ItemPrize("ItemPrize@" + node.path(), prize.name, prize.lore, prize.icon, prize.item);
                Config.PRIZES.put(prize.id, prize);
            } else {
                var identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (ItemPrize) Config.PRIZES.get(identifier);
                } else {
                    var item = ItemStack.of(RegistryTypes.ITEM_TYPE.get().findValue(ResourceKey.resolve(identifier))
                        .orElseThrow(AssertionError::new)).createSnapshot();
                    prize = new ItemPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), item);
                    Config.PRIZES.put(prize.id, prize);
                }
            }
            //TODO: Validate reference value counts
            var quantity = (!values.isEmpty() ? values.get(0) : node.node("quantity")).getInt(1);
            return Tuple.of(prize, quantity);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<ItemPrize, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
