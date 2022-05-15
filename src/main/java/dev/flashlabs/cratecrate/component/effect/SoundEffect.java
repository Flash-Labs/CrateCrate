package dev.flashlabs.cratecrate.component.effect;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public final class SoundEffect extends Effect.Locatable {

    public static final Type<SoundEffect, Tuple<Target, Vector3d>> TYPE = new SoundEffectType();

    private final SoundType type;
    private final Optional<Double> volume;
    private final Optional<Double> pitch;

    private SoundEffect(
        String id,
        SoundType type,
        Optional<Double> volume,
        Optional<Double> pitch
    ) {
        super(id);
        this.type = type;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Returns the name of this effect, which is the name of the type.
     */
    @Override
    public Text name(Optional<Tuple<Target, Vector3d>> value) {
        return Text.of(type.getName());
    }

    /**
     * Returns the lore of this effect, which is always empty.
     */
    @Override
    public List<Text> lore(Optional<Tuple<Target, Vector3d>> value) {
        return ImmutableList.of();
    }

    /**
     * Returns the icon of this effect, which is a record_13 item with this
     * effect's name/lore.
     */
    @Override
    public ItemStack icon(Optional<Tuple<Target, Vector3d>> value) {
        return ItemStack.builder()
            .itemType(ItemTypes.RECORD_13)
            .add(Keys.DISPLAY_NAME, name(value))
            .add(Keys.ITEM_LORE, lore(value))
            .build();
    }

    @Override
    public boolean give(Location<World> location) {
        location.getExtent().playSound(
            type,
            location.getPosition(),
            volume.orElse(1.0),
            pitch.orElse(1.0)
        );
        return true;
    }

    private static final class SoundEffectType extends Type<SoundEffect, Tuple<Target, Vector3d>> {

        private SoundEffectType() {
            super("Sound", CrateCrate.get().getContainer());
        }

        /**
         * Matches nodes having a {@code sound} child or identifying a sound
         * type.
         */
        @Override
        public boolean matches(Node node) {
            if (node.get("sound").getType() == Node.Type.UNDEFINED) {
                try {
                    node.get(Serializers.CATALOG_TYPE.of(SoundType.class));
                } catch (SerializationException ignored) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Deserializes a sound effect, defined as:
         *
         * <pre>{@code
         * SoundEffect:
         *     sound: String (SoundType) | Object
         *         type: String (SoundType)
         *         volume: Optional<Double>
         *         pitch: Optional<Double>
         * }</pre>
         */
        @Override
        public SoundEffect deserializeComponent(Node node) throws SerializationException {
            SoundType type = node.get("sound").getType() == Node.Type.STRING
                ? node.get("sound", Serializers.CATALOG_TYPE.of(SoundType.class))
                : node.get("sound.type", Serializers.CATALOG_TYPE.of(SoundType.class));
            Optional<Double> volume = node.get("sound.volume", Storm.DOUBLE.optional());
            Optional<Double> pitch = node.get("sound.pitch", Storm.DOUBLE.optional());
            return new SoundEffect(String.valueOf(node.getKey()), type, volume, pitch);
        }

        @Override
        public void reserializeComponent(Node node, SoundEffect component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a sound effect reference, defined as:
         *
         * <pre>{@code
         * SoundEffectReference:
         *     node:
         *        SoundEffect |
         *        String (SoundEffect id or SoundType)
         *     values: Effect.Locatable reference value
         * }</pre>
         */
        @Override
        public Tuple<SoundEffect, Tuple<Target, Vector3d>> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            SoundEffect effect;
            if (node.getType() == Node.Type.OBJECT) {
                effect = deserializeComponent(node);
                effect = new SoundEffect("SoundEffect@" + node.getPath(), effect.type, effect.volume, effect.pitch);
                Config.EFFECTS.put(effect.id, effect);
            } else {
                String identifier = node.get(Storm.STRING);
                if (Config.EFFECTS.containsKey(identifier)) {
                    effect = (SoundEffect) Config.EFFECTS.get(identifier);
                } else {
                    SoundType type = node.get(Serializers.CATALOG_TYPE.of(SoundType.class));
                    effect = new SoundEffect(identifier, type, Optional.empty(), Optional.empty());
                    Config.EFFECTS.put(effect.id, effect);
                }
            }
            return Tuple.of(effect, deserializeReferenceValue(node, values));
        }

        @Override
        public void reserializeReference(Node node, Tuple<SoundEffect, Tuple<Target, Vector3d>> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
