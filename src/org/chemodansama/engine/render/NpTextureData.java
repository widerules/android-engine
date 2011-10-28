package org.chemodansama.engine.render;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.utils.NpByteBuffer;

public class NpTextureData {
    
    protected final NpTextureHeader mHeader;
    protected final ArrayList<ByteBuffer> mMips = new ArrayList<ByteBuffer>();
    
    public NpTextureData(InputStream in) throws IOException {
        mHeader = loadData(in, mMips); 
    }
    
    protected NpTextureData(NpTextureHeader header) {
        mHeader = header;
    }
    
    public NpTextureHeader getHeader() {
        return mHeader;
    }
    
    ByteBuffer getMip(int i) {
        return ((i >= 0) && (i < mMips.size())) ? mMips.get(i) : null;
    }
    
    Iterable<ByteBuffer> getMips() {
        return mMips;
    }
    
    void clearMipsData() {
        mMips.clear();
    }
    
    int initGL10(GL10 gl, boolean clampToEdge) {
        
        if (gl == null) {
            LogHelper.w("gl == null. Interrupting");
            return 0;
        }
        
        if (mHeader.getMipsCount() <= 0) {
            LogHelper.w("no mips in texture data. Interrupting.");
        }
        
        IntBuffer textureBuf = NpByteBuffer.allocateDirectNativeInt(1);

        gl.glGenTextures(1, textureBuf);

        int textureID = textureBuf.get(0);

        if (textureID == 0) {
            LogHelper.i("Generated TextureID == 0");
            return 0;
        }

        LogHelper.i("textureID == " + textureID);
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, 
                           mHeader.getMagFilter());
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, 
                           mHeader.getMinFilter());
        
        int clamp = (clampToEdge) ? GL10.GL_CLAMP_TO_EDGE : GL10.GL_REPEAT;
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, clamp);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, clamp);
        
        if (mHeader.isCompressedFormat()) {
            gl.glCompressedTexImage2D(mHeader.getTarget(), 0, 
                                      mHeader.getInternalFormat(), 
                                      mHeader.getWidth(), mHeader.getHeight(), 0, 
                                      mHeader.getMipSize(0), 
                                      mMips.get(0));
        } else {
            gl.glTexImage2D(GL10.GL_TEXTURE_2D, 
                            0, // level 
                            mHeader.getInternalFormat(), 
                            mHeader.getWidth(),
                            mHeader.getHeight(), 
                            0, // border 
                            mHeader.getFormat(), 
                            mHeader.getType(), 
                            mMips.get(0));
        }
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        return textureID;
    }
    
    public void loadData(InputStream in) throws IOException {
        loadData(in, mMips);
    }
    
    protected NpTextureHeader loadData(InputStream in, 
            ArrayList<ByteBuffer> mips) throws IOException {
        if (in == null) {
            throw new IOException("in == null");
        }
        
        DataInputStream din = new DataInputStream(in);
        NpTextureHeader header = new NpTextureHeader(din);

        if (header.getMipsCount() <= 0) {
            throw new RuntimeException("mipsCount <= 0");
        }
        
        byte[] bb = null;
        
        for (int i = 0; i < header.getMipsCount(); i++) {
            int mipSize = Integer.reverseBytes(din.readInt());
            
            if ((bb == null) || (bb.length < mipSize)) {
                bb = new byte[mipSize];
            }
            
            din.read(bb, 0, mipSize);

            ByteBuffer b = ByteBuffer.allocateDirect(mipSize);
            b.put(bb, 0, mipSize);
            b.position(0);
            mips.add(b);
        }
        
        return header;
    }
}