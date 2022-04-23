package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.SerializationException;
import dev.flashlabs.cratecrate.internal.Serializers;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Returns the icon of this prize, defaulting to the item. If the icon
     * does not have a defined display name or lore, it is set to this prize's
     * name/lore with the quantity if it is larger than the max stack size. The
     * quantity of the icon is set to the given quantity if it is no more
     * than the max stack size, else it is {@code 1}.
     */
    @Override
    public ItemStack icon(Optional<Integer> quantity) {
        ItemStack base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(item::createStack);
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
    public boolean give(User user, Integer quantity) {
        InventoryTransactionResult result = user.getInventory().offer(ItemStack.builder()
            .fromSnapshot(item)
            .quantity(quantity)
            .build());
        if (result.getType() == InventoryTransactionResult.Type.SUCCESS) {
            return true;
        } else {
            CrateCrate.getContainer().getLogger().error("Failed to give item: " + result.getType().name());
            return false;
        }
    }

    private static final class ItemPrizeType extends Type<ItemPrize, Integer> {

        private ItemPrizeType() {
            super("Item", CrateCrate.getContainer());
        }

        /**
         * Matches nodes having a {@code item} child or identifying an item
         * type.
         */
        @Override
        public boolean matches(ConfigurationNode node) {
            if (!node.getNode("item").isVirtual()) {
                return true;
            }
            try {
                Serializers.ITEM_TYPE.deserialize(node);
                return true;
            } catch (Exception ignored) {
                return false;
            }
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
            ItemStackSnapshot item = Serializers.ITEM_STACK.deserialize(node.getNode("item")).createSnapshot();
            return new ItemPrize(String.valueOf(node.getKey()), name, lore, icon, item);
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
                prize = new ItemPrize("ItemPrize@" + Arrays.toString(node.getPath()), prize.name, prize.lore, prize.icon, prize.item);
                Config.PRIZES.put(prize.id, prize);
            } else {
                String identifier = node.getString("");
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (ItemPrize) Config.PRIZES.get(identifier);
                } else {
                    ItemStackSnapshot item = Serializers.ITEM_TYPE.deserialize(node).createSnapshot();
                    prize = new ItemPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), item);
                    Config.PRIZES.put(prize.id, prize);
                }
            }
            //TODO: Validate reference value counts
            int quantity = (!values.isEmpty() ? values.get(0) : node.getNode("quantity")).getInt(1);
            return Tuple.of(prize, quantity);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<ItemPrize, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
