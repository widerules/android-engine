package org.chemodansama.engine.render.imgui;

public final class NpRect {
    
    private float mX;
    private float mY;
    private float mW;
    private float mH;
    
    public NpRect() {
        mX = 0;
        mY = 0;
        mW = 0;
        mH = 0;
    }
    
    public NpRect(float x, float y, float w, float h) {
        mX = x;
        mY = y;
        mW = w;
        mH = h;
    }
    
    public float getX() {
        return mX;
    }
    
    public void setX(float x) {
        mX = x;
    }
    
    public float getY() {
        return mY;
    }
    
    public void setY(float y) {
        mY = y;
    }
    
    public float getW() {
        return mW;
    }
    
    public void setW(float w) {
        mW = w;
    }
    
    public float getH() {
        return mH;
    }
    
    public void setH(float h) {
        mH = h;
    }
    
    public boolean overlapsPoint(float x, float y) {
        return (x > mX) && (x < mX + mW) 
            && (y > mY) && (y < mY + mH);
    }
}
