package org.chemodansama.engine.tmx.render;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Comparator;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.NpObjectsPool;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.utils.NpByteBuffer;

class RenderOpComparator implements Comparator<TmxRenderOp> {
    
    @Override
    public int compare(TmxRenderOp rop1, TmxRenderOp rop2) {
        
        int r = rop1.plane - rop2.plane;

        if (r != 0) {
            return r;
        } 

        if (rop1.isShadow && !rop2.isShadow) {
            return -1;
        }
        
        if (!rop1.isShadow && rop2.isShadow) {
            return 1;
        }
        
        return rop1.y - rop2.y;
    }
}

class TmxRenderOp {
    public float[] texCoords;
    public float[] vertices;
    public NpTexture texture;
    public int y;
    public int plane;
    public boolean isShadow;

//    boolean overlaps(TmxRenderOp rop) {
//        float x = (vertices[0 * 2 + 0] + vertices[2 * 2 + 0]) / 2;
//        float y = (vertices[0 * 2 + 1] + vertices[2 * 2 + 1]) / 2;
//        
//        float ex = Math.abs((vertices[0 * 2 + 0] - vertices[2 * 2 + 0]) / 2);
//        float ey = Math.abs((vertices[0 * 2 + 1] - vertices[2 * 2 + 1]) / 2);
//        
//        float rx = (rop.vertices[0 * 2 + 0] + rop.vertices[2 * 2 + 0]) / 2;
//        float ry = (rop.vertices[0 * 2 + 1] + rop.vertices[2 * 2 + 1]) / 2;
//        
//        float rex = Math.abs((rop.vertices[0 * 2 + 0] - rop.vertices[2 * 2 + 0]) / 2);
//        float rey = Math.abs((rop.vertices[0 * 2 + 1] - rop.vertices[2 * 2 + 1]) / 2);
//        
//        return ((Math.abs(x - rx) < ex + rex) && (Math.abs(y - ry) < ey + rey));
//    }
}

public class TmxRenderQueue {
    
    private final int mSize;
    
    private final RenderOpComparator mComparator;
    
    private int mCount = 0;
    private final ShortBuffer mIndicesBuffer;
    
    // renderOps pool.
    private final TmxRenderOp[] mRenderOps; 
    private final short[] mIndices;
    
    private final FloatBuffer mTexCoords;
    private final FloatBuffer mVertices;
   
    private final NpObjectsPool<TmxRenderOp> mRenderOpsPool;
    
    /**
     * @param size must be > 0
     */
    public TmxRenderQueue(int size) {
        
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        
        mSize = size;
        
        mRenderOpsPool = new NpObjectsPool<TmxRenderOp>(size, true) {
            @Override
            protected TmxRenderOp create() {
                return new TmxRenderOp();
            }
        };
        
        mVertices  = NpByteBuffer.allocateDirectNativeFloat(size * 4 * 2);
        mTexCoords = NpByteBuffer.allocateDirectNativeFloat(size * 4 * 2);
        mIndicesBuffer = NpByteBuffer.allocateDirectNativeShort(size * 6);
        mIndices   = new short[size * 6]; 
        
        mRenderOps = new TmxRenderOp[size];
        for (int i = 0; i < size; i++) {
            mRenderOps[i] = new TmxRenderOp();
        }
        
        mComparator = new RenderOpComparator();
    }
    
    public void addRenderOp(GL10 gl, TmxRenderOp rop) {
        mRenderOps[mCount] = rop;
        mCount++;
        if (mCount >= mSize) {
            flush(gl);
            LogHelper.e("FLUSH!");
        }
    }
    
    public void addRenderOp(GL10 gl, int y, float[] vertices, 
            NpTexture texture, float[] textureCoords, int plane, 
            boolean isShadow) {
        
        TmxRenderOp rop = mRenderOpsPool.getNext();

        rop.vertices  = vertices;
        rop.texture   = texture;
        rop.texCoords = textureCoords;
        rop.y         = y;
        rop.plane     = plane;
        rop.isShadow  = isShadow;
        
        addRenderOp(gl, rop);
    }
    
    public void finishRender(GL10 gl) {
        flush(gl);
        tearDownGLStates(gl);
    }
    
    public void flush(GL10 gl) {
        
        if (mCount == 0) {
            return;
        }
        
        sortOps();
        
        render(gl);
        
        mRenderOpsPool.rewind();
        
        mCount = 0;
    }

    public void prepareRender(GL10 gl) {
        setupGLStates(gl);
    }
    
    private void present(GL10 gl, TmxRenderOp rop, int quadsCount) {

        if (gl == null) {
            return;
        }
        
        if (rop == null) {
            return;
        }
        
        if (quadsCount == 0) {
            return;
        }
        
        mVertices.position(0);
        mTexCoords.position(0);
        
        mIndicesBuffer.put(mIndices);
        mIndicesBuffer.position(0);
        
        gl.glDrawElements(GL10.GL_TRIANGLES, quadsCount * 6, 
                          GL10.GL_UNSIGNED_SHORT, mIndicesBuffer);
       
        return;
    }
    
    private static void setupRenderOp(GL10 gl, TmxRenderOp rop, 
            boolean setupTex, boolean setupBlend) {
        if (setupTex) {
            if (rop.texture != null) {
                rop.texture.bindGL10(gl);
            } else {
                NpTexture.unbind(gl);
            }
        }

        if (setupBlend) {
            if (rop.isShadow) {
                gl.glBlendFunc(GL10.GL_DST_COLOR, GL10.GL_ZERO);
            } else {
                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }
    
    private void render(GL10 gl) {
        
        TmxRenderOp prevOp = null;
        boolean isRenderOpBound = false;
        int quadsCount = 0;
        
        for (int i = 0; i < mCount; i++) {
            TmxRenderOp rop = mRenderOps[i];
            
            if (prevOp != null) {
                boolean needTexChange = (prevOp.texture != rop.texture);
                boolean needBlendChange = (prevOp.isShadow != rop.isShadow);
                
                if (needBlendChange || needTexChange) {
                    
                    if (!isRenderOpBound) {
                        setupRenderOp(gl, prevOp, true, true);
                        isRenderOpBound = true;
                    }
                    
                    present(gl, prevOp, quadsCount);
                    
                    setupRenderOp(gl, rop, needTexChange, needBlendChange);
                    
                    quadsCount = 0;
                }
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
        
        present(gl, prevOp, quadsCount);
        
        if ((prevOp != null) && (prevOp.isShadow)) {
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        }
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
        Arrays.sort(mRenderOps, 0, mCount, mComparator);
    }
    
    public int size() {
        return mCount;
    }

    private void tearDownGLStates(GL10 gl) {
        
        if (gl == null) {
            LogHelper.e("Cant tear down GL states for WireLevel: gl is null");
            return;
        }

        gl.glDisable(GL10.GL_TEXTURE_2D);
    }
}
