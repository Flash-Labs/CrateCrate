package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
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
            CrateCrate.get().getContainer().getLogger().error("Failed to give item: " + result.getType().name());
            return false;
        }
    }

    private static final class ItemPrizeType extends Type<ItemPrize, Integer> {

        private ItemPrizeType() {
            super("Item", CrateCrate.get().getContainer());
        }

        /**
         * Matches nodes having a {@code item} child or identifying an item
         * type.
         */
        @Override
        public boolean matches(Node node) {
            if (node.get("item").getType() == Node.Type.UNDEFINED) {
                try {
                    node.get(Serializers.ITEM_TYPE);
                } catch (SerializationException ignored) {
                    return false;
                }
            }
            return true;
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
        public ItemPrize deserializeComponent(Node node) throws SerializationException {
            Optional<String> name = node.get("name", Storm.STRING.optional());
            Optional<ImmutableList<String>> lore = node.get("lore", Storm.LIST.of(Storm.STRING).optional()).map(ImmutableList::copyOf);
            Optional<ItemStackSnapshot> icon = node.get("icon", Serializers.ITEM_STACK.optional()).map(ItemStack::createSnapshot);
            ItemStackSnapshot item = node.get("item", Serializers.ITEM_STACK).createSnapshot();
            return new ItemPrize(String.valueOf(node.getKey()), name, lore, icon, item);
        }

        @Override
        public void reserializeComponent(Node node, ItemPrize component) throws SerializationException {
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
        public Tuple<ItemPrize, Integer> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            ItemPrize prize;
            if (node.getType() == Node.Type.OBJECT) {
                prize = deserializeComponent(node);
                prize = new ItemPrize("ItemPrize@" + node.getPath(), prize.name, prize.lore, prize.icon, prize.item);
                Config.PRIZES.put(prize.id, prize);
            } else {
                String identifier = node.get(Storm.STRING);
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (ItemPrize) Config.PRIZES.get(identifier);
                } else {
                    ItemStackSnapshot item = Serializers.ITEM_TYPE.deserialize(node).createSnapshot();
                    prize = new ItemPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), item);
                    Config.PRIZES.put(prize.id, prize);
                }
            }
            if (values.isEmpty() && node.get("quantity").getType() == Node.Type.UNDEFINED) {
                throw new SerializationException(node, "Expected a reference value for the quantity.");
            }
            int quantity = (!values.isEmpty() ? values.get(values.size() - 1) : node.get("quantity"))
                .get(Storm.INTEGER.range(Range.closed(1, prize.item.getType().getMaxStackQuantity())));
            return Tuple.of(prize, quantity);
        }

        @Override
        public void reserializeReference(Node node, Tuple<ItemPrize, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
