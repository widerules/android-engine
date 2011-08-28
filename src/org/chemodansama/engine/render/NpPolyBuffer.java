package org.chemodansama.engine.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

final public class NpPolyBuffer {
    
    private short mQuadsCount = 0;
    final private int mQuadsLimit;
    
    final private float[] mVertArray;
    final private float[] mTexCoordArray;
    final private short[] mIndicesArray;
    
    final private FloatBuffer mTexCoordBuffer;
    final private FloatBuffer mVertBuffer;
    final private ShortBuffer mIndices;
    
    public NpPolyBuffer(int quadsLimit) {
        
        mQuadsLimit = quadsLimit;
        
        mVertArray = new float[mQuadsLimit * 4 * 3];
        mTexCoordArray = new float[mQuadsLimit * 4 * 2];
        mIndicesArray = new short[mQuadsLimit * 6];
        
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
    
    public void flushRender(GL10 gl) {
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
    
    public void pushQuad(GL10 gl, float x1, float y1, float x2, float y2,  
            float tx1, float ty1, float tx2, float ty2) {
       
        int offs = mQuadsCount * 4 * 3; 
       
        mVertArray[offs + 0] = x1;
        mVertArray[offs + 1] = y1;
        mVertArray[offs + 2] = 0.0f;
        offs += 3;
        
        mVertArray[offs + 0] = x2;
        mVertArray[offs + 1] = y1;
        mVertArray[offs + 2] = 0.0f;
        offs += 3;
        
        mVertArray[offs + 0] = x2;
        mVertArray[offs + 1] = y2;
        mVertArray[offs + 2] = 0.0f;
        offs += 3;
        
        mVertArray[offs + 0] = x1;
        mVertArray[offs + 1] = y2;
        mVertArray[offs + 2] = 0.0f;
        
        
        offs = mQuadsCount * 4 * 2;
        
        mTexCoordArray[offs + 0] = tx1;
        mTexCoordArray[offs + 1] = ty1;
        offs += 2;
        
        mTexCoordArray[offs + 0] = tx2;
        mTexCoordArray[offs + 1] = ty1;
        offs += 2;
        
        mTexCoordArray[offs + 0] = tx2;
        mTexCoordArray[offs + 1] = ty2;
        offs += 2;
        
        mTexCoordArray[offs + 0] = tx1;
        mTexCoordArray[offs + 1] = ty2;
        
        offs = mQuadsCount * 6;
        
        int i = mQuadsCount * 4;
        
        mIndicesArray[offs + 0] = (short) i;
        mIndicesArray[offs + 1] = (short) (i + 1);
        mIndicesArray[offs + 2] = (short) (i + 2);
        
        mIndicesArray[offs + 3] = (short) i;
        mIndicesArray[offs + 4] = (short) (i + 2);
        mIndicesArray[offs + 5] = (short) (i + 3);
        
        mQuadsCount++;
        
        if (mQuadsCount >= mQuadsLimit) {
            flushRender(gl);
        }
    }
    
    public void pushQuadWH(GL10 gl, float x, float y, float w, float h, 
            float tx, float ty, float tw, float th) {
        pushQuad(gl, x, y, x + w, y + h, tx, ty, tx + tw, ty + th);
    }
}
