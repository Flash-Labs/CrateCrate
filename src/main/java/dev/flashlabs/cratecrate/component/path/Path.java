package dev.flashlabs.cratecrate.component.path;

import com.flowpowered.math.vector.Vector3d;

public abstract class Path {

    private final int interval;
    private final int precision;
    private final int segments;
    private final double shift;
    private final double speed;
    private final Vector3d scale;

    protected Path(
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

}
