package org.chemodansama.engine;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;

public interface NpGlContextAsset {
    public abstract void refreshContextAssets(GL10 gl, AssetManager assets) 
            throws IOException;
    public abstract void releaseAssets(GL10 gl);
}
