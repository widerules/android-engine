package org.chemodansama.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.render.imgui.NpGuiState;

import android.app.Activity;
import android.content.res.AssetManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

public abstract class NpGame {
    
    private final Activity mActivity;
    
    private int mHeight = 0;
    
    private boolean mNeedSetupStateSurfaceSize = false;
    
    private NpVec2 mPointerCoord = new NpVec2();
    private boolean mPointerDown = false;
    
    private NpVec2 mPointerOffset = new NpVec2();
    private Stack<NpGameState> mStates = new Stack<NpGameState>();
    ArrayList<NpGameState> mStatesToDelete = new ArrayList<NpGameState>();
    
    private int mWidth = 0;
    
    private final Collection<Iterable<? extends NpGlContextAsset>> mContextAssets = 
            new ArrayList<Iterable<? extends NpGlContextAsset>>();
    private volatile boolean mPendingRelease = false;
    
    public NpGame(Activity activity) {
        super();
        
        mActivity = activity;
    }
    
    public void addContextAssetsCollection(Iterable<? extends NpGlContextAsset> assets) {
        mContextAssets.add(assets);
    }
    
    public abstract NpGameState constructInitialState(NpGame g, GL10 gl, 
            AssetManager assets);
    
    synchronized public Activity getActivity() {
        return mActivity;
    }
    
    synchronized public final float getPointerOffsetX() {
        return mPointerOffset.getX();
    }
    
    synchronized public final float getPointerOffsetY() {
        return mPointerOffset.getY();
    }
    
    synchronized public final int getStatesCount() {
        return mStates.size();
    }
    
    synchronized public final boolean isEmpty() {
        return mStates.isEmpty();
    }
    
    synchronized public final boolean isPointerDown() {
        return mPointerDown;
    }
    
    synchronized public final boolean onBackPressed() {
        if (mStates.size() > 0) {
            return mStates.peek().onBackPressed(); 
        } else {
            return false;
        }
    }
    
    synchronized public final boolean onKeyEvent(int keyCode, KeyEvent event) {
        if (mStates.size() > 0) {
            return mStates.peek().handleKeyEvent(keyCode, event); 
        } else {        
            return false;
        }
    }
    
    synchronized public final void onSurfaceCreated(GL10 gl, EGLConfig config, 
            AssetManager assets) {
        if (mStates.isEmpty()) {
            pushState(constructInitialState(this, gl, assets));
        } else {
            for (NpGameState s : mStates) {
                s.refreshContextAssets(gl, config, assets);
            }
        }
    }
    
    synchronized public final boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            
            mPointerOffset.setValues(0, 0);
            mPointerCoord.setValues(event.getX(), event.getY());
            mPointerDown = true;
            
            NpGuiState.onMouseMove((int)mPointerCoord.getX(),
                                   (int)mPointerCoord.getY());
            
            NpGuiState.onMouseDown();
            
            break;

        case MotionEvent.ACTION_MOVE:
            if (mPointerDown) {
                
                mPointerOffset.setValues(mPointerCoord.getX() - event.getX(), 
                                         mPointerCoord.getY() - event.getY());
                
                mPointerCoord.setValues(event.getX(), event.getY());
                
                NpGuiState.onMouseMove((int) mPointerCoord.getX(), 
                                       (int) mPointerCoord.getY());
            }
            break;

        case MotionEvent.ACTION_UP:
            mPointerOffset.setValues(0, 0);
            mPointerDown = false;
            NpGuiState.onMouseUp();
            break;
        }
        
        if (mStates.size() > 0) {
            mStates.peek().handleMotionEvent(event);
        }
        
        return true;
    }
    
    synchronized public final void popState() {
        if (mStates.size() > 0) {
            mStatesToDelete.add(mStates.pop());
        }
        
        if (mStates.size() > 0) {
            mStates.peek().onForeground();
            mNeedSetupStateSurfaceSize = true;
        }
    }
    
    synchronized public final void pushState(final NpGameState state) {
        if (state == null) {
            return;
        }
        
        mStates.push(state);
        
        mNeedSetupStateSurfaceSize = true;
    }
    
    synchronized public final void render(GL10 gl) {

        renderActiveState(gl);
        
        for (NpGameState s : mStatesToDelete) {
            s.onRelease(gl);
        }
        mStatesToDelete.clear();
        
        if (mPendingRelease) {
            for (Iterable<? extends NpGlContextAsset> assets : mContextAssets) {
                for (NpGlContextAsset asset : assets) {
                    asset.releaseAssets(gl);
                }
            }
            mContextAssets.clear();
            mPendingRelease = false;
        }
    }
    
    private void renderActiveState(GL10 gl) {
        if (mStates.size() <= 0) {
            return;
        }

        NpGameState s = mStates.peek(); 

        if (s == null) {
            return;
        }

        if (mNeedSetupStateSurfaceSize) {
            s.setupOnSurfaceChanged(gl, mWidth, mHeight);
            mNeedSetupStateSurfaceSize = false;
        }

        s.render(gl);
    }

    synchronized public final void setupOnSurfaceChanged(GL10 gl, int width, 
            int height) {
        
        mWidth = width; 
        mHeight = height;
        
        if (mStates.size() > 0) {
            mStates.peek().setupOnSurfaceChanged(gl, width, height); 
        }        
    }
    
    public final void shutDown() {
        mStatesToDelete.addAll(mStates);
        mStates.clear();
        
        mPendingRelease = true;
    }
    
    /**
     * @return {@code true} if can shut down application.
     *         {@code false} otherwise. 
     */
    synchronized final boolean update() {

        boolean r = false;

        if (mStates.size() > 0) {
            mStates.peek().update();
        } else {
            r = mStatesToDelete.size() == 0;
        }
        
        return r;
    }
}
