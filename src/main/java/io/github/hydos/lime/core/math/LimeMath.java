package io.github.hydos.lime.core.math;

public class LimeMath {
    public static double log2(double n) {
        return java.lang.Math.log(n) / java.lang.Math.log(2);
    }

    public static int clamp(int min, int max, int value) {
        return java.lang.Math.max(min, java.lang.Math.min(max, value));
    }
}
