package org.chemodansama.engine.render.imgui;

public enum NpWidgetDimOpType {
    SUBTRACT("Subtract"), ADD("Add"), MULTIPLY("Multiply"), DIVIDE("Divide");
    
    final private String mSkinString;
    
    private NpWidgetDimOpType(String skinString) {
        mSkinString = skinString;
    }
    
    final public String getSkinString() {
        return mSkinString;
    }
    
    static final public NpWidgetDimOpType parseStr(String str) {
        for (NpWidgetDimOpType t : NpWidgetDimOpType.values()) {
            if (t.getSkinString().equalsIgnoreCase(str)) {
                return t;
            }
        }
        
        // use ADD as default 
        return ADD;
    }
}
