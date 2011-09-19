package org.chemodansama.engine.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.utils.NpByteBuffer;

import android.util.Log;

final public class NpTexture {
    private NpTextureHeader mHeader;
    private int mTextureID = 0;

    public static void unbind(GL10 gl) {
        if (gl == null) {
            return;
        }
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
    }
    
    public NpTexture(GL10 gl, InputStream in, boolean clampToEdge) 
            throws IOException {
        super();
        
        loadFromStream(gl, in, clampToEdge);
    }
    
    public boolean bindGL10(GL10 gl) {
        if (gl == null) {
            return false;
        }
        
        if (mTextureID == 0) {
            return false;
        }
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        return true;
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
    
    @Override
    protected void finalize() throws Throwable {
        if ((mTextureID != 0) || (mHeader != null)) {
            LogHelper.e("Texture was not released!");
        }
        
        super.finalize();
    }
    
    public NpTextureHeader getHeader() {
        return mHeader;
    }
    
    public int getTextureID() {
        return mTextureID;
    }
    
    private boolean initGL10(GL10 gl, NpTextureData texData, 
            boolean clampToEdge) {
        
        if (texData == null) {
            LogHelper.w("texData is null. Interrupting.");
            return false;
        }

        if (gl == null) {
            LogHelper.w("gl == null. Interrupting");
            return false;
        }
        
        if (mTextureID != 0) {
            LogHelper.w("textureID != 0. Interrupting.");
            return false;
        }

        NpTextureHeader header = texData.getHeader();
        
        if (header.getMipsCount() <= 0) {
            LogHelper.w("no mips in texture data. Interrupting.");
        }
        
        IntBuffer textureBuf = NpByteBuffer.allocateDirectNativeInt(1);

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
                           header.getMagFilter());

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, 
                           GL10.GL_TEXTURE_MIN_FILTER, 
                           header.getMinFilter());
        
        int clamp = (clampToEdge) ? GL10.GL_CLAMP_TO_EDGE : GL10.GL_REPEAT;
        
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, clamp);
        
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, clamp);
        
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 
                        0, // level 
                        header.getInternalFormat(), 
                        header.getWidth(),
                        header.getHeight(), 
                        0, // border 
                        header.getFormat(), 
                        header.getType(), 
                        texData.getMips().get(0));

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        mHeader = header;
        
        return true;
    }
    
    private void loadFromStream(GL10 gl, InputStream in, 
            boolean clampToEdge) throws IOException {
        if (gl == null) {
            throw new IllegalArgumentException("gl is null");
        }
        
        if (in == null) {
            throw new IllegalArgumentException("Input stream is null");
        }
        
        if (!initGL10(gl, new NpTextureData(in), clampToEdge)) {
            LogHelper.w("Texture was not initialized!" 
                        + " Texture is in Zombie state!");
        }
    }
    
    public void release(GL10 gl) {
        IntBuffer t = NpByteBuffer.allocateDirectNativeInt(1);
        t.put(mTextureID);
        t.rewind();
        
        gl.glDeleteTextures(1, t);
        
        mTextureID = 0;
        mHeader = null;
    }
    
    public void reloadOnSurfaceCreated(GL10 gl, InputStream in, 
            boolean clampToEdge) throws IOException {
        mTextureID = 0;
        mHeader = null;
        loadFromStream(gl, in, clampToEdge);
    }
}

final class NpTextureData {
    
    private final NpTextureHeader mHeader;
    private ArrayList<ByteBuffer> mMips = new ArrayList<ByteBuffer>();
    private ArrayList<Integer> mMipSize = new ArrayList<Integer>(); 
    
    public NpTextureData(InputStream in) throws IOException {
        if (in == null) {
            mHeader = null;
            throw new IOException("in == null");
        }
        
        DataInputStream din = new DataInputStream(in);
        mHeader = new NpTextureHeader(din);

        for (int i = 0; i < mHeader.getMipsCount(); i++) {
            mMipSize.add(Integer.reverseBytes(din.readInt()));

            ByteBuffer b = ByteBuffer.allocateDirect(mMipSize.get(i));

            byte[] bb = new byte[mMipSize.get(i)];

            din.read(bb, 0, mMipSize.get(i));

            b.put(bb);
            b.position(0);

            mMips.add(b);
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
}
