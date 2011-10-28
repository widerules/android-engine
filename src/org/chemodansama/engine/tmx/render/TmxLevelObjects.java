package org.chemodansama.engine.tmx.render;

import java.util.Collection;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpBox;
import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.render.NpPolyBuffer;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.tmx.TmxMap;
import org.chemodansama.engine.tmx.TmxObject;
import org.chemodansama.engine.tmx.TmxObjectGroup;
import org.chemodansama.engine.tmx.TmxTexturePack;
import org.chemodansama.engine.tmx.TmxTileset;

public class TmxLevelObjects {
    
    private static TreeMap<Integer, Integer> calcObjectsCountsByPlanes(TmxMap map) {
        
        if (map == null) {
            throw new IllegalArgumentException("map == null");
        }
        
        TreeMap<Integer, Integer> counts = 
                new TreeMap<Integer, Integer>();

        for (TmxObjectGroup og : map.getObjectGroups()) {
            if (!og.visible) {
                continue;
            }
            int plane = getObjectGroupPlane(og);
            
            int newPlaneCnt = getIntValue(counts.get(plane)) 
                              + og.getObjectsCount();
            
            counts.put(plane, newPlaneCnt);
        }
        
        return counts;
    }
    
    private static int calcTotalObjectsCount(TreeMap<Integer, Integer> counts) {
        
        if (counts == null) {
            throw new IllegalArgumentException("counts == null");
        }
        
        int r = 0;
        for (Integer i : counts.values()) {
            r += i;
        } 
        return r;
    }

    /**
     * @param i
     * @return int value of the parameter or zero if the parameter is null
     */
    private static int getIntValue(Integer i) {
        return (i == null) ? 0 : i;
    }
    
    private static int getObjectGroupPlane(TmxObjectGroup og) {
        if (og == null) {
            return 0;
        }
        
        String plane = og.getProperty("plane");
        return (plane != null) ? Integer.parseInt(plane) : 0;
    }
    
    private static boolean isShadowObjectsGroup(TmxObjectGroup og) {
        String isShadow = og.getProperty("isShadow");
        
        return (isShadow != null) && (Integer.parseInt(isShadow) != 0);
    }
    
    private final TmxMap mLevel;
    
    private final TmxTexturePack mTexturePack;
    
    private TmxKdTree mKdTree = null;
    
    private final TreeMap<Integer, TmxRenderObject[]> mObjects = 
            new TreeMap<Integer, TmxRenderObject[]>();
    
    /**
     * Total count of render objects in the level.
     */
    private int mCount = 0;
    
    public TmxLevelObjects(TmxMap level, TmxTexturePack texturePack) {
        
        mLevel = level;
        mTexturePack = texturePack;
        
        setupObjects();
    }
    
    public void debugRender(GL10 gl, NpPolyBuffer pb) {
        mKdTree.debugRender(gl, pb);
    }
    
    public int getCount() {
        return mCount;
    }
    
    /**
     * @param cameraBounds
     */
    public void getVisibleObjects(NpBox cameraBounds, 
            Collection<TmxRenderObject> objects){
        
        if (objects == null) {
            return;
        }
        
        mKdTree.getVisibleObjects(cameraBounds, objects);
    }
    
    private void setupObjects() {
        
        TreeMap<Integer, Integer> counts = calcObjectsCountsByPlanes(mLevel);
        mCount = calcTotalObjectsCount(counts);
        
        TmxRenderObject[] objects = new TmxRenderObject[mCount];

        TmxTileset ts = null;
        NpTexture texture = null;
        String textureName = null;
        NpVec2 tc = new NpVec2();
        
        int c = 0;
        int w = 0;
        int h = 0;
        
        for (TmxObjectGroup og : mLevel.getObjectGroups()) {
            
            if (!og.visible) {
                continue;
            }
            
            int plane = getObjectGroupPlane(og);
            
            TmxRenderObject[] planeObjects = mObjects.get(plane);
            int planeCount = 0;
            if (planeObjects == null) {
                planeObjects = new TmxRenderObject[counts.get(plane)];
                counts.put(plane, planeCount);
                mObjects.put(plane, planeObjects);
            } else {
                planeCount = counts.get(plane);
            }
            
            boolean isShadow = isShadowObjectsGroup(og);
            
            for (TmxObject o : og.getObjects()) {
                
                if (o.gid == 0) {
                    continue;
                }
                
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
                
                TmxRenderObject ro = new TmxRenderObject(o.x, o.y, w, -h, 
                                                         texture, 
                                                         tc.coords[0], 
                                                         tc.coords[1], 
                                                         ts.tileWidth, 
                                                         ts.tileHeight, 
                                                         plane, isShadow);
                
                planeObjects[planeCount++] = ro;
                objects[c++] = ro;
            }
            
            counts.put(plane, planeCount);
        }
        
        mKdTree = new TmxKdTree(objects, mLevel);
    }
}
