package org.chemodansama.engine.render;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class NpRenderQueue {
    
    private ArrayList<NpRenderOp> mROPS = new ArrayList<NpRenderOp>();
    
    public NpRenderQueue() {
        super();
    }
    
    public void clear() {
        mROPS.clear();
    }
    
    public void addRenderOp(final NpRenderOp r) {
        mROPS.add(r);
    }

    public void sortRenderOps() {
        
    }
    
    public void executeRenderOps(GL10 gl) {
        
        if (mROPS.size() > 0) {
            NpRenderOp r = mROPS.get(0);
            
            if (r != null) {
                r.setupGeometry(gl);
                r.setupMaterial(gl);
                r.setupInstance(gl);
                
                r.draw(gl);
            }
        }
        
        for (int i = 1; i < mROPS.size(); i++) {
            NpRenderOp t = mROPS.get(i);
            
            if (t != null) {
                t.setupGeometry(gl);
                t.setupMaterial(gl);
                t.setupInstance(gl);
                
                t.draw(gl);
            }
        }
        
    }
}
