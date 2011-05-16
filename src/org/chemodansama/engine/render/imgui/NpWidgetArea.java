package org.chemodansama.engine.render.imgui;

class NpWidgetArea extends NpWidgetRect {
    private final String mName;

    private final NpWidgetScale mWidthScale;
    private final NpWidgetScale mHeightScale;
    
    NpWidgetArea(String name, NpWidgetDim x, NpWidgetDim y, NpWidgetDim w, 
                 NpWidgetDim h, NpWidgetScale widthScale, 
                 NpWidgetScale heightScale) {
        super(x, y, w, h);
        
        mName = name;
        
        mWidthScale = widthScale;
        mHeightScale = heightScale;
    }
    
    public String getName() {
        return mName;
    }
    
    public NpWidgetScale getWidthScale() {
        return mWidthScale;
    }

    public NpWidgetScale getHeightScale() {
        return mHeightScale;
    }
}
