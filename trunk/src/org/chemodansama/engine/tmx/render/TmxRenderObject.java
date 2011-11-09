package org.chemodansama.engine.tmx.render;

import org.chemodansama.engine.render.NpRenderObject;

public abstract class TmxRenderObject implements NpRenderObject {
    
    protected final TmxRenderQueue mRq;
    
    public TmxRenderObject(TmxRenderQueue rq) {
        if (rq == null) {
            throw new IllegalArgumentException("rq == null");
        }
        
        mRq = rq;
    }
}