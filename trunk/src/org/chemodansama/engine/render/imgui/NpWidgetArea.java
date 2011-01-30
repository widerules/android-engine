package org.chemodansama.engine.render.imgui;

class NpWidgetArea {
    final private String mName;

    final private NpWidgetDim mX;
    final private NpWidgetDim mY;
    final private NpWidgetDim mWidth;
    final private NpWidgetDim mHeight;
    
    NpWidgetArea(String name, NpWidgetDim x, NpWidgetDim y, NpWidgetDim w, 
                 NpWidgetDim h) {
        mName = name;
        
        mX = x;
        mY = y;
        mWidth = w;
        mHeight = h;
    }
    
    public String getName() {
        return mName;
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
