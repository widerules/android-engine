package org.chemodansama.engine.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.utils.NpByteBuffer;

import android.content.res.AssetManager;

final public class NpTexture {
    
    private final NpTextureData mData;
    private int mTextureID = 0;

    public static void unbind(GL10 gl) {
        if (gl == null) {
            return;
        }
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
    }
    
    public NpTexture(GL10 gl, String fileName, AssetManager assets, 
            boolean clampToEdge) throws IOException {
        
        if (assets == null) {
            throw new IllegalArgumentException("assets == null");
        }
        
        if (fileName == null) {
            throw new IllegalArgumentException("fileName == null");
        }
        
        InputStream in = null;
        try {
            in = assets.open(fileName);
            
            if (fileName.endsWith(".pvr")) {
                mData = new NpPvrTextureData(in);
            } else {
                mData = new NpTextureData(in);
            }
            mTextureID = mData.initGL10(gl, clampToEdge);
        } finally {
            if (in != null) {
                in.close();
            } 
        }
    }
    
    public NpTexture(GL10 gl, NpTextureData data, boolean clampToEdge) 
            throws IOException {
        
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        
        mData = data;
        mTextureID = mData.initGL10(gl, clampToEdge);
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
        if (mTextureID != 0) {
            LogHelper.e("Texture was not released!");
        }
        
        super.finalize();
    }
    
    public NpTextureHeader getHeader() {
        return (mData != null) ? mData.getHeader() : null;
    }
    
    public int getTextureID() {
        return mTextureID;
    }
       
    public void release(GL10 gl) {
        
        if (mTextureID != 0) {
            IntBuffer t = NpByteBuffer.allocateDirectNativeInt(1);
            t.put(mTextureID);
            t.rewind();

            gl.glDeleteTextures(1, t);
            mTextureID = 0;
        }
    }
    
    public void reloadOnSurfaceCreated(GL10 gl, InputStream in, 
            boolean clampToEdge) throws IOException {
        mData.loadData(in);
        mTextureID = mData.initGL10(gl, clampToEdge);
    }
}
