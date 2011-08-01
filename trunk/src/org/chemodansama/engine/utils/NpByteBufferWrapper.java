package org.chemodansama.engine.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class NpByteBufferWrapper {
    static final public FloatBuffer allocateFloatBuffer(int size) {
        final int bufSize = size * Float.SIZE / 8;
        
        ByteBuffer b = ByteBuffer.allocateDirect(bufSize);
        b.order(ByteOrder.nativeOrder());

        return b.asFloatBuffer(); 
    }
    
    static final public IntBuffer allocateIntBuffer(int size) {
        final int bufSize = size * Integer.SIZE / 8;
        
        ByteBuffer b = ByteBuffer.allocateDirect(bufSize);
        b.order(ByteOrder.nativeOrder());

        return b.asIntBuffer(); 
    }
    
    static final public ShortBuffer allocateShortBuffer(int size) {
        final int bufSize = size * Short.SIZE / 8;
        
        ByteBuffer b = ByteBuffer.allocateDirect(bufSize);
        b.order(ByteOrder.nativeOrder());

        return b.asShortBuffer(); 
    }
}