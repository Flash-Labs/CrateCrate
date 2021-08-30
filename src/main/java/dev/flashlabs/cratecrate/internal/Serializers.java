package dev.flashlabs.cratecrate.internal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Serializers {

    public interface Serializer<T> {

        T deserialize(ConfigurationNode node) throws SerializationException;

        void reserialize(ConfigurationNode node, T value) throws SerializationException;

    }

    public static final Serializer<ItemType> ITEM_TYPE = new Serializer<>() {

        public ItemType deserialize(ConfigurationNode node) throws SerializationException {
            var id = Optional.ofNullable(node.getString()).orElse("");
            return RegistryTypes.ITEM_TYPE.get().findValue(ResourceKey.resolve(id))
                .orElseThrow(() -> new SerializationException(node, ItemType.class, "Unknown item type."));
        }

        public void reserialize(ConfigurationNode node, ItemType value) throws SerializationException {
            node.set(String.class, value.key(RegistryTypes.ITEM_TYPE).formatted());
        }

    };

    public static final Serializer<ItemStack> ITEM_STACK = new Serializer<>() {

        @Override
        public ItemStack deserialize(ConfigurationNode node) throws SerializationException {
            var builder = ItemStack.builder()
                .itemType(ITEM_TYPE.deserialize(node.isMap() ? node.node("type") : node));
            if (node.hasChild("name")) {
                builder.add(Keys.CUSTOM_NAME, LegacyComponentSerializer.legacyAmpersand().deserialize(node.node("name").getString("")));
            }
            if (node.hasChild("lore")) {
                builder.add(Keys.LORE, node.node("lore").childrenList().stream()
                    .map(n -> LegacyComponentSerializer.legacyAmpersand().deserialize(n.getString("")).asComponent())
                    .toList());
            }
            if (node.hasChild("enchantments")) {
                var enchantments = new ArrayList<Enchantment>();
                for (ConfigurationNode n : node.node("enchantments").childrenList()) {
                    var type = RegistryTypes.ENCHANTMENT_TYPE.get().findValue(ResourceKey.resolve(n.node(0).getString("")))
                        .orElseThrow(() -> new SerializationException(n.node(0), EnchantmentType.class, "Unknown echantment type."));
                    var level = n.node(1).getInt(1);
                    enchantments.add(Enchantment.of(type, level));
                }
                builder.add(Keys.APPLIED_ENCHANTMENTS, enchantments);
            }
            //TODO: keys (missing RegistryTypes.KEYS)
            if (node.hasChild("nbt")) {
                builder.fromContainer(builder.build().toContainer().set(DataQuery.of("UnsafeData"), node.node("nbt").raw()));
            }
            if (node.hasChild("quantity")) {
                builder.quantity(node.node("quantity").getInt(1));
            }
            return builder.build();
        }

        @Override
        public void reserialize(ConfigurationNode node, ItemStack value) throws SerializationException {
            ITEM_TYPE.reserialize(value.getKeys().isEmpty() && value.quantity() == 1 ? node : node.node("type"), value.type());
            if (value.get(Keys.CUSTOM_NAME).isPresent()) {
                node.node("name").set(LegacyComponentSerializer.legacyAmpersand().serialize(value.get(Keys.CUSTOM_NAME).get()));
            }
            //TODO: Replace with base.get(Keys.LORE).isPresent(); see SpongePowered/Sponge#3512
            var lore = value.toContainer().get(DataQuery.of("UnsafeData", "display", "Lore"));
            if (lore.isPresent()) {
                node.node("lore").set(((List<Component>) lore.get()).stream()
                    .map(t -> LegacyComponentSerializer.legacyAmpersand().serialize(t))
                    .toList());
            }
            if (value.get(Keys.APPLIED_ENCHANTMENTS).isPresent()) {
                node.node("enchantments").set(value.get(Keys.APPLIED_ENCHANTMENTS).get().stream()
                    .map(e -> List.of(e.type().key(RegistryTypes.ENCHANTMENT_TYPE), e.level()))
                    .toList());
            }
            //TODO: keys (missing RegistryTypes.KEYS)
            var nbt = value.toContainer().get(DataQuery.of("UnsafeData"));
            if (nbt.isPresent()) {
                node.node("nbt").set(nbt.get());
            }
            if (value.quantity() != 1) {
                node.node("quantity").set(value.quantity());
            }
        }

    };

}
