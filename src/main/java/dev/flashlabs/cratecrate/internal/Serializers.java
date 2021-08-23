package dev.flashlabs.cratecrate.internal;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Optional;

public class Serializers {

    public interface Serializer<T> {

        T deserialize(ConfigurationNode node) throws SerializationException;

        void serialize(ConfigurationNode node, T value) throws SerializationException;

    }

    public static final Serializer<ItemType> ITEM_TYPE = new Serializer<>() {

        public ItemType deserialize(ConfigurationNode node) throws SerializationException {
            var id = Optional.ofNullable(node.getString()).orElse("");
            return RegistryTypes.ITEM_TYPE.get().findValue(ResourceKey.resolve(id))
                .orElseThrow(() -> new SerializationException(node, ItemType.class, "Unknown item type."));
        }

        public void serialize(ConfigurationNode node, ItemType value) throws SerializationException {
            node.set(String.class, value.key(RegistryTypes.ITEM_TYPE).formatted());
        }

    };

}
