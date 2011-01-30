package org.chemodansama.engine.render.imgui;

public class NpRect {
    
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
    
    public float getY() {
        return mY;
    }
    
    public float getW() {
        return mW;
    }
    
    public float getH() {
        return mH;
    }
}
