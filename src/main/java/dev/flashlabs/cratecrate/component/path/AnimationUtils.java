package dev.flashlabs.cratecrate.component.path;

public final class AnimationUtils {

    public static double TAU = 2 * Math.PI;

    public static double[] shift(double radians, int segments) {
        double[] shifts = new double[segments];
        for (int i = 0; i < segments; i++) {
            shifts[i] = radians + i * (TAU / segments);
        }
        return shifts;
    }

}
