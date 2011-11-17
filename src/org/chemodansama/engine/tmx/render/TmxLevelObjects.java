package org.chemodansama.engine.tmx.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpBox;
import org.chemodansama.engine.render.NpPolyBuffer;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.render.NpTopdownCamera;
import org.chemodansama.engine.tmx.TmxMap;
import org.chemodansama.engine.tmx.TmxMapObject;
import org.chemodansama.engine.tmx.TmxObjectGroup;
import org.chemodansama.engine.tmx.TmxTexturePack;
import org.chemodansama.engine.tmx.TmxTileset;

public class TmxLevelObjects extends TmxRenderObject {
    
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
    
    private final Collection<TmxMapObjectRenderer> mVisibleObjectsBuffer = 
            new ArrayList<TmxMapObjectRenderer>();
    private final TmxMap mLevel;
    private final TmxTexturePack mTexturePack;
    private TmxKdTree mKdTree = null;
    private final TreeMap<Integer, TmxMapObjectRenderer[]> mObjects = 
            new TreeMap<Integer, TmxMapObjectRenderer[]>();
            
            
    
    /**
     * Total count of render objects in the level.
     */
    private int mCount = 0;
    private NpTopdownCamera mCamera = null;
    private NpBox mCameraBounds;
    
    public TmxLevelObjects(TmxRenderQueue rq, TmxMap level, 
            TmxTexturePack texturePack, NpTopdownCamera camera) {
        super(rq);
        
        mLevel = level;
        mTexturePack = texturePack;
        
        setupObjects();
        
        setCamera(camera);
    }
    
    public TmxLevelObjects(TmxRenderQueue rq, TmxMap level, 
            TmxTexturePack texturePack) {
        this(rq, level, texturePack, null);
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
            Collection<TmxMapObjectRenderer> objects){
        
        if (objects == null) {
            return;
        }
        
        mKdTree.getVisibleObjects(cameraBounds, objects);
    }
    
    private void setupObjects() {
        
        TreeMap<Integer, Integer> counts = calcObjectsCountsByPlanes(mLevel);
        mCount = calcTotalObjectsCount(counts);
        
        TmxMapObjectRenderer[] objects = new TmxMapObjectRenderer[mCount];

        TmxTileset ts = null;
        NpTexture texture = null;
        String textureName = null;
        
        int c = 0;
        
        for (TmxObjectGroup og : mLevel.getObjectGroups()) {
            
            if (!og.visible) {
                continue;
            }
            
            int plane = getObjectGroupPlane(og);
            
            TmxMapObjectRenderer[] planeObjects = mObjects.get(plane);
            int planeCount = 0;
            if (planeObjects == null) {
                planeObjects = new TmxMapObjectRenderer[counts.get(plane)];
                counts.put(plane, planeCount);
                mObjects.put(plane, planeObjects);
            } else {
                planeCount = counts.get(plane);
            }
            
            boolean isShadow = isShadowObjectsGroup(og);
            
            for (TmxMapObject o : og.getObjects()) {
                
                if (o.gid == 0) {
                    continue;
                }
                
                if ((ts == null) || !ts.containsGid(o.gid)) {
                    ts = mLevel.getTilesetByGid(o.gid);
                    
                    if (ts == null) {
                        continue;
                    }
                }
                
                if ((texture == null) || (textureName == null) 
                        || (textureName != ts.imageName)) {
                    texture = mTexturePack.getTexture(ts.imageName);
                    if (texture == null) {
                        continue;
                    }
                    textureName = ts.imageName;
                }
                
                TmxMapObjectRenderer ro = new TmxMapObjectRenderer(o, texture, 
                                                                   ts, plane, 
                                                                   isShadow);
                
                planeObjects[planeCount++] = ro;
                objects[c++] = ro;
            }
            
            counts.put(plane, planeCount);
        }
        
        mKdTree = new TmxKdTree(objects, mLevel);
    }

    @Override
    public void draw(GL10 gl) {
        mVisibleObjectsBuffer.clear();
        
        getVisibleObjects(mCameraBounds, mVisibleObjectsBuffer);

        for (TmxMapObjectRenderer o : mVisibleObjectsBuffer) {
            o.pushRenderOp(gl, mRq);
        }
    }

    public synchronized void setCamera(NpTopdownCamera camera) {
        mCamera = camera;
        updateCameraBounds();
    }

    private void updateCameraBounds() {
        mCameraBounds = (mCamera != null) ? mCamera.getBounds() : null;
    }
    
    @Override
    public boolean update(long deltaTime) {
        updateCameraBounds();
        return true;
    }
}
