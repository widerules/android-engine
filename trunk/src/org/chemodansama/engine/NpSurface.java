package org.chemodansama.engine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.render.imgui.NpSkin;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

final class NpRenderer implements GLSurfaceView.Renderer {

    private final AssetManager mAssets;
    private final NpActivityTerminator mTerminator;
    private final NpGame mGame;

    private final String mSchemeName;
    
    private NpGameUpdateThread mUpdater = null;
    
    public NpRenderer(AssetManager assets, NpActivityTerminator ft, 
            NpGame game, String schemeName) {
        super();
        
        mSchemeName = schemeName;
        
        mGame = game;
        
        mTerminator = ft;
        
        mUpdater = new NpGameUpdateThread(mGame, mTerminator);
        mAssets = assets;
    }
    
    @Override
    public void onDrawFrame(GL10 gl) {
        mGame.render(gl);
    }
    
    public void onPause() {
        if (mUpdater != null) {
            mUpdater.terminate();
            mUpdater.join();
            mUpdater = null; 
        }
    }

    public void onResume() {
        if (mUpdater == null) {
            mUpdater = new NpGameUpdateThread(mGame, mTerminator);
        }

        
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mGame.setupOnSurfaceChanged(gl, width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        
        NpSkin.loadScheme(gl, mAssets, mSchemeName);
        
        mGame.onSurfaceCreated(gl, config, mAssets);
        
        mUpdater.start();
    }
    
    boolean onTouchEvent(MotionEvent event) {
        return mGame.onTouchEvent(event);
    }
    
    boolean handleKeyEvent(int keyCode, KeyEvent event) {
        return mGame.onKeyEvent(keyCode, event);
    }
    
    public boolean onBackPressed() {
        return mGame.onBackPressed();
    }
}

public final class NpSurface extends GLSurfaceView {

    private NpRenderer mRenderer = null;
    
    public NpSurface(Context context, NpActivityTerminator ft, NpGame game, 
            String schemeName) {
        super(context);
        this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        
        mRenderer = new NpRenderer(context.getAssets(), ft, game, schemeName);
        setRenderer(mRenderer);
        getHolder().setFormat(PixelFormat.TRANSPARENT);        
    }
    
    @Override
    public void onPause() {
        mRenderer.onPause();
        super.onPause();
    }
    
    @Override
    public void onResume() {
        mRenderer.onResume();
        super.onResume();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRenderer != null) {
            return mRenderer.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }
    
    public boolean handleKeyEvent(int keyCode, KeyEvent event) {
        return mRenderer.handleKeyEvent(keyCode, event);
    }
    
    public boolean onBackPressed() {
        return mRenderer.onBackPressed();
    }
}