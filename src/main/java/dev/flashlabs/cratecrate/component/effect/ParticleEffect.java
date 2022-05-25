package dev.flashlabs.cratecrate.component.effect;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.component.path.Path;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class ParticleEffect extends Effect.Locatable {

    public static final Type<ParticleEffect, Tuple<Target, Vector3d>> TYPE = new ParticleEffectType();

    private final ParticleType type;
    private final Optional<Color> color;
    private final Path path;

    private ParticleEffect(
        String id,
        ParticleType type,
        Optional<Color> color,
        Path path
    ) {
        super(id);
        this.type = type;
        this.color = color;
        this.path = path;
    }

    @Override
    public Text name(Optional<Tuple<Target, Vector3d>> value) {
        return Text.of(type.getName(), type.equals(ParticleTypes.REDSTONE_DUST)
            ? Text.of(" (", color.map(c -> Integer.toString(c.getRgb(), 16).toUpperCase()).orElse("Random"), ")")
            : Text.EMPTY);
    }

    /**
     * Returns the lore of this effect, which is always empty.
     */
    @Override
    public List<Text> lore(Optional<Tuple<Target, Vector3d>> value) {
        return ImmutableList.of();
    }

    /**
     * Returns the icon of this effect, which is a redstone item with this
     * effect's name/lore.
     */
    @Override
    public ItemStack icon(Optional<Tuple<Target, Vector3d>> value) {
        return ItemStack.builder()
            .itemType(ItemTypes.REDSTONE)
            .add(Keys.DISPLAY_NAME, name(value))
            .add(Keys.ITEM_LORE, lore(value))
            .build();
    }

    @Override
    public boolean give(Location<World> location) {
        Task task = Task.builder()
            .execute(new Runnable() {

                private double radians = path.shift();
                private final double increment = path.speed() * (TrigMath.TWO_PI / path.precision());
                private final org.spongepowered.api.effect.particle.ParticleEffect.Builder builder = org.spongepowered.api.effect.particle.ParticleEffect.builder().type(type);

                @Override
                public void run() {
                    if (type.equals(ParticleTypes.REDSTONE_DUST)) {
                        builder.option(ParticleOptions.COLOR, color.orElseGet(() -> Color.ofRgb(
                            (int) (127.5 + 127.5 * TrigMath.cos(6.0 / 7.0 * radians)),
                            (int) (127.5 + 127.5 * TrigMath.sin(6.0 / 7.0 * radians)),
                            (int) (127.5 - 127.5 * TrigMath.cos(6.0 / 7.0 * radians))
                        )));
                    }
                    org.spongepowered.api.effect.particle.ParticleEffect effect = builder.build();
                    for (Vector3d vector : path.positions(radians)) {
                        location.getExtent().spawnParticles(effect, location.getPosition().add(path.scale().mul(vector)));
                    }
                    radians += increment;
                }

            })
            .interval(path.interval(), TimeUnit.MILLISECONDS)
            .submit(CrateCrate.get().getContainer());
        Task.builder()
            .execute(task::cancel)
            .delay(10000, TimeUnit.MILLISECONDS)
            .submit(CrateCrate.get().getContainer());
        return true;
    }

    private static final class ParticleEffectType extends Type<ParticleEffect, Tuple<Target, Vector3d>> {

        private ParticleEffectType() {
            super("Particle", CrateCrate.get().getContainer());
        }

        /**
         * Matches nodes having a {@code particle} child.
         */
        @Override
        public boolean matches(Node node) {
            return node.get("particle").getType() != Node.Type.UNDEFINED;
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
        public ParticleEffect deserializeComponent(Node node) throws SerializationException {
            ParticleType type = node.get("particle").getType() == Node.Type.STRING
                ? node.get("particle", Serializers.CATALOG_TYPE.of(ParticleType.class))
                : node.get("particle.type", Serializers.CATALOG_TYPE.of(ParticleType.class));
            Optional<Color> color = node.get("particle.color", Storm.INTEGER.optional()).map(Color::ofRgb);
            Path path = Path.deserialize(node.get("path"));
            return new ParticleEffect(String.valueOf(node.getKey()), type, color, path);
        }

        @Override
        public void reserializeComponent(Node node, ParticleEffect component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a particle effect reference, defined as:
         *
         * <pre>{@code
         * SoundEffectReference:
         *     node:
         *        ParticleEffect |
         *        String (ParticleEffect id)
         *     values: Effect.Locatable reference value
         * }</pre>
         */
        @Override
        public Tuple<ParticleEffect, Tuple<Target, Vector3d>> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            ParticleEffect effect;
            if (node.getType() == Node.Type.OBJECT) {
                effect = deserializeComponent(node);
                effect = new ParticleEffect("ParticleEffect@" + node.getPath(), effect.type, effect.color, effect.path);
                Config.EFFECTS.put(effect.id, effect);
            } else {
                String identifier = node.get(Storm.STRING);
                if (Config.EFFECTS.containsKey(identifier)) {
                    effect = (ParticleEffect) Config.EFFECTS.get(identifier);
                } else {
                    throw new AssertionError();
                }
            }
            return Tuple.of(effect, deserializeReferenceValue(node, values));
        }

        @Override
        public void reserializeReference(Node node, Tuple<ParticleEffect, Tuple<Target, Vector3d>> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
