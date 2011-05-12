package org.chemodansama.engine.render.imgui;

public class NpRect {
    
    protected int mX;
    protected int mY;
    protected int mW;
    protected int mH;

    public NpRect() {
        mX = 0;
        mY = 0;
        mW = 0;
        mH = 0;
    }

    public NpRect(int x, int y, int w, int h) {
        mX = x;
        mY = y;
        mW = w;
        mH = h;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public int getW() {
        return mW;
    }

    public int getH() {
        return mH;
    }

    public boolean overlapsPoint(int x, int y) {
        return (x > mX) && (x < mX + mW) 
        && (y > mY) && (y < mY + mH);
    }
    
    public void setX(int x) {
        mX = x;
    }
    
    public void setX(float x) {
        mX = (int) x;
    }

    public void setY(int y) {
        mY = y;
    }

    public void setY(float y) {
        mY = (int) y;
    }
    
    public void setW(int w) {
        mW = w;
    }
    
    public void setW(float w) {
        mW = (int) w;
    }

    public void setH(int h) {
        mH = h;
    }
    
    public void setH(float h) {
        mH = (int) h;
    }

    public void set(int x, int y, int w, int h) {
        mX = x;
        mY = y;
        mW = w;
        mH = h;
    }
    
    public void set(float x, float y, float w, float h) {
        mX = (int) x;
        mY = (int) y;
        mW = (int) w;
        mH = (int) h;
    }
}
