package org.chemodansama.engine.math;

final public class NpVec2i {
    private int mX;
    private int mY;
    
    public NpVec2i() {
        mX = 0;
        mY = 0;
    }
    
    public NpVec2i(final int x, final int y) {
        mX = x;
        mY = y;
    }
    
    public int getX() {
        return mX;
    }
    
    public int getY() {
        return mY;
    }
    
    public NpVec2 mulF(float sx, float sy) {
        return new NpVec2(mX * sx, mY * sy);
    }
    
    public NpVec2 mulF(float s) {
        return new NpVec2(mX * s, mY * s);
    }
    
    public void setX(final int x) {
        mX = x;
    }
    
    public void setY(final int y) {
        mY = y;
    }
}
