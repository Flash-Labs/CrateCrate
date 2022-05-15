package dev.flashlabs.cratecrate.component.effect;

import com.flowpowered.math.vector.Vector3d;
import dev.flashlabs.cratecrate.component.Component;
import dev.flashlabs.cratecrate.component.Type;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Effect<T> extends Component<T> {

    public static final Map<String, Type<? extends Effect, ?>> TYPES = new HashMap<>();

    public enum Action {
        IDLE,
        OPEN,
        GIVE,
        REJECT,
        PREVIEW,
    }

    protected Effect(String id) {
        super(id);
    }

    public abstract boolean give(Player player, Location<World> location, T value);

    public static abstract class Locatable extends Effect<Tuple<Locatable.Target, Vector3d>> {

        public enum Target {
            PLAYER,
            LOCATION
        }

        protected Locatable(String id) {
            super(id);
        }

        @Override
        public boolean give(Player player, Location<World> location, Tuple<Target, Vector3d> value) {
            return give((value.getFirst() == Target.PLAYER ? player.getLocation() : location).add(value.getSecond()));
        }

        public abstract boolean give(Location<World> location);

        protected static Tuple<Target, Vector3d> deserializeReferenceValue(Node node, List<? extends Node> values) {
            Target target = Target.LOCATION;
            Vector3d offset = Vector3d.ZERO;
            if (node.getType() == Node.Type.OBJECT) {
                target = node.get("target", Storm.ENUM.of(Target.class).optional(Target.LOCATION));
                offset = Vector3d.from(node.resolve("offset", 0).get(Storm.DOUBLE), node.resolve("offset", 1).get(Storm.DOUBLE), node.resolve("offset", 2).get(Storm.DOUBLE));
            } else if (values.size() == 1) {
                target = values.get(0).get(Storm.ENUM.of(Target.class));
            } else if (values.size() == 3) {
                offset = Vector3d.from(values.get(0).get(Storm.DOUBLE), values.get(1).get(Storm.DOUBLE), values.get(2).get(Storm.DOUBLE));
            } else if (values.size() == 4) {
                target = values.get(0).get(Storm.ENUM.of(Target.class));
                offset = Vector3d.from(values.get(1).get(Storm.DOUBLE), values.get(2).get(Storm.DOUBLE), values.get(3).get(Storm.DOUBLE));
            }
            return Tuple.of(target, offset);
        }

    }

}
