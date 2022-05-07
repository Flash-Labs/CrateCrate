package dev.flashlabs.cratecrate.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import dev.willbanders.storm.serializer.Serializer;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.stream.Collectors;

public final class Serializers {

    public static final CatalogTypeSerializer<CatalogType> CATALOG_TYPE = new CatalogTypeSerializer<>(null, "minecraft");

    public static final class CatalogTypeSerializer<T extends CatalogType> implements Serializer<T> {

        private final Class<T> clazz;
        private final String namespace;

        private CatalogTypeSerializer(Class<T> clazz, String namespace) {
            this.clazz = clazz;
            this.namespace = namespace;
        }

        @Override
        public T deserialize(Node node) throws SerializationException {
            String id = node.get(Storm.STRING);
            return Sponge.getRegistry().getType(clazz, id.contains(":") ? id : namespace + ":" + id)
                .orElseThrow(() -> new SerializationException(node, "Expected " + clazz.getSimpleName() + " id."));
        }

        @Override
        public void reserialize(Node node, T value) throws SerializationException {
            node.set(value.getId().replace(namespace + ":", ""), Storm.STRING);
        }

        public <T extends CatalogType> CatalogTypeSerializer<T> of(Class<T> clazz) {
            return new CatalogTypeSerializer<>(clazz, namespace);
        }

        public CatalogTypeSerializer<T> namespace(String namespace) {
            return new CatalogTypeSerializer<>(clazz, namespace);
        }

    }

    public static final Serializer<Currency> CURRENCY = new Serializer<Currency>() {

        @Override
        public Currency deserialize(Node node) throws SerializationException {
            String id = node.get(Storm.STRING);
            //TODO: Alternative to iterating over currencies?
            return Sponge.getServiceManager().provideUnchecked(EconomyService.class).getCurrencies().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new SerializationException(node, "Unknown currency."));
        }

        @Override
        public void reserialize(Node node, Currency value) throws SerializationException {
            node.setValue(value.getId());
        }

    };

    public static final Serializer<ItemStack> ITEM_TYPE = new Serializer<ItemStack>() {

        public ItemStack deserialize(Node node) throws SerializationException {
            String[] split = node.get(Storm.STRING).split("/");
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

        public void reserialize(Node node, ItemStack value) throws SerializationException {
            String id = value.getType().getId().replace("minecraft:", "");
            Object data = value.toContainer().get(DataQuery.of("UnsafeDamage")).orElse(null);
            node.setValue(data != null ? id + ":" + data : id);
        }

    };

    public static final Serializer<ItemStack> ITEM_STACK = new Serializer<ItemStack>() {

        @Override
        public ItemStack deserialize(Node node) throws SerializationException {
            ItemStack base = ITEM_TYPE.deserialize(node.getType() == Node.Type.OBJECT ? node.get("type") : node);
            ItemStack.Builder builder = ItemStack.builder().from(base);
            node.get("name", Storm.STRING.optional()).ifPresent(s -> {
                builder.add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(s));
            });
            node.get("lore", Storm.LIST.of(TEXT).optional()).ifPresent(l -> {
                builder.add(Keys.ITEM_LORE, l);
            });
            node.get("enchantments", Storm.LIST.of(n -> n).optional()).ifPresent(l -> {
                builder.add(Keys.ITEM_ENCHANTMENTS, l.stream()
                    .map(n -> {
                        List<Object> tuple = n.get(Storm.TUPLE.of(ImmutableList.of(
                            CATALOG_TYPE.of(EnchantmentType.class).namespace("sponge"),
                            Storm.INTEGER.range(Range.atLeast(1))
                        )));
                        return Enchantment.of((EnchantmentType) tuple.get(0), (Integer) tuple.get(1));
                    })
                    .collect(Collectors.toList()));
            });
            node.get("nbt", Storm.OBJECT.optional()).ifPresent(o -> {
                builder.fromContainer(builder.build().toContainer().set(DataQuery.of("UnsafeData"), o));
            });
            node.get("quantity", Storm.INTEGER.range(Range.closed(1, base.getType().getMaxStackQuantity())).optional()).ifPresent(q -> {
                builder.quantity(q);
            });
            return builder.build();
        }

        @Override
        public void reserialize(Node node, ItemStack value) throws SerializationException {
            ITEM_TYPE.reserialize(value.getKeys().isEmpty() && value.getQuantity() == 1 ? node : node.resolve("type"), value);
            value.get(Keys.DISPLAY_NAME).ifPresent(t -> {
                node.set("name", t, TEXT);
            });
            value.get(Keys.ITEM_LORE).ifPresent(l -> {
                node.set("lore", l, Storm.LIST.of(TEXT));
            });
            value.get(Keys.ITEM_ENCHANTMENTS).ifPresent(l -> {
                node.set("enchantments",
                    l.stream()
                        .map(e -> ImmutableList.of(e.getType(), e.getLevel()))
                        .collect(Collectors.toList()),
                    Storm.TUPLE.of(ImmutableList.of(
                        CATALOG_TYPE.of(EnchantmentType.class).namespace("sponge"),
                        Storm.INTEGER.range(Range.atLeast(1))
                    ))
                );
            });
            value.toContainer().get(DataQuery.of("UnsafeData")).ifPresent(o -> {
                //TODO: Prune duplicate NBT keys
                node.set("nbt", o, Storm.ANY);
            });
            if (value.getQuantity() != 1) {
                node.set("quantity", value.getQuantity(), Storm.INTEGER);
            }
        }

    };

    public static final Serializer<Text> TEXT = new Serializer<Text>() {

        @Override
        public Text deserialize(Node node) throws SerializationException {
            return TextSerializers.FORMATTING_CODE.deserialize(node.get(Storm.STRING));
        }

        @Override
        public void reserialize(Node node, Text value) throws SerializationException {
            node.set(TextSerializers.FORMATTING_CODE.serialize(value), Storm.STRING);
        }

    };

}
