package org.chemodansama.engine.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogTag;

import android.util.Log;

final public class NpTexture {
    
    private int mTextureID = 0;
    
    private NpTextureHeader mHeader;
    
    public NpTexture(GL10 gl, InputStream in, boolean clampToEdge) {
        super();
        
        NpTextureData d = new NpTextureData(in);
        
        if (d.isDataValid()) {
            mHeader = d.getHeader();
            initGL10(gl, d, clampToEdge);
        } 
    }
    
    public void release(GL10 gl) {
        IntBuffer t = ByteBuffer.allocate(4).asIntBuffer();
        t.put(mTextureID);
        t.rewind();
        
        gl.glDeleteTextures(1, t);
        
        mTextureID = 0;
    }
    
    public boolean bindGL10(GL10 gl) {
        if (mTextureID != 0) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof NpTexture)) {
            return mTextureID == ((NpTexture) o).mTextureID; 
        } else {
            return super.equals(o);
        }
    }
    
    public boolean equalsToTexture(NpTexture t) {
        return (t != null) ? mTextureID == t.mTextureID : false;
    }
    
    public NpTextureHeader getHeader() {
        return mHeader;
    }
    
    public int getTextureID() {
        return mTextureID;
    }
    
    @Override
    public int hashCode() {
        return mTextureID;
    }
    
    public boolean initGL10(GL10 gl, NpTextureData texData, 
            boolean clampToEdge) {
        
        if ((texData == null) || !texData.isDataValid() || (mTextureID != 0) 
                || (mHeader.getMipsCount() <= 0) || (gl == null)) {
            return false;
        }
        
        IntBuffer textureBuf = ByteBuffer.allocateDirect(4).asIntBuffer();

        gl.glGenTextures(1, textureBuf);

        mTextureID = textureBuf.get(0);

        if (mTextureID == 0) {
            Log.e(LogTag.TAG, "Generated TextureID == 0");
            return false;
        }

        Log.i(LogTag.TAG, "mTextureID == " + mTextureID);
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, 
                           GL10.GL_TEXTURE_MAG_FILTER, 
                           mHeader.getMagFilter());

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, 
                           GL10.GL_TEXTURE_MIN_FILTER, 
                           mHeader.getMinFilter());
        
        int clamp = (clampToEdge) ? GL10.GL_CLAMP_TO_EDGE : GL10.GL_REPEAT;
        
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, clamp);
        
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, clamp);
        
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 
                        0, // level 
                        mHeader.getInternalFormat(), 
                        mHeader.getWidth(),
                        mHeader.getHeight(), 
                        0, // border 
                        mHeader.getFormat(), 
                        mHeader.getType(), 
                        texData.getMips().get(0));

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        return true;
    }
}

final class NpTextureData {
    
    private boolean mDataValid = false; // valid Header, Mips and MipsSize
    
    private NpTextureHeader mHeader = new NpTextureHeader();
    private ArrayList<ByteBuffer> mMips = new ArrayList<ByteBuffer>();
    private ArrayList<Integer> mMipSize = new ArrayList<Integer>(); 
    
    public NpTextureData(InputStream in) {
        mDataValid = false;
        
        if (in == null) {
            Log.w(LogTag.TAG, "cant load texture data: input stream is null");
            return;
        }
        
        DataInputStream din = new DataInputStream(in);
        
        if (mHeader.loadFromStream(din)) {

            try {
                for (int i = 0; i < mHeader.getMipsCount(); i++) {
                    mMipSize.add(Integer.reverseBytes(din.readInt()));

                    ByteBuffer b = ByteBuffer.allocateDirect(mMipSize.get(i));

                    byte[] bb = new byte[mMipSize.get(i)];

                    din.read(bb, 0, mMipSize.get(i));

                    b.put(bb);
                    b.position(0);

                    mMips.add(b);
                }

                mDataValid = true;
                return;
                
            } catch (IOException e) {
                Log.e(LogTag.TAG, "IOException while reading texture", e);
                return;
            }
        } else {
            return;
        }
    }

    public NpTextureHeader getHeader() {
        return mHeader;
    }

    public ArrayList<ByteBuffer> getMips() {
        return mMips;
    }

    public ArrayList<Integer> getMipSize() {
        return mMipSize;
    }

    public boolean isDataValid() {
        return mDataValid;
    }
}
