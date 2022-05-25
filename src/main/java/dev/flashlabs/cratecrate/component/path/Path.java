package dev.flashlabs.cratecrate.component.path;

import com.flowpowered.math.vector.Vector3d;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;

public abstract class Path {

    //TODO: Allow registration of custom path types
    public enum Type {
        CIRCLE,
        HELIX,
        SPIRAL,
        VORTEX,
    }

    private final int interval;
    private final int precision;
    private final int segments;
    private final double shift;
    private final double speed;
    private final Vector3d scale;

    Path(
        int interval,
        int precision,
        int segments,
        double shift,
        double speed,
        Vector3d scale
    ) {
        this.interval = interval;
        this.precision = precision;
        this.segments = segments;
        this.shift = shift;
        this.speed = speed;
        this.scale = scale;
    }

    public final int interval() {
        return interval;
    }

    public final int precision() {
        return precision;
    }

    public final int segments() {
        return segments;
    }

    public final double shift() {
        return shift;
    }

    public final double speed() {
        return speed;
    }

    public final Vector3d scale() {
        return scale;
    }

    public abstract Vector3d[] positions(double radians);

    public static Path deserialize(Node node) {
        Type type = node.getType() == Node.Type.STRING
            ? node.get(Storm.ENUM.of(Type.class))
            : node.get("type", Storm.ENUM.of(Type.class));
        switch (type) {
            case CIRCLE: return CirclePath.deserialize(node);
            case HELIX: return HelixPath.deserialize(node);
            case SPIRAL: return SpiralPath.deserialize(node);
            case VORTEX: return VortexPath.deserialize(node);
            default: throw new AssertionError();
        }
    }

}
