package org.chemodansama.engine;

final class NpGameUpdateThread implements Runnable {

    private final NpGame mGame;
    
    private volatile boolean mSuspended = false;
    private volatile boolean mTerminated = false;

    private final NpActivityTerminator mTerminator;
    private final Thread mThread;
    
    public NpGameUpdateThread(NpGame g, NpActivityTerminator ft) {
        super();
        
        mTerminator = ft;
        mGame = g;
        mThread = new Thread(this, "updater thread");
    }
    
    synchronized void resume() {
        mSuspended = false;
    }
    
    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (mTerminated) {
                    break;
                }
            }
            
            if ((mGame != null) && mGame.update()) {
                synchronized (this) {
                    if (mTerminator != null) {
                        mTerminator.finish();                        
                    }
                }
                break;
            }
            
            try {
                while (true) {
                    Thread.sleep(10);
                    synchronized (this) {
                        if (!mSuspended) {
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    void start() {
        if (!mThread.isAlive()) {
            mThread.start();
        }
    }
    
    synchronized void suspend() {
        mSuspended = true;
    }
    
    void terminateAndJoin() {
        mTerminated = true;
        mSuspended = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
