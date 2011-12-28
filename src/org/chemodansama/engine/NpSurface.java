package org.chemodansama.engine;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

public final class NpSurface extends GLSurfaceView {

    private final NpRenderer mRenderer;
    
    public NpSurface(Context context, NpActivityTerminator ft, NpGame game, 
            String schemeName) {
        super(context);
        this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        
        mRenderer = new NpRenderer(context.getAssets(), ft, game, schemeName);
        setRenderer(mRenderer);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);        
    }
    
    public boolean handleKeyEvent(int keyCode, KeyEvent event) {
        return mRenderer.handleKeyEvent(keyCode, event);
    }
    
    public boolean onBackPressed() {
        return mRenderer.onBackPressed();
    }
    
    public void onDestroy() {
        mRenderer.onDestroy();
    }
    
    @Override
    public void onPause() {
        mRenderer.onPause();
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mRenderer.onTouchEvent(event);
    }
}