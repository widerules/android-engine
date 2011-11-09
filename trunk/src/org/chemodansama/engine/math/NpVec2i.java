package org.chemodansama.engine.math;

final public class NpVec2i {
    
    public final int[] coords = new int[2];
    
    public NpVec2i() {
        this(0, 0);
    }
    
    public NpVec2i(final int x, final int y) {
        coords[0] = x;
        coords[1] = y;
    }
    
    public double dist(NpVec2i t) {
        if (t == null) {
            throw new IllegalArgumentException("t == null");
        }
        
        double dx = coords[0] - t.coords[0];
        double dy = coords[1] - t.coords[1];
        
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public int getX() {
        return coords[0];
    }
    
    public int getY() {
        return coords[1];
    }
    
    public NpVec2 mulF(float sx, float sy) {
        return new NpVec2(coords[0] * sx, coords[1] * sy);
    }
    
    public NpVec2 mulF(float s) {
        return new NpVec2(coords[0] * s, coords[1] * s);
    }
    
    public void setX(final int x) {
        coords[0] = x;
    }
    
    public void setY(final int y) {
        coords[1] = y;
    }
}
