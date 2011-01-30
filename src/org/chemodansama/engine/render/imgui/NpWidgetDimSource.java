package org.chemodansama.engine.render.imgui;

enum NpWidgetDimSource {
    WIDTH("Width"), HEIGHT("Height");
    
    final private String mSkinString;
    
    private NpWidgetDimSource(String skinString) {
        mSkinString = skinString;
    }
    
    public String getSkinString() {
        return mSkinString;
    }
    
    static final NpWidgetDimSource parseStr(String str) {
        for (NpWidgetDimSource s : NpWidgetDimSource.values()) {
            if (s.getSkinString().equalsIgnoreCase(str)) {
                return s;
            }
        }
        
        return WIDTH;
    }
}