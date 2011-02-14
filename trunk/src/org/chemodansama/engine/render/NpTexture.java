package org.chemodansama.engine.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.utils.NpEndianness;

import android.util.Log;

final public class NpTexture implements LogTag {
    
    private int mTextureID = 0;
    private boolean mDataValid = false; // valid Header, Mips and MipsSize
    
    private NpTextureHeader mHeader = new NpTextureHeader();
    
    private ArrayList<ByteBuffer> mMips = new ArrayList<ByteBuffer>();
    private ArrayList<Integer> mMipSize = new ArrayList<Integer>(); 
    
    public NpTexture() {
        super();
    }
    
    public boolean bindGL10(GL10 gl) {
        if (mTextureID != 0) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
            
            return true;
        }
        return false;
    }
    
    public NpTextureHeader getHeader() {
        return mHeader;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (o instanceof NpTexture) {
                return mTextureID == ((NpTexture) o).mTextureID; 
            } else {
                return super.equals(o);
            }
        } else {
            return super.equals(o);
        }
    }
    
    @Override
    public int hashCode() {
        return mTextureID;
    }
    
    public int getTextureID() {
        return mTextureID;
    }
    
    public boolean equalsToTexture(NpTexture t) {
        return (t != null) ? mTextureID == t.mTextureID : false;
    }
    
    public boolean initFromStream(InputStream in) {

        mDataValid = false;
        
        if (in == null) {
            return false;
        }
        
        DataInputStream din = new DataInputStream(in);
        
        if (mHeader.loadFromStream(din)) {

            try {
                for (int i = 0; i < mHeader.getMipsCount(); i++) {
                    mMipSize.add(NpEndianness.convertInt(din.readInt()));

                    ByteBuffer b = ByteBuffer.allocateDirect(mMipSize.get(i));

                    byte[] bb = new byte[mMipSize.get(i)];

                    din.read(bb, 0, mMipSize.get(i));

                    b.put(bb);
                    b.position(0);

                    mMips.add(b);
                }

                mDataValid = true;
                return true;
                
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public boolean initGL10(GL10 gl) {
        
        if (!mDataValid || (mTextureID != 0) || (mHeader.getMipsCount() <= 0) 
                || (gl == null)) {
            return false;
        }
        
        IntBuffer textureBuf = ByteBuffer.allocateDirect(4).asIntBuffer();

        gl.glGenTextures(1, textureBuf);

        mTextureID = textureBuf.get(0);

        if (mTextureID == 0) {
            Log.e(TAG, "TexID == 0");
            return false;
        }

        Log.i(TAG, "mTextureID == " + mTextureID);
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, 
                           GL10.GL_TEXTURE_MAG_FILTER, 
                           mHeader.getMagFilter());

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, 
                           GL10.GL_TEXTURE_MIN_FILTER, 
                           mHeader.getMinFilter());
        
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, 
                           GL10.GL_CLAMP_TO_EDGE);
        
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, 
                           GL10.GL_CLAMP_TO_EDGE);
        
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 
                        0, // level 
                        mHeader.getInternalFormat(), 
                        mHeader.getWidth(),
                        mHeader.getHeight(), 
                        0, // border 
                        mHeader.getFormat(), 
                        mHeader.getType(), 
                        mMips.get(0));

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        return true;
    }
    
    public void releaseMips() {
        mMips.clear();
        mMipSize.clear();
        
        mDataValid = false;
    }
    
}
