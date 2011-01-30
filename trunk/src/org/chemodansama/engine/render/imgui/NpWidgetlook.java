package org.chemodansama.engine.render.imgui;

import java.util.EnumMap;

final public class NpWidgetlook {
    private String mName = "";

    private EnumMap<NpWidgetState, NpWidgetStatelook> mStateLook = 
        new EnumMap<NpWidgetState, NpWidgetStatelook>(NpWidgetState.class);
    
    NpWidgetlook(String name, 
                 EnumMap<NpWidgetState, NpWidgetStatelook> stateLook) {
        mName = name;
        mStateLook.putAll(stateLook);
    }
    
    public NpWidgetStatelook getStateLook(NpWidgetState state) {
        return mStateLook.get(state);
    }
    
    public String getName() {
        return mName;
    }
}
