package org.chemodansama.engine.render;

public class NpSpriteAnimation {
    
    public final int verticalOrigin;
    public final int fps;
    public final int[] sequence;
    public final String name;
    public final String tileset;
    
    public NpSpriteAnimation(String name, String tileset, int fps, 
            int verticalOrigin, int[] sequence) {
        this.name = name;
        this.tileset = tileset;  
        this.fps = fps;
        this.verticalOrigin = verticalOrigin;
        this.sequence = sequence;
    }
}
