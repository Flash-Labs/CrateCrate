package dev.flashlabs.cratecrate.internal;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.stream.Collectors;

public final class Serializers {

    public interface Serializer<T> {

        T deserialize(ConfigurationNode node) throws SerializationException;

        void reserialize(ConfigurationNode node, T value) throws SerializationException;

    }

    public static final Serializer<Currency> CURRENCY = new Serializer<Currency>() {

        @Override
        public Currency deserialize(ConfigurationNode node) throws SerializationException {
            String id = node.getString("");
            //TODO: Alternative to iterating over currencies?
            return Sponge.getServiceManager().provideUnchecked(EconomyService.class).getCurrencies().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new SerializationException(node, "Unknown currency."));
        }

        @Override
        public void reserialize(ConfigurationNode node, Currency value) throws SerializationException {
            node.setValue(value.getId());
        }

    };

    public static final Serializer<ItemStack> ITEM_TYPE = new Serializer<ItemStack>() {

        public ItemStack deserialize(ConfigurationNode node) throws SerializationException {
            String[] split = node.getString("").split("/");
            ItemStack item = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, split[0].contains(":") ? split[0] : "minecraft:" + split[0])
                .orElseThrow(() -> new SerializationException(node, "Unknown item type.")));
            if (split.length == 2) {
                try {
                    item = ItemStack.builder().fromContainer(item.toContainer().set(DataQuery.of("UnsafeDamage"), Integer.parseInt(split[1]))).build();
                } catch (ArithmeticException e) {
                    throw new SerializationException(node, "Invalid item data.");
                }
            }
            return item;
        }

        public void reserialize(ConfigurationNode node, ItemStack value) throws SerializationException {
            String id = value.getType().getId().replace("minecraft:", "");
            Object data = value.toContainer().get(DataQuery.of("UnsafeDamage")).orElse(null);
            node.setValue(data != null ? id + ":" + data : id);
        }

    };

    public static final Serializer<ItemStack> ITEM_STACK = new Serializer<ItemStack>() {

        @Override
        public ItemStack deserialize(ConfigurationNode node) throws SerializationException {
            ItemStack.Builder builder = ItemStack.builder()
                .from(ITEM_TYPE.deserialize(node.isMap() ? node.getNode("type") : node));
            if (!node.getNode("name").isVirtual()) {
                builder.add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(node.getNode("name").getString("")));
            }
            if (!node.getNode("lore").isVirtual()) {
                builder.add(Keys.ITEM_LORE, node.getNode("lore").getChildrenList().stream()
                    .map(n -> TextSerializers.FORMATTING_CODE.deserialize(n.getString("")))
                    .collect(Collectors.toList()));
            }
            if (!node.getNode("enchantments").isVirtual()) {
                builder.add(Keys.ITEM_ENCHANTMENTS, node.getNode("enchantments").getChildrenList().stream()
                    .map(n -> {
                        EnchantmentType type = Sponge.getRegistry().getType(EnchantmentType.class, n.getNode(0).getString(""))
                            .orElseThrow(() -> new SerializationException(n.getNode(0), "Unknown enchantment type."));
                        int level = n.getNode(1).getInt(1);
                        return Enchantment.of(type, level);
                    })
                    .collect(Collectors.toList()));
            }
            //TODO: keys (missing RegistryTypes.KEYS)
            if (!node.getNode("nbt").isVirtual()) {
                builder.fromContainer(builder.build().toContainer().set(DataQuery.of("UnsafeData"), node.getNode("nbt").getValue()));
            }
            if (!node.getNode("quantity").isVirtual()) {
                builder.quantity(node.getNode("quantity").getInt(1));
            }
            return builder.build();
        }

        @Override
        public void reserialize(ConfigurationNode node, ItemStack value) throws SerializationException {
            ITEM_TYPE.reserialize(value.getKeys().isEmpty() && value.getQuantity() == 1 ? node : node.getNode("type"), value);
            value.get(Keys.DISPLAY_NAME).ifPresent(t -> {
                node.getNode("name").setValue(TextSerializers.FORMATTING_CODE.serialize(t));
            });
            value.get(Keys.ITEM_LORE).ifPresent(l -> {
                node.getNode("lore").setValue(l.stream()
                    .map(t -> TextSerializers.FORMATTING_CODE.serialize(t))
                    .collect(Collectors.toList()));
            });
            value.get(Keys.ITEM_ENCHANTMENTS).ifPresent(l -> {
                node.getNode("enchantments").setValue(l.stream()
                    .map(e -> Lists.newArrayList(e.getType().getId().replace("minecraft:", ""), e.getLevel()))
                    .collect(Collectors.toList()));
            });
            value.toContainer().get(DataQuery.of("UnsafeData")).ifPresent(n -> {
                //TODO: Prune duplicate NBT keys
                node.getNode("nbt").setValue(n);
            });
            if (value.getQuantity() != 1) {
                node.getNode("quantity").setValue(value.getQuantity());
            }
        }

    };

}
