package org.chemodansama.engine.render.imgui;

final public class NpWidgetIdGen {
    
    static int mId = 1;
    
    static public int nextId() {
        return mId++;
    }
    
    static void reset() {
        mId = 1;
    }
}
