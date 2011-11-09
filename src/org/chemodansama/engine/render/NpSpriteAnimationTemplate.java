package org.chemodansama.engine.render;

import org.chemodansama.engine.tmx.TmxTileset;

public class NpSpriteAnimationTemplate {
    public final TmxTileset tileset;
    public final NpTexture texture;
    public final NpSpriteAnimation animation;
    
    public NpSpriteAnimationTemplate(TmxTileset tileset, NpTexture texture, 
            NpSpriteAnimation animation) {
        
        if (tileset == null) {
            throw new IllegalArgumentException("tileset == null");
        }
        
        if (texture == null) {
            throw new IllegalArgumentException("texture == null");
        }
        
        if (animation == null) {
            throw new IllegalArgumentException("animation == null");
        }
        
        this.tileset = tileset;
        this.texture = texture;
        this.animation = animation;
    }
}