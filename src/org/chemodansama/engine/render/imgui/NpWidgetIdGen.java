package org.chemodansama.engine.render.imgui;

final public class NpWidgetIdGen {
    
    static int mID = 1;
    
    static public int getNewID() {
        mID++;
        return mID;
    }
    
    static void reset() {
        mID = 1;
    }
}
