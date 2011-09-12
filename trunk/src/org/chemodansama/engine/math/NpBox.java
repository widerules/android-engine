package org.chemodansama.engine.math;

public class NpBox {
    public final float[] center = new float[2];
    public final float[] extents = new float[2];
    
    public NpBox() {
        this(0, 0, 0, 0);
    }
    
    public NpBox(float x, float y, float ew, float eh) {
        center[0] = x;
        center[1] = y;
        
        extents[0] = ew;
        extents[1] = eh;
    }
    
    public boolean overlaps(NpBox box) {
        
        if (box == null) {
            return false;
        }
        
        float cx = box.center[0] - center[0];
        float cy = box.center[1] - center[1];
        
        return ((Math.abs(cx) < box.extents[0] + extents[0]) 
                && (Math.abs(cy) < box.extents[1] + extents[1]));
    }
}
