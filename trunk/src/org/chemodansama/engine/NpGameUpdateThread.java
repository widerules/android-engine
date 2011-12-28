package org.chemodansama.engine;

final class NpGameUpdateThread implements Runnable {

    private final NpGame mGame;
    
    private volatile boolean mSuspended = false;
    private volatile boolean mTerminated = false;

    private final NpActivityTerminator mTerminator;
    private final Thread mThread;
    
    public NpGameUpdateThread(NpGame game, NpActivityTerminator ft) {
        super();
        
        if (game == null) {
            throw new IllegalArgumentException("game == null");
        }
        
        mTerminator = ft;
        mGame = game;
        mThread = new Thread(this, "updater thread");
    }
    
    void resume() {
        mSuspended = false;
    }
    
    @Override
    public void run() {
        while (true) {
            if (mTerminated) {
                break;
            }
            
            if (mGame.update()) {
                if (mTerminator != null) {
                    mTerminator.finish();                        
                }
                break;
            }
            
            try {
                while (true) {
                    Thread.sleep(10);
                    if (!mSuspended) {
                        break;
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
    
    void suspend() {
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
