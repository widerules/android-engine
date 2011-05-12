package org.chemodansama.engine.render.imgui;

public enum NpWidgetScale {
    STRETCH("stretch"), REPEAT("repeat");
    
    private static final NpWidgetScale DEFAULT = STRETCH;
    
    private final String mSkinString;
    
    private NpWidgetScale(String skinString) {
        mSkinString = skinString;
    }
    
    public static final NpWidgetScale parseStr(String skinString) {
        
        if (skinString == null) {
            return DEFAULT;
        }
        
        for (NpWidgetScale s : NpWidgetScale.values()) {
            if (s.mSkinString.equals(skinString)) {
                return s;
            }
        }
        
        return DEFAULT;
    }
}
