package org.chemodansama.engine.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class NpByteBuffer {
    /**
     * @param size specifies requested buffer size in floats
     * @return direct allocated IntBuffer instance with native order 
     */
    static final public FloatBuffer allocateDirectNativeFloat(int size) {
        final int bufSize = size * Float.SIZE / 8;
        
        ByteBuffer b = ByteBuffer.allocateDirect(bufSize);
        b.order(ByteOrder.nativeOrder());

        return b.asFloatBuffer(); 
    }
    
    /**
     * @param size specifies requested buffer size in ints
     * @return direct allocated IntBuffer instance with native order 
     */
    static final public IntBuffer allocateDirectNativeInt(int size) {
        final int bufSize = size * Integer.SIZE / 8;
        
        ByteBuffer b = ByteBuffer.allocateDirect(bufSize);
        b.order(ByteOrder.nativeOrder());

        return b.asIntBuffer(); 
    }
    
    /**
     * @param size specifies requested buffer size in shorts
     * @return direct allocated IntBuffer instance with native order 
     */
    static final public ShortBuffer allocateDirectNativeShort(int size) {
        final int bufSize = size * Short.SIZE / 8;
        
        ByteBuffer b = ByteBuffer.allocateDirect(bufSize);
        b.order(ByteOrder.nativeOrder());

        return b.asShortBuffer(); 
    }
}