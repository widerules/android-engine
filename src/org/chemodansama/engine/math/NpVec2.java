package org.chemodansama.engine.math;

final public class NpVec2 {
    public final float[] coords = new float[2];
    
    public NpVec2() {
        coords[0] = 0;
        coords[1] = 0;
    }
    
    public NpVec2(float x, float y) {
        coords[0] = x;
        coords[1] = y;
    }
    
    public float getX() {
        return coords[0];
    }
    
    public float getY() {
        return coords[1];
    }
    
    public void setX(float x) {
        coords[0] = x;
    }
    
    public void setY(float y) {
        coords[1] = y;
    }
    
    public void setValues(float x, float y) {
        coords[0] = x;
        coords[1] = y;
    }
    
    public void setValues(NpVec2 src) {
        if (src == null) {
            return;
        }
        
        coords[0] = src.coords[0];
        coords[1] = src.coords[1];
    }
    
    public static void sub(float[] a, float[] b, float[] out) {
        out[0] = a[0] - b[0];
        out[1] = a[1] - b[1];
    }
}
