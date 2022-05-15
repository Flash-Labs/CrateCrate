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
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public final class FireworkEffect extends Effect.Locatable {

    public static final Type<FireworkEffect, Tuple<Target, Vector3d>> TYPE = new FireworkEffectType();

    private static final Random RANDOM = new Random();

    private final Optional<FireworkShape> shape;
    private final Optional<List<Color>> colors;
    private final Optional<List<Color>> fades;
    private final Optional<Boolean> trail;
    private final Optional<Boolean> flicker;
    private final Optional<Integer> duration;

    private FireworkEffect(
        String id,
        Optional<FireworkShape> shape,
        Optional<List<Color>> colors,
        Optional<List<Color>> fades,
        Optional<Boolean> trail,
        Optional<Boolean> flicker,
        Optional<Integer> duration
    ) {
        super(id);
        this.shape = shape;
        this.colors = colors;
        this.fades = fades;
        this.trail = trail;
        this.flicker = flicker;
        this.duration = duration;
    }

    /**
     * Returns the name of this effect, which is {@code Firework (<shape>)} with
     * the shape being {@code Random} if undefined.
     */
    @Override
    public Text name(Optional<Tuple<Target, Vector3d>> value) {
        return Text.of("Firework (", shape.map(CatalogType::getName).orElse("Random"), ")");
    }

    /**
     * Returns the lore of this effect, which is always empty.
     */
    @Override
    public List<Text> lore(Optional<Tuple<Target, Vector3d>> value) {
        return ImmutableList.of();
    }

    /**
     * Returns the icon of this effect, which is a fireworks item with this
     * effect's name/lore.
     */
    @Override
    public ItemStack icon(Optional<Tuple<Target, Vector3d>> value) {
        return ItemStack.builder()
            .itemType(ItemTypes.FIREWORKS)
            .add(Keys.DISPLAY_NAME, name(value))
            .add(Keys.ITEM_LORE, lore(value))
            .build();
    }

    @Override
    public boolean give(Location<World> location) {
        Entity firework = location.getExtent().createEntity(EntityTypes.FIREWORK, location.getPosition());
        firework.offer(Keys.FIREWORK_EFFECTS, ImmutableList.of(org.spongepowered.api.item.FireworkEffect.builder()
            .shape(shape.orElseGet(() -> {
                Collection<FireworkShape> shapes = Sponge.getRegistry().getAllOf(FireworkShape.class);
                return shapes.stream().skip(RANDOM.nextInt(shapes.size())).findFirst().orElse(FireworkShapes.BALL);
            }))
            .colors(colors.orElseGet(() -> RANDOM.nextBoolean()
                ? ImmutableList.of(Color.ofRgb(RANDOM.nextInt(0xFFFFFF)))
                : ImmutableList.of(Color.ofRgb(RANDOM.nextInt(0xFFFFFF)), Color.ofRgb(RANDOM.nextInt(0xFFFFFF)))
            ))
            .fades(fades.orElseGet(() -> RANDOM.nextBoolean()
                ? ImmutableList.of()
                : ImmutableList.of(Color.ofRgb(RANDOM.nextInt(0xFFFFFF)))
            ))
            .trail(trail.orElseGet(RANDOM::nextBoolean))
            .flicker(flicker.orElseGet(RANDOM::nextBoolean))
            .build()));
        firework.offer(Keys.FIREWORK_FLIGHT_MODIFIER, duration.orElseGet(() -> 1 + RANDOM.nextInt(3)));
        firework.offer(Keys.EXPLOSION_RADIUS, Optional.of(0));
        return location.getExtent().spawnEntity(firework);
    }

    private static final class FireworkEffectType extends Type<FireworkEffect, Tuple<Target, Vector3d>> {

        private FireworkEffectType() {
            super("Firework", CrateCrate.get().getContainer());
        }

        /**
         * Matches nodes having a {@code firework} child or a string value
         * starting with {@code firework}.
         */
        @Override
        public boolean matches(Node node) {
            return node.get("firework").getType() != Node.Type.UNDEFINED
                || node.getType() == Node.Type.STRING && node.get(Storm.STRING).startsWith("firework");
        }

        /**
         * Deserializes a firework effect, defined as:
         *
         * <pre>{@code
         * FireworkEffect:
         *     firework: String (FireworkShape id) | Object
         *         shape: String (FireworkShape id)
         *         colors: Optional<List<Integer>>
         *         fades: Optional<List<Integer>>
         *         trail: Optional<Boolean>
         *         flicker: Optional<Boolean>
         *         duration: Optional<Integer>
         * }</pre>
         */
        @Override
        public FireworkEffect deserializeComponent(Node node) throws SerializationException {
            Optional<FireworkShape> shape = node.get("firework").getType() == Node.Type.STRING
                ? node.get("firework", Serializers.CATALOG_TYPE.of(FireworkShape.class).optional())
                : node.get("firework.shape", Serializers.CATALOG_TYPE.of(FireworkShape.class).optional());
            Optional<List<Color>> colors = node.get("firework.colors", Storm.LIST.of(Storm.INTEGER).optional())
                .map(l -> l.stream().map(Color::ofRgb).collect(Collectors.toList()));
            Optional<List<Color>> fades = node.get("firework.fades", Storm.LIST.of(Storm.INTEGER).optional())
                .map(l -> l.stream().map(Color::ofRgb).collect(Collectors.toList()));
            Optional<Boolean> trail = node.get("firework.trail", Storm.BOOLEAN.optional());
            Optional<Boolean> flicker = node.get("firework.flicker", Storm.BOOLEAN.optional());
            Optional<Integer> duration = node.get("firework.duration", Storm.INTEGER.optional());
            return new FireworkEffect(String.valueOf(node.getKey()), shape, colors, fades, trail, flicker, duration);
        }

        @Override
        public void reserializeComponent(Node node, FireworkEffect component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a firework effect reference, defined as:
         *
         * <pre>{@code
         * FireworkEffectReference:
         *     node:
         *        FireworkEffect |
         *        String (FireworkEffect id or prefixed with "firework")
         *     values: Effect.Locatable reference value
         * }</pre>
         */
        @Override
        public Tuple<FireworkEffect, Tuple<Target, Vector3d>> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            FireworkEffect effect;
            if (node.getType() == Node.Type.OBJECT) {
                effect = deserializeComponent(node);
                effect = new FireworkEffect("FireworkEffect@" + node.getPath(), effect.shape, effect.colors, effect.fades, effect.trail, effect.flicker, effect.duration);
                Config.EFFECTS.put(effect.id, effect);
            } else {
                String identifier = node.get(Storm.STRING);
                if (Config.EFFECTS.containsKey(identifier)) {
                    effect = (FireworkEffect) Config.EFFECTS.get(identifier);
                } else {
                    String[] split = identifier.split("/");
                    Optional<FireworkShape> shape = split.length == 2 ? Sponge.getRegistry().getType(FireworkShape.class, split[1]) : Optional.empty();
                    effect = new FireworkEffect(identifier, shape, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
                    Config.EFFECTS.put(effect.id, effect);
                }
            }
            return Tuple.of(effect, deserializeReferenceValue(node, values));
        }

        @Override
        public void reserializeReference(Node node, Tuple<FireworkEffect, Tuple<Target, Vector3d>> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
