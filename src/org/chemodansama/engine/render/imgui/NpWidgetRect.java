package org.chemodansama.engine.render.imgui;

public class NpWidgetRect {

    protected final NpWidgetDim mX;
    protected final NpWidgetDim mY;
    protected final NpWidgetDim mWidth;
    protected final NpWidgetDim mHeight;

    public NpWidgetRect(NpWidgetDim x, NpWidgetDim y, NpWidgetDim w, 
            NpWidgetDim h) {
        super();
        
        mX = x;
        mY = y;
        mWidth = w;
        mHeight = h;
    }

    public NpWidgetDim getX() {
        return mX;
    }

    public NpWidgetDim getY() {
        return mY;
    }

    public NpWidgetDim getWidth() {
        return mWidth;
    }

    public NpWidgetDim getHeight() {
        return mHeight;
    }
}