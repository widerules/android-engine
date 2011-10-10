package org.chemodansama.engine.tmx.render;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.NpObjectsPool;
import org.chemodansama.engine.math.NpBox;
import org.chemodansama.engine.math.NpRect;
import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.tmx.TmxLayer;
import org.chemodansama.engine.tmx.TmxMap;
import org.chemodansama.engine.tmx.TmxTexturePack;
import org.chemodansama.engine.tmx.TmxTileset;

import android.content.res.AssetManager;

public class TmxTilesRender {
    
    private static int getLayerPlane(TmxLayer layer) {
        if (layer == null) {
            return 0;
        }
        
        String plane = layer.getProperty("plane");
        return (plane != null) ? Integer.parseInt(plane) : 0;
    }
    
    private static boolean isShadowLayer(TmxLayer layer) {
        if (layer == null) {
            return false;
        }
        
        String isShadow = layer.getProperty("isShadow");
        return (isShadow != null) ? Integer.parseInt(isShadow) > 0 : false;
    }
    
    private final TmxTexturePack mTextures;
    
    private final TmxMap mMap;
    
    private final NpObjectsPool<TmxRenderOp> mRenderOpsPool;
    
    public TmxTilesRender(GL10 gl, AssetManager assets, 
            TmxTexturePack texturePack, TmxMap map, 
            String levelName) throws IOException {
        
        if (map == null) {
            throw new NullPointerException("map == null");
        }
        
        if (texturePack == null) {
            throw new NullPointerException("texturePack == null");
        }
        
        mRenderOpsPool = new NpObjectsPool<TmxRenderOp>(1600, true) {
            @Override
            protected TmxRenderOp create() {
                
                TmxRenderOp rop = new TmxRenderOp();
                rop.vertices = new float[4 * 2];
                rop.texCoords = new float[4 * 2];
                
                return rop;
            }
        };
        
        texturePack.addTextures(gl, assets, levelName + TmxTexturePack.EXT);
        mTextures = texturePack;
        
        mMap = map;
    }
    
    public TmxRenderOp buildRenderOp(GL10 gl, NpTexture texture,  
            float x, float y, float w, float h, 
            float tx, float ty, float tw, float th, boolean isShadow, int plane) {
        TmxRenderOp rop = mRenderOpsPool.getNext();
        
        rop.vertices[0 * 2] = x;
        rop.vertices[0 * 2 + 1] = y;
        
        rop.vertices[1 * 2] = x + w;
        rop.vertices[1 * 2 + 1] = y;
        
        rop.vertices[2 * 2] = x + w;
        rop.vertices[2 * 2 + 1] = y + h;
        
        rop.vertices[3 * 2] = x;
        rop.vertices[3 * 2 + 1] = y + h;
        
        rop.texCoords[0 * 2] = tx;
        rop.texCoords[0 * 2 + 1] = ty;
        
        rop.texCoords[1 * 2] = tx + tw;
        rop.texCoords[1 * 2 + 1] = ty;
        
        rop.texCoords[2 * 2] = tx + tw;
        rop.texCoords[2 * 2 + 1] = ty + th;
        
        rop.texCoords[3 * 2] = tx;
        rop.texCoords[3 * 2 + 1] = ty + th;
        
        rop.y = (int) (y + h);
        rop.texture = texture;
        rop.isShadow = isShadow;
        rop.plane = plane;
        
        return rop;
    }
    
    public void flushRender() {
        mRenderOpsPool.rewind();
    }

    public void render(GL10 gl, NpBox camera, TmxRenderQueue rq) {
        
        if (camera == null) {
            LogHelper.e("Cant render: camera is null");
            return;
        }
        
        //TODO: tileWidth/Height must be calculated 
        //      based on the device screen size.
        int tileWidth = mMap.getTileWidth(); 
        int tileHeigth = mMap.getTileHeight();
        
        int halfWidthInTiles = (int) camera.extents[0] / tileWidth + 1;
        int halfHeightInTiles = (int) camera.extents[1] / tileHeigth + 1;
        
        int cameraX = (int) camera.center[0] / tileWidth;
        int cameraY = (int) camera.center[1] / tileHeigth;
        
        NpRect cameraBounds = new NpRect(cameraX - halfWidthInTiles, 
                                         cameraY - halfHeightInTiles,  
                                         halfWidthInTiles * 2, 
                                         halfHeightInTiles * 2);
        
        for (TmxLayer layer : mMap.getLayers()) {
            renderLayer(gl, layer, cameraBounds, tileWidth, tileHeigth, rq);
        }
    }

    private void renderLayer(GL10 gl, TmxLayer layer, 
            NpRect cameraBounds, int tileWidth, int tileHeigth, 
            TmxRenderQueue rq) {
        
        if ((layer == null) || !layer.isVisible) {
            return;
        }
        
        if (gl == null) {
            return;
        }
        
        if (cameraBounds == null) {
            return;
        }
        
        int layerPlane = getLayerPlane(layer);
        boolean isShadow = isShadowLayer(layer);
        
        TmxTileset ts = null;
        NpVec2 tc = new NpVec2();
        
        NpTexture texture = null;
        String textureName = null;
        
        TmxRenderOp rop = null;
        
        for (int x = cameraBounds.x; x < cameraBounds.right(); x++) {
            for (int y = cameraBounds.y; y < cameraBounds.top(); y++) {
                int tileId = layer.getTileGid(x, y);
                
                if (tileId == 0) {
                    continue;
                }
                
                if ((ts == null) || !ts.containsGid(tileId)) {
                    ts = mMap.getTilesetByGid(tileId);
                    
                    if (ts == null) {
                        continue;
                    }
                }
                
                if ((texture == null) || (textureName == null) 
                        || !textureName.equals(ts.name)) {
                    textureName = ts.name;
                    texture = mTextures.getTexture(textureName);
                    
                    if (texture == null) {
                        continue;
                    }
                }
                
                if (!ts.getTileTexcoords(tileId, tc)) {
                    continue;
                }
                
                rop = buildRenderOp(gl, texture, x * tileWidth, y * tileHeigth, 
                                    tileWidth, tileHeigth, tc.coords[0], 
                                    tc.coords[1], ts.tileWidth, -ts.tileHeight, 
                                    isShadow, layerPlane);
                
                rq.addRenderOp(gl, rop);
            }
        }
    }
}