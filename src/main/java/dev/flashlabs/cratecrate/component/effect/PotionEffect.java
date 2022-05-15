package dev.flashlabs.cratecrate.component.effect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public final class PotionEffect extends Effect<Integer> {

    public static final Type<PotionEffect, Integer> TYPE = new PotionEffectType();

    private final org.spongepowered.api.effect.potion.PotionEffectType type;
    private final int amplifier;
    private final Optional<Boolean> ambient;
    private final Optional<Boolean> particles;

    private PotionEffect(
        String id,
        org.spongepowered.api.effect.potion.PotionEffectType type,
        int amplifier,
        Optional<Boolean> ambient,
        Optional<Boolean> particles
    ) {
        super(id);
        this.type = type;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
    }

    /**
     * Returns the name of this effect, which is the translation of the potion
     * type followed by the strength of the potion if it has an amplifier of at
     * least {@code 1} (using roman numerals up to V).
     */
    @Override
    public Text name(Optional<Integer> value) {
        Text level;
        switch (amplifier) {
            case 0: level = Text.EMPTY; break;
            case 1: level = Text.of(" II"); break;
            case 2: level = Text.of(" III"); break;
            case 3: level = Text.of(" IV"); break;
            case 4: level = Text.of(" V"); break;
            default: level = Text.of(" ", amplifier + 1); break;
        }
        return Text.of(type.getPotionTranslation(), level);
    }

    /**
     * Returns the lore of this effect, which is either an empty list or the
     * HH:mm:ss formatted duration if a reference value is given.
     */
    @Override
    public List<Text> lore(Optional<Integer> value) {
        return value
            .map(d -> ImmutableList.of((Text) Text.of(DurationFormatUtils.formatDurationHMS(d))))
            .orElse(ImmutableList.of());
    }

    /**
     * Returns the icon of this effect, which is a splash potion with this
     * effect's name/lore.
     */
    @Override
    public ItemStack icon(Optional<Integer> value) {
        return ItemStack.builder()
            .itemType(ItemTypes.SPLASH_POTION)
            .add(Keys.DISPLAY_NAME, name(value))
            .add(Keys.ITEM_LORE, lore(value))
            .build();
    }

    @Override
    public boolean give(Player player, Location<World> location, Integer duration) {
        List<org.spongepowered.api.effect.potion.PotionEffect> effects = player.get(Keys.POTION_EFFECTS).orElseGet(Lists::newArrayList);
        effects.add(org.spongepowered.api.effect.potion.PotionEffect.builder()
            .potionType(type)
            .amplifier(amplifier)
            .ambience(ambient.orElse(false))
            .particles(particles.orElse(true))
            .duration(20 * duration)
            .build());
        return player.offer(Keys.POTION_EFFECTS, effects).isSuccessful();
    }

    private static final class PotionEffectType extends Type<PotionEffect, Integer> {

        private PotionEffectType() {
            super("Potion", CrateCrate.get().getContainer());
        }

        /**
         * Matches nodes having a {@code potion} child or identifying a potion
         * type.
         */
        @Override
        public boolean matches(Node node) {
            if (node.get("potion").getType() == Node.Type.UNDEFINED) {
                try {
                    node.get(Serializers.POTION_TYPE);
                } catch (SerializationException ignored) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Deserializes a potion effect, defined as:
         *
         * <pre>{@code
         * FireworkEffect:
         *     potion: String (PotionType) | Object
         *         type: String (PotionType)
         *         ambient: Optional<Boolean>
         *         particles: Optional<Boolean>
         * }</pre>
         */
        @Override
        public PotionEffect deserializeComponent(Node node) throws SerializationException {
            org.spongepowered.api.effect.potion.PotionEffect base = node.get("potion").getType() == Node.Type.STRING
                ? node.get("potion", Serializers.POTION_TYPE)
                : node.get("potion.type", Serializers.POTION_TYPE);
            Optional<Boolean> ambient = node.get("potion.ambient", Storm.BOOLEAN.optional());
            Optional<Boolean> particles = node.get("potion.particles", Storm.BOOLEAN.optional());
            return new PotionEffect(String.valueOf(node.getKey()), base.getType(), base.getAmplifier(), ambient, particles);
        }

        @Override
        public void reserializeComponent(Node node, PotionEffect component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a potion effect reference, defined as:
         *
         * <pre>{@code
         * PotionEffectReference:
         *     node:
         *        PotionEffect |
         *        String (PotionType)
         *     values: [
         *        Integer
         *     ]
         * }</pre>
         */
        @Override
        public Tuple<PotionEffect, Integer> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            PotionEffect effect;
            if (node.getType() == Node.Type.OBJECT) {
                effect = deserializeComponent(node);
                effect = new PotionEffect("PotionEffect@" + node.getPath(), effect.type, effect.amplifier, effect.ambient, effect.particles);
                Config.EFFECTS.put(effect.id, effect);
            } else {
                String identifier = node.get(Storm.STRING);
                if (Config.EFFECTS.containsKey(identifier)) {
                    effect = (PotionEffect) Config.EFFECTS.get(identifier);
                } else {
                    org.spongepowered.api.effect.potion.PotionEffect base = node.get(Serializers.POTION_TYPE);
                    effect = new PotionEffect(identifier, base.getType(), base.getAmplifier(), Optional.empty(), Optional.empty());
                    Config.EFFECTS.put(effect.id, effect);
                }
            }
            if (values.isEmpty() && node.get("duration").getType() == Node.Type.UNDEFINED) {
                throw new SerializationException(node, "Expected a reference value for the duration.");
            }
            int duration = (!values.isEmpty() ? values.get(values.size() - 1) : node.get("duration"))
                .get(Storm.INTEGER.range(Range.atLeast(1)));
            return Tuple.of(effect, duration);
        }

        @Override
        public void reserializeReference(Node node, Tuple<PotionEffect, Integer> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
