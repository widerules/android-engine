package org.chemodansama.engine;

final class NpGameUpdateThread implements Runnable {

    private Thread mThread = null;
    
    private NpGame mGame = null;
    private volatile boolean mTerminated = false;

    private NpActivityTerminator mTerminator = null;
    
    public NpGameUpdateThread(NpGame g, NpActivityTerminator ft) {
        super();
        
        mTerminator = ft;
        
        mGame = g;
        
        mThread = new Thread(this, "updater thread");
    }
    
    public void join() {
        try {
            mThread.join();
        } catch (InterruptedException e) {
        }
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
            }
            
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }
    
    
    public void start() {
        mThread.start();
    }
    
    synchronized public void terminate() {
        mTerminated = true;
    }
}
