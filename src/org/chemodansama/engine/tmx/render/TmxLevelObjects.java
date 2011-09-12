package org.chemodansama.engine.tmx.render;

import java.util.Collection;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpBox;
import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.render.NpBlendMode;
import org.chemodansama.engine.render.NpPolyBuffer;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.tmx.TmxMap;
import org.chemodansama.engine.tmx.TmxObject;
import org.chemodansama.engine.tmx.TmxObjectGroup;
import org.chemodansama.engine.tmx.TmxTexturePack;
import org.chemodansama.engine.tmx.TmxTileset;

/* TODO:
 * 1/ Objects must be rendered by layers. 
 * 2/ Use kd-Tree for camera culling.
 * 3/ 
 */

public class TmxLevelObjects {
    private final TmxMap mLevel;
    private final TmxTexturePack mTexturePack;

    private TmxRenderObject[] mObjects = null;
    
    private TmxKdTree mKdTree;
    
    private boolean isMultiplicativeObjectsGroup(TmxObjectGroup og) {
        String isShadow = og.getProperty("isShadow");
        
        return (isShadow != null) && (Integer.parseInt(isShadow) != 0);
    }
    
    private void setupObjects() {
        
        int cnt = 0;
        for (TmxObjectGroup og : mLevel.getObjectGroups()) {
            cnt += og.getObjectsCount();
        }
        mObjects = new TmxRenderObject[cnt];

        TmxTileset ts = null;
        NpTexture texture = null;
        String textureName = null;
        NpVec2 tc = new NpVec2();
        
        int c = 0;
        
        int w = 0;
        int h = 0;
        
        for (TmxObjectGroup og : mLevel.getObjectGroups()) {
            
            NpBlendMode blend = (isMultiplicativeObjectsGroup(og))
                                ? NpBlendMode.MULT
                                : NpBlendMode.ADD;
            
            for (TmxObject o : og.getObjects()) {
                if ((ts == null) || !ts.containsGid(o.gid)) {
                    ts = mLevel.getTilesetByGid(o.gid);
                    
                    if (ts == null) {
                        continue;
                    }
                    
                    w = ts.tileWidthInPels;
                    h = ts.tileHeightInPels;
                }
                
                if ((texture == null) || (textureName == null) 
                        || (textureName != ts.name)) {
                    texture = mTexturePack.getTexture(ts.name);
                    if (texture == null) {
                        continue;
                    }
                    textureName = ts.name;
                }
                
                if (!ts.getTileTexcoords(o.gid, tc)) {
                    continue;
                }
                
                tc.coords[1] -= ts.tileHeight;
                
                mObjects[c] = 
                        new TmxRenderObject(o.x, o.y, w, -h, 
                                            texture, 
                                            tc.coords[0], tc.coords[1], 
                                            ts.tileWidth, ts.tileHeight, blend);
                c++;
            }
        }
        
        mKdTree = new TmxKdTree(mObjects, mLevel);
    }
    
    public TmxLevelObjects(TmxMap level, TmxTexturePack texturePack) {
        
        mLevel = level;
        mTexturePack = texturePack;
        
        setupObjects();
    }
    
    
    public void debugRender(GL10 gl, NpPolyBuffer pb) {
        mKdTree.debugRender(gl, pb);
    }
    
    /**
     * @param cameraBounds
     * @return immutable collection of visible objects.
     */
    public void getVisibleObjects(NpBox cameraBounds, 
            Collection<TmxRenderObject> objects){
        mKdTree.getVisibleObjects(cameraBounds, objects);
    }
    
    public int getCount() {
        return mObjects.length;
    }
}
