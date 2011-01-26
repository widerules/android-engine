package org.chemodansama.engine.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.utils.NpEndianness;

public class NpTexture {
    
    private int mTextureID = 0;
    private boolean mDataValid = false; // valid Header, Mips and MipsSize
    
    private NpTextureHeader mHeader = new NpTextureHeader();
    
    private ArrayList<ByteBuffer> mMips = new ArrayList<ByteBuffer>();
    private ArrayList<Integer> mMipSize = new ArrayList<Integer>(); 
    
    public NpTexture() {
        super();
    }
    
    public boolean bindGL10(GL10 gl) {
        if (mTextureID > 0) {
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
        
        boolean r = false;

        DataInputStream din = new DataInputStream(in);
        
        if (din != null) {
        
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
            
                    r = true;
                } catch (IOException e) {
                    r = false;
                }
                
            }
        }
        
        mDataValid = r;
        
        return r;
    }
    
    public boolean initGL10(GL10 gl) {
        
        boolean r = false;
        
        if (mDataValid && (mTextureID == 0) && (mHeader.getMipsCount() > 0)) {
            if (gl != null) {
                IntBuffer textureBuf = ByteBuffer.allocateDirect(4).asIntBuffer();
                gl.glGenTextures(1, textureBuf);
                
                if (mTextureID > 0) {
                    IntBuffer b = ByteBuffer.allocateDirect(1).asIntBuffer();
                    b.put(mTextureID);
                    b.position(0);
                    
                    gl.glDeleteTextures(1, b);
                }
                
                mTextureID = textureBuf.get(0);
                
                if (mTextureID > 0) {
                    
                    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
                    
                    gl.glTexImage2D(GL10.GL_TEXTURE_2D, 
                                    0, // level 
                                    mHeader.getInternalFormat(), 
                                    mHeader.getWidth(),
                                    mHeader.getHeight(), 
                                    0, // border 
                                    mHeader.getFormat(), 
                                    mHeader.getType(), 
                                    mMips.get(0));
                    
                    gl.glTexParameterf(GL10.GL_TEXTURE_2D, 
                                       GL10.GL_TEXTURE_MAG_FILTER, 
                                       mHeader.getMagFilter());
                    
                    gl.glTexParameterf(GL10.GL_TEXTURE_2D, 
                                       GL10.GL_TEXTURE_MIN_FILTER, 
                                       mHeader.getMinFilter());
                    
                    r = true;
                } 
            }
        }
        
        return r;
    }
    
    public void releaseMips() {
        mMips.clear();
        mMipSize.clear();
        
        mDataValid = false;
    }
    
}
