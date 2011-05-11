package org.chemodansama.engine.render.imgui;

public final class NpRect extends NpImmutableRect {
    public NpRect() {
        super();
    }
    
    public NpRect(float x, float y, float w, float h) {
        super(x, y, w, h);
    }
    
    public void setX(float x) {
        mX = x;
    }
    
    public void setY(float y) {
        mY = y;
    }
    
    public void setW(float w) {
        mW = w;
    }
    
    public void setH(float h) {
        mH = h;
    }
    
    public void set(float x, float y, float w, float h) {
        mX = x;
        mY = y;
        mW = w;
        mH = h;
    }
}
