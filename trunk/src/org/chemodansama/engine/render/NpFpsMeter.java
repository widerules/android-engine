package org.chemodansama.engine.render;

public class NpFpsMeter {

    private int mFPS = 0;
    private int mFrames = 0;
    private long mTime = System.currentTimeMillis();
    
    public synchronized int getFPS() {
        return mFPS;
    }
    
    public synchronized void onRender() {
        mFrames++;
    }
    
    public void onUpdate(long timeInMillis) {
        if (timeInMillis - mTime > 1000) {
            mTime = timeInMillis;
            synchronized (this) {
                mFPS = mFrames;
                mFrames = 0;
            }
        }
    }
}