package org.chemodansama.engine.tmx.render;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.render.NpTexture;

public class TmxMapObjectRenderer {
    private final float[] mVertices;
    private final float[] mTexCoords;
    
    private final NpTexture mTexture;

    private final float[] mCenter;
    private final float[] mExtents;
    
    public final int plane;
    public final boolean isShadow;
    private final int y;
    
    public TmxMapObjectRenderer(int x, int y, int w, int h, NpTexture texture, 
            float tx, float ty, float tw, float th, int plane, 
            boolean isShadow) {

        this.mTexture = texture;
        this.plane = plane;
        this.isShadow = isShadow;

        mCenter = new float[2];
        mCenter[0] = x + w / 2;
        mCenter[1] = y + h / 2;
        
        mExtents = new float[2];
        mExtents[0] = Math.abs(w / 2);
        mExtents[1] = Math.abs(h / 2);
        
        this.y = (int) (mCenter[1] + mExtents[1]);
        
        mVertices = new float[4 * 2];
        mTexCoords = new float[4 * 2];

        mVertices[0 * 2] = x;
        mVertices[0 * 2 + 1] = y;
        
        mVertices[1 * 2] = x + w;
        mVertices[1 * 2 + 1] = y;
        
        mVertices[2 * 2] = x + w;
        mVertices[2 * 2 + 1] = y + h;
        
        mVertices[3 * 2] = x;
        mVertices[3 * 2 + 1] = y + h;
        
        mTexCoords[0 * 2] = tx;
        mTexCoords[0 * 2 + 1] = ty;
        
        mTexCoords[1 * 2] = tx + tw;
        mTexCoords[1 * 2 + 1] = ty;
        
        mTexCoords[2 * 2] = tx + tw;
        mTexCoords[2 * 2 + 1] = ty + th;
        
        mTexCoords[3 * 2] = tx;
        mTexCoords[3 * 2 + 1] = ty + th;
    }
    
    public void pushRenderOp(GL10 gl, TmxRenderQueue rq) {
        rq.addRenderOp(gl, y, mVertices, mTexture, mTexCoords, plane, isShadow);
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
    
    public float right() {
        return mCenter[0] + mExtents[0];
    }
    
    public float top() {
        return mCenter[1] + mExtents[1];
    }
    
    public float bottom() {
        return mCenter[1] - mExtents[1];
    }
}