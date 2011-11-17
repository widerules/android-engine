package org.chemodansama.engine.render;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpBox;
import org.chemodansama.engine.math.NpRect;

import android.util.FloatMath;

/**
 * Top-down camera
 */
public class NpTopdownCamera {
    
    public interface CameraTarget {
        float getX();
        float getY();
    }
    
    private CameraTarget mTarget = null;
    
    private float mHalfX = 0;
    private float mHalfY = 0;

    private int mW = 0;
    private int mH = 0;
    
    private float mPosX = 0;
    private float mPosY = 0;
    
    public NpTopdownCamera(CameraTarget character, int w, int h) {
        setScreenSize(w, h);
        track(character);
    }

    public void setScreenSize(int w, int h) {
        mW = w;
        mH = h;
        
        mHalfX = (float) w / 6;
        mHalfY = (float) h / 6;
    }
    
    public void track(CameraTarget character) {
        mTarget = character;
        
        if (character != null) {
            mPosX = character.getX();
            mPosY = character.getY();
        }
    }
    
    public void setupModelviewMatrix(GL10 gl) {
        gl.glLoadIdentity();
        gl.glTranslatef(FloatMath.floor(-mPosX), FloatMath.floor(-mPosY), 0);
    }
    
    public void setupProjectionMatrix(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-mW / 2, mW / 2, mH / 2, -mH / 2, -1, 1);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }
    
    public void update() {
        if (mTarget == null) {
            return;
        }
        
        float cx = mTarget.getX(); 
        
        if (cx > mPosX + mHalfX) {
            mPosX = cx - mHalfX;
        } else if (cx < mPosX - mHalfX) {
            mPosX = cx + mHalfX;
        }
        
        float cy = mTarget.getY();
        
        if (cy > mPosY + mHalfY) {
            mPosY = cy - mHalfY;
        } else if (cy < mPosY - mHalfY) {
            mPosY = cy + mHalfY;
        }
    }
    
    public NpRect getBoundsRect() {
        return new NpRect((int)FloatMath.floor(mPosX), 
                          (int)FloatMath.floor(mPosY), mW / 2, mH / 2);
    }
    
    public NpBox getBounds() {
        return new NpBox(mPosX, mPosY, mW / 2, mH / 2); 
    }
    
    public float getX() {
        return mPosX;
    }
    
    public float getY() {
        return mPosY;
    }
    
    public int getW() {
        return mW;
    }
    
    public int getH() {
        return mH;
    }
}
