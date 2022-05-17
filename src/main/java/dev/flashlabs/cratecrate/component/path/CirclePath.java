package dev.flashlabs.cratecrate.component.path;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;

public final class CirclePath extends Path {

    private final Vector3d axis;

    public CirclePath(
        int interval,
        int precision,
        int segments,
        double shift,
        double speed,
        Vector3d scale,
        Vector3d axis
    ) {
        super(interval, precision, segments, shift, speed, scale);
        this.axis = axis;
    }

    @Override
    public Vector3d[] positions(double radians) {
        Vector3d[] vectors = new Vector3d[segments()];
        double[] shifts = AnimationUtils.shift(radians, segments());
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = axis.equals(Vector3d.UNIT_Y)
                ? Vector3d.from(TrigMath.cos(shifts[i]), 0.0, TrigMath.sin(shifts[i]))
                : Quaterniond.fromAngleRadAxis(shifts[i], axis).rotate(Vector3d.from(-axis.getZ(), 0.0, axis.getX()).normalize());
        }
        return vectors;
    }

    public static CirclePath deserialize(Node node) throws SerializationException {
        int interval = node.get("interval", Storm.INTEGER.optional(20));
        int precision = node.get("precision", Storm.INTEGER.optional(120));
        int segments = node.get("segments", Storm.INTEGER.optional(1));
        double shift = node.get("shift", Storm.DOUBLE.optional(0.0));
        double speed = node.get("speed", Storm.DOUBLE.optional(1.0));
        Vector3d scale = Vector3d.from(
            node.resolve("scale", 0).get(Storm.DOUBLE.optional(1.0)),
            node.resolve("scale", 1).get(Storm.DOUBLE.optional(1.0)),
            node.resolve("scale", 2).get(Storm.DOUBLE.optional(1.0))
        );
        Vector3d axis = Vector3d.from(
            node.resolve("axis", 0).get(Storm.DOUBLE.optional(1.0)),
            node.resolve("axis", 1).get(Storm.DOUBLE.optional(1.0)),
            node.resolve("axis", 2).get(Storm.DOUBLE.optional(1.0))
        );
        return new CirclePath(interval, precision, segments, shift, speed, scale, axis);
    }

}
