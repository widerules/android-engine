package org.chemodansama.engine.render.imgui;

import org.chemodansama.engine.math.NpVec4;

public class NpFontParams {
    public String name = "";
    public float height = 0;
    public NpVec4 color = new NpVec4(1, 1, 1, 1);
    
    public NpFontParams(String name, float height, NpVec4 color) {
        this.name = name;
        this.height = height;
        this.color = color;
    }
}
