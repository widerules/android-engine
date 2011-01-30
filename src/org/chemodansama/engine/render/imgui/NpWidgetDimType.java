package org.chemodansama.engine.render.imgui;

enum NpWidgetDimType {
    ABSOLUTE("Absolute"), SCALE("Scale"), IMAGE("Image");
    
    final private String mSkinString;
    
    private NpWidgetDimType(String skinString) {
        mSkinString = skinString;
    }
    
    public String getSkinString() {
        return mSkinString;
    }
}
