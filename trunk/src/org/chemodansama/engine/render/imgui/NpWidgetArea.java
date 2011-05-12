package org.chemodansama.engine.render.imgui;

class NpWidgetArea {
    private final String mName;

    private final NpWidgetDim mX;
    private final NpWidgetDim mY;
    private final NpWidgetDim mWidth;
    private final NpWidgetDim mHeight;
    
    private final NpWidgetScale mWidthScale;
    private final NpWidgetScale mHeightScale;
    
    NpWidgetArea(String name, NpWidgetDim x, NpWidgetDim y, NpWidgetDim w, 
                 NpWidgetDim h, NpWidgetScale widthScale, 
                 NpWidgetScale heightScale) {
        mName = name;
        
        mX = x;
        mY = y;
        mWidth = w;
        mHeight = h;
        
        mWidthScale = widthScale;
        mHeightScale = heightScale;
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

    public NpWidgetScale getWidthScale() {
        return mWidthScale;
    }

    public NpWidgetScale getHeightScale() {
        return mHeightScale;
    }
}
