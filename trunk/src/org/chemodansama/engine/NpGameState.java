package org.chemodansama.engine;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import android.content.res.AssetManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class NpGameState {

    protected abstract class OnRenderAction {
        abstract protected void perform(GL10 gl);
    }
    
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;
    
    protected AssetManager mAssets;
    protected final NpGame mGame;
    
    private final ArrayList<OnRenderAction> mOnRenderActions = 
            new ArrayList<NpGameState.OnRenderAction>();
    
    protected void addOnRenderAction(OnRenderAction action) {
        if (action == null) {
            return;
        }
        mOnRenderActions.add(action);
    }
    
    protected NpGameState(NpGame game, GL10 gl, AssetManager assets) {
        super();
        
        if (game == null) {
            throw new IllegalArgumentException("game == null");
        }
        
        if (assets == null) {
            throw new IllegalArgumentException("assets == null");
        }
        
        mAssets = assets;
        mGame = game;
    }
    
    protected boolean handleKeyEvent(int keyCode, KeyEvent event) {
        return false;
    }
    
    protected void onForeground() {
        
    }
    
    protected boolean onBackPressed() {
        return false;
    }
    
    protected void onRelease(GL10 gl) {
        
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
        mAssets = assets;
    }
    
    protected void render(GL10 gl) {
        for (OnRenderAction a : mOnRenderActions) {
            a.perform(gl);
        }
        mOnRenderActions.clear();
    }
    
    /** setupOnSurfaceChanged - called when surface has changed its size 
     *                          or state pops up to the head 
     *                          of the game-states stack
     * @param gl
     * @param width
     * @param height
     */
    protected void setupOnSurfaceChanged(GL10 gl, int width, int height) {
        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }
        
        mSurfaceWidth = width; 
        mSurfaceHeight = height;
    }
    
    protected void update() {
        
    }

    public final int getSurfaceWidth() {
        return mSurfaceWidth;
    }

    public final int getSurfaceHeight() {
        return mSurfaceHeight;
    }
}
