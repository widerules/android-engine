package org.chemodansama.engine.render;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.tmx.TmxTileset;
import org.chemodansama.engine.tmx.render.TmxRenderQueue;

public class NpSprite {
    
    private final static float[] VERTS = 
        {-0.5f, 0, 0.5f, 0, 0.5f, -1, -0.5f, -1};
    
    public interface NpSpriteListener {
        void loopEnd(NpSprite s);
    }
    
    private final float[] mVertices = new float[4 * 2];
    private final float[] mTexCoords = new float[4 * 2];
    
    private final int level;
    public final NpSpriteTemplate template;
    private final long mFrameDelay;

    private final ArrayList<NpSpriteListener> mListeners = 
            new ArrayList<NpSprite.NpSpriteListener>();
    
    private boolean mActive = true;
    private boolean mLooped = false;
    private long mFrameTime = 0;
    private int mFrame = 0;
    
    public float x;
    public float y;

    public NpSprite(NpSpriteTemplate template, int level, float x, float y) {
        if (template == null) {
            throw new IllegalArgumentException("template == null");
        }
        this.template = template;
        
        this.level = level;
        this.x = x;
        this.y = y;
        
        mFrameDelay = (template.animation.fps != 0) 
                      ? 1000 / template.animation.fps 
                      : 1;
    }
    
    public NpSprite(NpSpriteTemplate template, int level, float x, float y, 
            boolean looped) {
        this(template, level, x, y);
        this.mLooped = looped;
    }
    
    public void addListener(NpSpriteListener listener) {
        if ((listener == null) || (mListeners.contains(listener))) {
            return;
        }
        
        mListeners.add(listener);
    }
    
    public void removeListener(NpSpriteListener listener) {
        mListeners.remove(listener);
    }

    public void update(long timeInMilli) {
        if (mFrameTime == 0) {
            mFrameTime = timeInMilli;
        }
        
        if (!mActive) {
            return;
        }
        
        if (timeInMilli - mFrameTime > mFrameDelay) {
            mFrame++;
            
            if (mFrame >= template.animation.sequence.length) {
                if (!mLooped) {
                    mFrame = template.animation.sequence.length;
                    mActive = false;
                } else {
                    mFrame = 0;
                }
            }
            
            mFrameTime = timeInMilli;
        }
    }
    
    public void render(GL10 gl, TmxRenderQueue rq) {
        
        if (rq == null) {
            return;
        }
        
        if (!mActive) { 
            return;
        }
        
        int gid = template.animation.sequence[mFrame]; 

        updateVertices();
        updateTexCoords(template.tileset, gid);
        
        rq.addRenderOp(gl, (int) y, mVertices, 
                       template.texture, mTexCoords, level, false);
    }
    
    /**
     * Prepare texture coords array for draw call with specified tileset and gid
     * @param tileset current animation tileset
     * @param gid current animation frame within tileset
     */
    private void updateTexCoords(TmxTileset tileset, int gid) {
        
        if (!tileset.getTileTexcoords(gid, mTexCoords)) {
            return;
        }
        mTexCoords[1] -= tileset.tileHeight;
        
        // CCW order here.
        mTexCoords[1 * 2 + 0] = mTexCoords[0] + tileset.tileWidth;
        mTexCoords[1 * 2 + 1] = mTexCoords[1];
        mTexCoords[2 * 2 + 0] = mTexCoords[0] + tileset.tileWidth;
        mTexCoords[2 * 2 + 1] = mTexCoords[1] + tileset.tileHeight;
        mTexCoords[3 * 2 + 0] = mTexCoords[0];
        mTexCoords[3 * 2 + 1] = mTexCoords[1] + tileset.tileHeight;
    }
    
    /**
     * Prepare vertices coords array for draw call.
     */
    private void updateVertices() {
        // translate base vertices to the player position
        for (int i = 0; i < 4; i++) {
            mVertices[i * 2] = 
                    VERTS[i * 2] * template.tileset.tileWidthInPels + x;
            
            mVertices[i * 2 + 1] = 
                    VERTS[i * 2 + 1] * template.tileset.tileWidthInPels 
                        + y + template.animation.verticalOrigin;
        }
    }
}
