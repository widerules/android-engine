package org.chemodansama.engine.render;


public class NpSpriteAnimation {
    
    public final int verticalOrigin;
    public final int fps;
    public final int[] sequence;
    public final String name;
    
    public NpSpriteAnimation(String name, int fps, int verticalOrigin, 
            int[] sequence) {
        this.name = name;
        this.fps = fps;
        this.verticalOrigin = verticalOrigin;
        this.sequence = sequence;
    }
}
