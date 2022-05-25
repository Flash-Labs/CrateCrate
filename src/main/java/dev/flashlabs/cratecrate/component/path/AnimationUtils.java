package dev.flashlabs.cratecrate.component.path;

import com.flowpowered.math.TrigMath;

public final class AnimationUtils {

    public static double[] shift(double radians, int segments) {
        double[] shifts = new double[segments];
        for (int i = 0; i < segments; i++) {
            shifts[i] = radians + i * (TrigMath.TWO_PI / segments);
        }
        return shifts;
    }

}
