package org.chemodansama.engine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import android.content.res.AssetManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class NpGameState {
    
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;
    
    final protected AssetManager mAssets;
    final protected NpGame mGame;
    
    protected NpGameState(NpGame g, GL10 gl, AssetManager assets) {
        super();
        
        mAssets = assets;
        mGame = g;
    }
    
    protected boolean handleKeyEvent(int keyCode, KeyEvent event) {
        return false;
    }
    
    protected void onForeground() {
        
    }
    
    protected boolean onBackPressed() {
        return false;
    }
    
    protected void handleMotionEvent(MotionEvent event) {
        
    }
    
    /** refreshContextAssets - called when the context is lost.
     * @param gl
     * @param config
     * @param assets
     */
    protected void refreshContextAssets(GL10 gl, EGLConfig config, 
            AssetManager assets) {
    }
    
    protected void render(GL10 gl) {
        
    }
    
    /** setupOnSurfaceChanged - called when surface has changed its size 
     *                          or state pops up at the head 
     *                          of the game-states stack
     * @param gl
     * @param width
     * @param height
     */
    protected void setupOnSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        
        mSurfaceWidth = width; 
        mSurfaceHeight = height;
    }
    
    protected void update() {
        
    }

    protected final int getSurfaceWidth() {
        return mSurfaceWidth;
    }

    protected final int getSurfaceHeight() {
        return mSurfaceHeight;
    }
}
