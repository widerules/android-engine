package org.chemodansama.engine.render;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.utils.NpByteBuffer;

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
        
        mVertArray = new float[mQuadsLimit * 4 * 2];
        mTexCoordArray = new float[mQuadsLimit * 4 * 2];
        mIndicesArray = new short[mQuadsLimit * 6];
        
        mVertBuffer = NpByteBuffer.allocateDirectNativeFloat(mVertArray.length);
        
        mTexCoordBuffer = 
                NpByteBuffer.allocateDirectNativeFloat(mTexCoordArray.length);
        
        mIndices = NpByteBuffer.allocateDirectNativeShort(mIndicesArray.length);
    }
    
    public void flushRender(GL10 gl) {
        if (mQuadsCount == 0) {
            return;
        }
        
        mTexCoordBuffer.put(mTexCoordArray);
        mTexCoordBuffer.position(0);

        mVertBuffer.put(mVertArray);
        mVertBuffer.position(0);

        mIndices.put(mIndicesArray);
        mIndices.position(0);

        // assume corresponding arrays are enabled in gl-state

        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordBuffer);

        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertBuffer);

        gl.glDrawElements(GL10.GL_TRIANGLES, mQuadsCount * 6, 
                          GL10.GL_UNSIGNED_SHORT, mIndices);

        mQuadsCount = 0;
    }
    
    public void pushQuad(GL10 gl, float[] vertices, int verticesOffset, 
            float[] texcoords, int texcoordsOffset) {
        if ((vertices == null) || (vertices.length - verticesOffset < 8)) {
            LogHelper.e("invalid vertices");
            return;
        }
        
        if ((texcoords == null) || (texcoords.length - texcoordsOffset < 8)) {
            LogHelper.e("invalid texcoords");
            return;
        }
        
        int offs = mQuadsCount * 4 * 2; 
        
        System.arraycopy(vertices, verticesOffset, mVertArray, offs, 8);
        System.arraycopy(texcoords, texcoordsOffset, mTexCoordArray, offs, 8);
        
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
    
    public void pushQuad(GL10 gl, float x1, float y1, float x2, float y2,  
            float tx1, float ty1, float tx2, float ty2) {
       
        int offs = mQuadsCount * 4 * 2; 
       
        mVertArray[offs + 0] = x1;
        mVertArray[offs + 1] = y1;
        offs += 2;
        
        mVertArray[offs + 0] = x2;
        mVertArray[offs + 1] = y1;
        offs += 2;
        
        mVertArray[offs + 0] = x2;
        mVertArray[offs + 1] = y2;
        offs += 2;
        
        mVertArray[offs + 0] = x1;
        mVertArray[offs + 1] = y2;
        
        
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
