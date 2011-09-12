package org.chemodansama.engine.tmx.render;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.math.NpBox;
import org.chemodansama.engine.math.NpRect;
import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.render.NpPolyBuffer;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.tmx.TmxLayer;
import org.chemodansama.engine.tmx.TmxMap;
import org.chemodansama.engine.tmx.TmxTexturePack;
import org.chemodansama.engine.tmx.TmxTileset;

import android.content.res.AssetManager;

public class TmxTilesRender {
    private static final int RENDER_BUFFER_SIZE = 32; // buffer size in quads.
    private static final String TEXPACK_EXT = ".tex";
    
    private final TmxTexturePack mTextures;
    private final TmxMap mMap;
    
    public TmxTilesRender(GL10 gl, AssetManager assets, 
            TmxTexturePack texturePack, TmxMap map, 
            String levelName) throws IOException {
        
        if (map == null) {
            throw new NullPointerException("map == null");
        }
        
        if (texturePack == null) {
            throw new NullPointerException("texturePack == null");
        }
        
        texturePack.addTextures(gl, assets, levelName + TEXPACK_EXT);
        mTextures = texturePack;
        
        mMap = map;
    }
    
    private void renderLayer(GL10 gl, TmxLayer layer, 
            NpRect cameraBounds, int tileWidth, int tileHeigth) {
        
        if ((layer == null) || !layer.isVisible) {
            return;
        }
        
        if (gl == null) {
            return;
        }
        
        if (cameraBounds == null) {
            return;
        }
        
        TmxTileset ts = null;
        NpVec2 tc = new NpVec2();
        
        NpTexture texture = null;
        String textureName = null;
        boolean textureBound = false;
        
        NpPolyBuffer pb = new NpPolyBuffer(RENDER_BUFFER_SIZE);
        
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
                    textureBound = false;
                    
                    if (texture == null) {
                        continue;
                    }
                }
                
                if (!ts.getTileTexcoords(tileId, tc)) {
                    continue;
                }
                
                if (!textureBound) {
                    // flush poly buffer on texture change.
                    pb.flushRender(gl);
                    
                    if (texture.bindGL10(gl)) {
                        textureBound = true;
                    }
                }
                
                pb.pushQuadWH(gl, x * tileWidth, y * tileHeigth, 
                              tileWidth, tileHeigth, tc.coords[0], tc.coords[1], 
                              ts.tileWidth, -ts.tileHeight);
            }
        }
        pb.flushRender(gl);
    }
    
    public void render(GL10 gl, NpBox camera) {
        
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
        
        setupGLStates(gl);
        
        for (TmxLayer layer : mMap.getLayers()) {
            renderLayer(gl, layer, cameraBounds, tileWidth, tileHeigth);
        }
        
        tearDownGLStates(gl);
    }

    private static void setupGLStates(GL10 gl) {
        if (gl == null) {
            LogHelper.e("Cant setup GL states for WireLevel: gl is null");
            return;
        }

        gl.glEnable(GL10.GL_TEXTURE_2D);

        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glColor4f(1, 1, 1, 1);
    }

    private static void tearDownGLStates(GL10 gl) {
        if (gl == null) {
            LogHelper.e("Cant tear down GL states for WireLevel: gl is null");
            return;
        }

        gl.glDisable(GL10.GL_BLEND);
    }
}