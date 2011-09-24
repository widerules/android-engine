package org.chemodansama.engine;

import java.util.ArrayList;

public abstract class NpObjectsPool<T> {
    
    private final ArrayList<T> mPool; 
    private int mIndex = 0;
    private final boolean mCanGrow;
    
    protected abstract T create();
    
    public NpObjectsPool(int initialCapacity, boolean canGrow) {
        
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity < 0");
        }
        
        mPool = new ArrayList<T>();
        for (int i = 0; i < initialCapacity; i++) {
            mPool.add(create());
        }
        mCanGrow = canGrow;
    }
    
    public T getNext() {
        if (mIndex < mPool.size()) {
            return mPool.get(mIndex++);
        }
        
        if (mCanGrow) {
            while (mIndex <= mPool.size()) {
                mPool.add(create());
            }
            return mPool.get(mIndex);
        }
        
        return null;
    }
    
    public void rewind() {
        mIndex = 0;
    }
}