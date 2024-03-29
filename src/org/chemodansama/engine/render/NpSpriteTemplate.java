package org.chemodansama.engine.render;

import org.chemodansama.engine.tmx.TmxTileset;

public class NpSpriteTemplate {
    public final int gid;
    public final String name;
    public final NpTexture texture;
    public final TmxTileset tileset;
    
    public NpSpriteTemplate(String name, TmxTileset tileset, NpTexture texture, 
            int gid) {
        if (tileset == null) {
            throw new IllegalArgumentException("tileset == null");
        }
        
        if (texture == null) {
            throw new IllegalArgumentException("texture == null");
        }
        
        this.name = name;
        this.tileset = tileset;
        this.texture = texture;
        this.gid = gid;
    }
}
