package org.chemodansama.engine.render.imgui;

public interface NpGuiReturnConsts {
    static final int GUI_RETURN_FLAG_CLICKED         = 1;
    static final int GUI_RETURN_FLAG_NORMAL          = 1 << 1;
    // mouse is pressed over widget
    static final int GUI_RETURN_FLAG_ACTIVE          = 1 << 2;
    // mouse is over widget
    static final int GUI_RETURN_FLAG_HOT             = 1 << 3; 
    static final int GUI_RETURN_FLAG_MOUSE_MOVED_IN  = 1 << 4;
    static final int GUI_RETURN_FLAG_MOUSE_MOVED_OUT = 1 << 5; 
}
