package org.chemodansama.engine.math;

public class NpVec2i {
    private int mX;
    private int mY;
    
    public NpVec2i() {
        mX = 0;
        mY = 0;
    }
    
    public NpVec2i(final int X, final int Y) {
        mX = X;
        mY = Y;
    }
    
    public int getX() {
        return mX;
    }
    
    public void setX(final int X) {
        mX = X;
    }
    
    public int getY() {
        return mY;
    }
    
    public void setY(final int Y) {
        mY = Y;
    }
}
