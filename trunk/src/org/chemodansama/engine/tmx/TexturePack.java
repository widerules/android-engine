package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.render.NpPvrTextureData;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.render.NpTextureData;

import android.content.res.AssetManager;
import android.util.Pair;

/**
 * TexturePack - generic texture pack for gl-android-application. 
 *
 */
public class TexturePack {
    // clamp to edge is set by default.
    private final static boolean CLAMP_TO_EDGE = true;
    
    // <Alias, <Filename, Texture>>
    private final TreeMap<String, Pair<String, NpTexture>> mTextures; 

    public TexturePack() {
        mTextures = new TreeMap<String, Pair<String, NpTexture>>();
    } 
    
    public boolean isEmpty() {
        return mTextures.isEmpty();
    }
    
    public NpTexture get(String name) {
        if ((name == null) || (name.equals(""))) {
            return null;
        }
        
        Pair<String, NpTexture> p = mTextures.get(name);

        return (p != null) ? p.second : null;
    }
    
    public boolean put(GL10 gl, String alias, String file, AssetManager assets) 
            throws IOException {
        
        if ((alias == null) || (alias.equals(""))) {
            return false;
        }
        
        if ((file == null) || (file.equals(""))) {
            return false;
        }
        
        if (assets == null) {
            return false;
        }
        
        if (mTextures.containsKey(alias)) {
            return true;
        }
        
        InputStream in = assets.open(file);

        NpTextureData td = null;
        if (file.endsWith("pvr")) {
            td = new NpPvrTextureData(in);
        } else {
            td = new NpTextureData(in);
        }
        
        NpTexture texture = new NpTexture(gl, td, CLAMP_TO_EDGE);
        
        mTextures.put(alias, 
                      new Pair<String, NpTexture>(file, texture));
        
        return true;
    }
    
    public boolean refreshContextAssets(GL10 gl, AssetManager assets) {
        if ((assets == null) || (gl == null)) {
            return false;
        }
        
        for (Pair<String, NpTexture> p : mTextures.values()) {
            InputStream in;
            try {
                in = assets.open(p.first);
                p.second.reloadOnSurfaceCreated(gl, in, CLAMP_TO_EDGE);
                in.close();
            } catch (IOException e) {
                LogHelper.e("cant reload texture " + p.first);
            }
        }
        
        return true;
    }
    
    public void release(GL10 gl) {
        for (Pair<String, NpTexture> t : mTextures.values()) {
            t.second.release(gl);
        }
        mTextures.clear();
    }
}