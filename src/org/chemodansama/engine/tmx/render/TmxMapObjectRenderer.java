package org.chemodansama.engine.tmx.render;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.tmx.TmxMapObject;
import org.chemodansama.engine.tmx.TmxTileset;

public class TmxMapObjectRenderer {
    private static float[] constructTexcoords(TmxMapObject o, TmxTileset ts) {

        float[] texCoords = new float[4 * 2];

        if (!ts.getTileTexcoords(o.gid, texCoords)) {
            texCoords[0] = 0;
            texCoords[1] = 1;
        }
        texCoords[1] -= ts.tileHeight;
        
        float tw = ts.tileWidth;
        float th = ts.tileHeight;
        
        texCoords[1 * 2]     = texCoords[0] + tw;
        texCoords[1 * 2 + 1] = texCoords[1];
        
        texCoords[2 * 2]     = texCoords[0] + tw;
        texCoords[2 * 2 + 1] = texCoords[1] + th;
        
        texCoords[3 * 2]     = texCoords[0];
        texCoords[3 * 2 + 1] = texCoords[1] + th;
        
        return texCoords;
    }
    private static float[] constructVertices(float x, float y, 
            float w, float h) {
        float[] vertices = new float[4 * 2];

        vertices[0 * 2] = x;
        vertices[0 * 2 + 1] = y;
        
        vertices[1 * 2] = x + w;
        vertices[1 * 2 + 1] = y;
        
        vertices[2 * 2] = x + w;
        vertices[2 * 2 + 1] = y + h;
        
        vertices[3 * 2] = x;
        vertices[3 * 2 + 1] = y + h;
        
        return vertices;
    }
    
    private final float[] mVertices;

    private final float[] mTexCoords;
    private final NpTexture mTexture;
    
    private final float[] mCenter;
    private final float[] mExtents;
    public final int plane;
    
    public final boolean isShadow;
    
    private final int y;
    
    public TmxMapObjectRenderer(TmxMapObject o, NpTexture texture, 
            TmxTileset ts, int plane, boolean isShadow) {

        if (texture == null) {
            throw new IllegalArgumentException("texture == null");
        }
        
        if (ts == null) {
            throw new IllegalArgumentException("ts == null");
        }
        
        if (o == null) {
            throw new IllegalArgumentException("o == null");
        }
        
        mTexture = texture;
        
        this.plane = plane;
        this.isShadow = isShadow;

        float x = o.x;
        float y = o.y;
        
        float w = ts.tileWidthInPels;
        float h = -ts.tileHeightInPels;
        
        mCenter = new float[2];
        mCenter[0] = x + w / 2;
        mCenter[1] = y + h / 2;
        
        mExtents = new float[2];
        mExtents[0] = Math.abs(w / 2);
        mExtents[1] = Math.abs(h / 2);
        
        this.y = (int) (mCenter[1] + mExtents[1]);
        
        mVertices = constructVertices(x, y, w, h);
        mTexCoords = constructTexcoords(o, ts);
    }
    
    public float bottom() {
        return mCenter[1] - mExtents[1];
    }
    
    public float centerX() {
        return mCenter[0];
    }
    
    public float centerY() {
        return mCenter[1];
    }
    
    public float extentsX() {
        return mExtents[0];
    }
    
    public float extentsY() {
        return mExtents[1];
    }
    
    public float left() {
        return mCenter[0] - mExtents[0];
    }
    
    public void pushRenderOp(GL10 gl, TmxRenderQueue rq) {
        rq.addRenderOp(gl, y, mVertices, mTexture, mTexCoords, plane, isShadow);
    }
    
    public float right() {
        return mCenter[0] + mExtents[0];
    }
    
    public float top() {
        return mCenter[1] + mExtents[1];
    }
}