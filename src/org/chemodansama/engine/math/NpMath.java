package org.chemodansama.engine.math;

final public class NpMath {
    public static final float ZERO = 0.000001f;
    
    public static float clampf(float x, float min, float max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }
}
