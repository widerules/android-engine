package org.chemodansama.engine.math;

public class NpVec2 {
    private float[] mValues = new float[2];
    
    public NpVec2() {
        mValues[0] = 0;
        mValues[1] = 0;
    }
    
    public NpVec2(float x, float y) {
        mValues[0] = x;
        mValues[1] = y;
    }
    
    public float getX() {
        return mValues[0];
    }
    
    public float getY() {
        return mValues[1];
    }
    
    public void setX(float x) {
        mValues[0] = x;
    }
    
    public void setY(float y) {
        mValues[1] = y;
    }
    
    public void setValues(float x, float y) {
        mValues[0] = x;
        mValues[1] = y;
    }
}
