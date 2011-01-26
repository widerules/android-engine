package org.chemodansama.engine.render;

import java.util.ArrayList;

import org.chemodansama.engine.math.NpMatrix4;

public class NpRenderableList extends NpRenderable {

    private ArrayList<NpRenderable> mChilds = new ArrayList<NpRenderable>();
    
    @Override
    public void update(final int deltaTime, final NpCamera camera, 
            final NpMatrix4 parentMat) {
        for (NpRenderable r : mChilds) {
            r.update(deltaTime, camera, parentMat);
        }
    }
    
    @Override
    public void attachChild(NpRenderable r) {
        mChilds.add(r);
    }
    
    @Override
    public void collectRenderOps(NpCamera camera, final NpRenderQueue rq) {
        for (NpRenderable r : mChilds) {
            r.collectRenderOps(camera, rq);
        }
    }
}
