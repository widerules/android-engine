package org.chemodansama.engine.render.imgui;

public enum NpWidgetState {
    WS_NORMAL("Normal"), WS_HOVER("Hover"), WS_PUSHED("Pushed"), 
    WS_DISABLED("Disabled");
    
    final private String mSkinString;
    
    private NpWidgetState(String skinString) {
        mSkinString = skinString;
    }
    
    final String getSkinString() {
        return mSkinString;
    }
    
    static final NpWidgetState parseStr(String s) {
        for (NpWidgetState st : NpWidgetState.values()) {
            if (st.getSkinString().equalsIgnoreCase(s)) {
                return st;
            }
        }
        
        return WS_NORMAL;
    }
}