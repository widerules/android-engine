package org.chemodansama.engine.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.NpGlContextAsset;
import org.chemodansama.engine.utils.NpByteBuffer;

import android.content.res.AssetManager;

final public class NpTexture implements NpGlContextAsset {
    
    private int mTextureID = 0;
    
    private final String fileName;
    private final boolean clampToEdge;

    private int mWidth = 0;
    private int mHeight = 0;
    
    public static void unbind(GL10 gl) {
        if (gl == null) {
            return;
        }
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
    }
    
    private static NpTextureData loadTextureData(String fileName, 
            AssetManager assets) throws IOException {
        InputStream in = null;
        try {
            in = assets.open(fileName);

            return (fileName.endsWith(".pvr")) ? new NpPvrTextureData(in) 
                                               : new NpTextureData(in);
        } finally {
            if (in != null) {
                in.close();
            } 
        }
    }
    
    public NpTexture(GL10 gl, String fileName, AssetManager assets, 
            boolean clampToEdge) throws IOException {
        
        if (assets == null) {
            throw new IllegalArgumentException("assets == null");
        }
        
        if (fileName == null) {
            throw new IllegalArgumentException("fileName == null");
        }
        
        this.fileName = fileName;
        this.clampToEdge = clampToEdge;
        
        NpTextureData data = loadTextureData(fileName, assets);
        mTextureID = data.initGL10(gl, clampToEdge);
        mWidth = data.getHeader().getWidth();
        mHeight = data.getHeader().getHeight();
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
    
    public int getWidth() {
        return mWidth;
    }
    
    public int getHeight() {
        return mHeight;
    }
    
    public int getTextureID() {
        return mTextureID;
    }
       
    @Override
    public void refreshContextAssets(GL10 gl, AssetManager assets)
            throws IOException {
        if (gl == null) {
            throw new IllegalArgumentException("gl == null");
        }

        if (assets == null) {
            throw new IllegalArgumentException("assets == null");
        }
        
        NpTextureData data = loadTextureData(fileName, assets);
        mTextureID = data.initGL10(gl, clampToEdge);
        mWidth = data.getHeader().getWidth();
        mHeight = data.getHeader().getHeight();
    }

    @Override
    public void releaseAssets(GL10 gl) {
        if (mTextureID != 0) {
            IntBuffer t = NpByteBuffer.allocateDirectNativeInt(1);
            t.put(mTextureID);
            t.rewind();

            gl.glDeleteTextures(1, t);
            mTextureID = 0;
        }
    }
}
