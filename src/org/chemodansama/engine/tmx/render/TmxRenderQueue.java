package org.chemodansama.engine.tmx.render;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Comparator;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.utils.NpByteBuffer;

class RenderOpComparator implements Comparator<Integer> {
    
    private final TmxRenderOp[] mRenderOps;
    
    public RenderOpComparator(TmxRenderOp[] renderOps) {
        mRenderOps = renderOps;
    }
    
    @Override
    public int compare(Integer object1, Integer object2) {
        
        int id1 = object1;
        int id2 = object2;
        
        TmxRenderOp rop1 = mRenderOps[id1];
        TmxRenderOp rop2 = mRenderOps[id2];
        
        return (int) Math.signum(rop1.y - rop2.y);
    }
}

class TmxRenderOp {
    public float[] texCoords;
    public float[] vertices;
    public NpTexture texture;
    public float y;
}

public class TmxRenderQueue {
    
    public final static int SIZE = 128;
    
    private final RenderOpComparator mComparator;
    
    private int mCount = 0;
    private final ShortBuffer mIndicesBuffer;
    
    private final Integer[] mIntegersPool;
    
    // renderOps pool.
    private final TmxRenderOp[] mRenderOps; 
    private final Integer[] mRenderOpsOrder;
    private final short[] mIndices;
    
    private final FloatBuffer mTexCoords;
    private final FloatBuffer mVertices;
   
    /**
     * Defines whether queue must sort render ops vertically or not.
     */
    public boolean sortOps = false;
    
    public TmxRenderQueue() {
        mVertices  = NpByteBuffer.allocateDirectNativeFloat(SIZE * 4 * 2);
        mTexCoords = NpByteBuffer.allocateDirectNativeFloat(SIZE * 4 * 2);
        mIndicesBuffer = NpByteBuffer.allocateDirectNativeShort(SIZE * 6);
        mIndices   = new short[SIZE * 6]; 
        
        mRenderOps = new TmxRenderOp[SIZE];
        mIntegersPool = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) {
            mRenderOps[i] = new TmxRenderOp();
            mIntegersPool[i] = new Integer(i);
        }
        
        mComparator = new RenderOpComparator(mRenderOps);
        mRenderOpsOrder = new Integer[SIZE];
    }
    
    public void addRenderOp(GL10 gl, float y, float[] vertices, 
            NpTexture texture, float[] textureCoords) {
        
        TmxRenderOp rop = mRenderOps[mCount];

        rop.vertices  = vertices;
        rop.texture   = texture;
        rop.texCoords = textureCoords;
        rop.y         = y;
        
        mRenderOpsOrder[mCount] = mIntegersPool[mCount];
        
        mCount++;
        
        if (mCount >= SIZE) {
            flush(gl);
        }
    }
    
    public void finishRender(GL10 gl) {
        flush(gl);
        tearDownGLStates(gl);
    }
    
    public void flush(GL10 gl) {
        
        if (mCount == 0) {
            return;
        }
        
        if (sortOps) {
            sortOps();
        }
        
        render(gl);
        
        mCount = 0;
    }

    public void prepareRender(GL10 gl) {
        setupGLStates(gl);
    }
    
    private NpTexture present(GL10 gl, TmxRenderOp rop, NpTexture boundTexture,
            int quadsCount) {

        NpTexture ret = boundTexture;
        
        if (quadsCount == 0) {
            return ret;
        }
        
        if ((ret == null) || (ret != rop.texture)) {
            ret = rop.texture;
            if (ret != null) {
                ret.bindGL10(gl);
            } else {
                NpTexture.unbind(gl);
            }
        }
        
        mVertices.position(0);
        mTexCoords.position(0);
        
        mIndicesBuffer.put(mIndices);
        mIndicesBuffer.position(0);
        
        gl.glDrawElements(GL10.GL_TRIANGLES, quadsCount * 6, 
                          GL10.GL_UNSIGNED_SHORT, mIndicesBuffer);
       
        return ret;
    }
    
    private void render(GL10 gl) {
        
        TmxRenderOp prevOp = null;
        NpTexture boundTexture = null;
        int quadsCount = 0;
        
        for (int i = 0; i < mCount; i++) {
            TmxRenderOp rop = mRenderOps[mRenderOpsOrder[i]];
            
            if ((prevOp != null) && (prevOp.texture != rop.texture)) {
                boundTexture = present(gl, prevOp, boundTexture, quadsCount);
                quadsCount = 0;
            }
            
            mVertices.put(rop.vertices);
            mTexCoords.put(rop.texCoords);
            
            int j = quadsCount * 4;
            int offs = quadsCount * 6;
            
            mIndices[offs + 0] = (short) j;
            mIndices[offs + 1] = (short) (j + 1);
            mIndices[offs + 2] = (short) (j + 2);
            
            mIndices[offs + 3] = (short) j;
            mIndices[offs + 4] = (short) (j + 2);
            mIndices[offs + 5] = (short) (j + 3);
            
            prevOp = rop;
            quadsCount++;
        }
        
        present(gl, prevOp, boundTexture, quadsCount);
    }
    
    private boolean setupGLStates(GL10 gl) {
        if (gl == null) {
            LogHelper.e("Cant setup GL states for WireLevel: gl is null");
            return false;
        }

        gl.glEnable(GL10.GL_TEXTURE_2D);

        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoords);
        gl.glVertexPointer  (2, GL10.GL_FLOAT, 0, mVertices);
        
        gl.glColor4f(1, 1, 1, 1);
        
        return true;
    }
    
    private void sortOps() {
        Arrays.sort(mRenderOpsOrder, 0, mCount, mComparator);
    }

    private void tearDownGLStates(GL10 gl) {
        
        if (gl == null) {
            LogHelper.e("Cant tear down GL states for WireLevel: gl is null");
            return;
        }

        gl.glDisable(GL10.GL_TEXTURE_2D);
    }
}
