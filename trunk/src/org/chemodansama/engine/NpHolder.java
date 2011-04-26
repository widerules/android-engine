package org.chemodansama.engine;

public final class NpHolder<T> {
    
    public T value;
    
    public NpHolder() {
        this.value = null;
    }
    
    public NpHolder(T value) {
        this.value = value;
    }
}
