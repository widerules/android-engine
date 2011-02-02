package org.chemodansama.engine.math;

public class NpVec2i {
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
    
    public void setX(final int x) {
        mX = x;
    }
    
    public int getY() {
        return mY;
    }
    
    public void setY(final int y) {
        mY = y;
    }
}
