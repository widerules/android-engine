package org.chemodansama.engine.render.imgui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

class NpPolyBuffer {
    
    static final private int QUADS_LIMIT = 16;
    
    private float[] mVertArray = new float[QUADS_LIMIT * 4 * 3];
    private float[] mTexCoordArray = new float[QUADS_LIMIT * 4 * 2];
    private short[] mIndicesArray = new short[QUADS_LIMIT * 6];
    private short mQuadsCount = 0;
    
    private FloatBuffer mTexCoordBuffer;
    private FloatBuffer mVertBuffer;
    private ShortBuffer mIndices;
    
    NpPolyBuffer() {
        ByteBuffer vbb = ByteBuffer.allocateDirect(mVertArray.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertBuffer = vbb.asFloatBuffer();

        vbb = ByteBuffer.allocateDirect(mTexCoordArray.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mTexCoordBuffer = vbb.asFloatBuffer();
        
        vbb = ByteBuffer.allocateDirect(mIndicesArray.length * 2);
        vbb.order(ByteOrder.nativeOrder());
        mIndices = vbb.asShortBuffer();
    }
    
    void flushRender(GL10 gl) {
        if (mQuadsCount > 0) {
            mTexCoordBuffer.put(mTexCoordArray);
            mTexCoordBuffer.position(0);

            mVertBuffer.put(mVertArray);
            mVertBuffer.position(0);
            
            mIndices.put(mIndicesArray);
            mIndices.position(0);

            // assume corresponding arrays are enabled in gl-state
            
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordBuffer);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertBuffer);

            gl.glDrawElements(GL10.GL_TRIANGLES, mQuadsCount * 6, 
                              GL10.GL_UNSIGNED_SHORT, mIndices);

            mQuadsCount = 0;
        }
    }
    
    void pushQuad(GL10 gl, float x, float y, float w, float h, 
            float tx, float ty, float tw, float th) {
       
        int offs = mQuadsCount * 4 * 3; 
       
        mVertArray[offs + 0] = x;
        mVertArray[offs + 1] = y;
        mVertArray[offs + 2] = 0.0f;
        offs += 3;
        
        mVertArray[offs + 0] = x + w;
        mVertArray[offs + 1] = y;
        mVertArray[offs + 2] = 0.0f;
        offs += 3;
        
        mVertArray[offs + 0] = x + w;
        mVertArray[offs + 1] = y + h;
        mVertArray[offs + 2] = 0.0f;
        offs += 3;
        
        mVertArray[offs + 0] = x;
        mVertArray[offs + 1] = y + h;
        mVertArray[offs + 2] = 0.0f;
        
        
        offs = mQuadsCount * 4 * 2;
        
        mTexCoordArray[offs + 0] = tx;
        mTexCoordArray[offs + 1] = ty;
        offs += 2;
        
        mTexCoordArray[offs + 0] = tx + tw;
        mTexCoordArray[offs + 1] = ty;
        offs += 2;
        
        mTexCoordArray[offs + 0] = tx + tw;
        mTexCoordArray[offs + 1] = ty + th;
        offs += 2;
        
        mTexCoordArray[offs + 0] = tx;
        mTexCoordArray[offs + 1] = ty + th;
        
        
        offs = mQuadsCount * 6;
        
        int i = mQuadsCount * 4;
        
        mIndicesArray[offs + 0] = (short) i;
        mIndicesArray[offs + 1] = (short) (i + 1);
        mIndicesArray[offs + 2] = (short) (i + 2);
        
        mIndicesArray[offs + 3] = (short) i;
        mIndicesArray[offs + 4] = (short) (i + 2);
        mIndicesArray[offs + 5] = (short) (i + 3);
        
        mQuadsCount++;
        
        if (mQuadsCount >= QUADS_LIMIT) {
            flushRender(gl);
        }
    }
}
