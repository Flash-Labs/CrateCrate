package dev.flashlabs.cratecrate.component.path;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector3d;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;

public final class SpiralPath extends Path {

    public SpiralPath(
        int interval,
        int precision,
        int segments,
        double shift,
        double speed,
        Vector3d scale
    ) {
        super(interval, precision, segments, shift, speed, scale);
    }

    @Override
    public Vector3d[] positions(double radians) {
        Vector3d[] vectors = new Vector3d[segments() * precision()];
        double[] shifts = AnimationUtils.shift(-radians, segments());
        double[] lengths = AnimationUtils.shift(0.0, precision());
        for (int i = 0; i < shifts.length; i++) {
            for (int j = 0; j < lengths.length; j++) {
                double angle = shifts[i] + lengths[j];
                double scale = lengths[j] / TrigMath.TWO_PI;
                vectors[precision() * i + j] = Vector3d.from(
                    TrigMath.cos(angle) * scale,
                    0.0,
                    TrigMath.sin(angle) * scale
                );
            }
        }
        return vectors;
    }

    public static SpiralPath deserialize(Node node) throws SerializationException {
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
        return new SpiralPath(interval, precision, segments, shift, speed, scale);
    }

}
