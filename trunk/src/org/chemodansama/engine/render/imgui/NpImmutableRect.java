package org.chemodansama.engine.render.imgui;

public class NpImmutableRect {

    protected float mX;
    protected float mY;
    protected float mW;
    protected float mH;

    public NpImmutableRect() {
        mX = 0;
        mY = 0;
        mW = 0;
        mH = 0;
    }

    public NpImmutableRect(float x, float y, float w, float h) {
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

    public boolean overlapsPoint(float x, float y) {
        return (x > mX) && (x < mX + mW) 
        && (y > mY) && (y < mY + mH);
    }
}
