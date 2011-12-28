package org.chemodansama.engine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.render.imgui.NpSkin;

import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

final class NpRenderer implements GLSurfaceView.Renderer {

    private final AssetManager mAssets;
    private final NpActivityTerminator mTerminator;
    private final NpGame mGame;

    private final String mSchemeName;
    
    private final NpGameUpdateThread mUpdater;
    
    NpRenderer(AssetManager assets, NpActivityTerminator ft, 
            NpGame game, String schemeName) {
        super();
        
        mSchemeName = schemeName;
        
        mGame = game;
        
        mTerminator = ft;
        
        mUpdater = new NpGameUpdateThread(mGame, mTerminator);
        mAssets = assets;
    }
    
    void onDestroy() {
        mUpdater.terminateAndJoin();
    }
    
    @Override
    public void onDrawFrame(GL10 gl) {
        mGame.render(gl);
    }
    
    synchronized void onPause() {
        mUpdater.suspend();
    }

    synchronized void onResume() {
        mUpdater.resume();
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mGame.setupOnSurfaceChanged(gl, width, height);
    }

    @Override
    synchronized public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        NpSkin.loadScheme(gl, mAssets, mSchemeName);
        
        mGame.onSurfaceCreated(gl, config, mAssets);
        
        mUpdater.resume();
        mUpdater.start();
    }
    
    boolean onTouchEvent(MotionEvent event) {
        return mGame.onTouchEvent(event);
    }
    
    boolean handleKeyEvent(int keyCode, KeyEvent event) {
        return mGame.onKeyEvent(keyCode, event);
    }
    
    boolean onBackPressed() {
        return mGame.onBackPressed();
    }
}