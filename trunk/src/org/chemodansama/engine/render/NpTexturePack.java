package org.chemodansama.engine.render;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.util.Pair;

/**
 * TexturePack - generic texture pack for gl-android-application. 
 *
 */
public class NpTexturePack implements Iterable<NpTexture> {
    // clamp to edge is set by default.
    private final static boolean CLAMP_TO_EDGE = true;
    
    // <Alias, <Filename, Texture>>
    private final TreeMap<String, Pair<String, NpTexture>> mTextures; 

    public NpTexturePack() {
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
        
        NpTexture texture = new NpTexture(gl, file, assets, CLAMP_TO_EDGE);
        
        mTextures.put(alias, 
                      new Pair<String, NpTexture>(file, texture));
        
        return true;
    }
    
    public boolean refreshContextAssets(GL10 gl, AssetManager assets) {
        if ((assets == null) || (gl == null)) {
            return false;
        }
        
        for (Pair<String, NpTexture> p : mTextures.values()) {
            try {
                p.second.refreshContextAssets(gl, assets);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return true;
    }
    
    public void release(GL10 gl) {
        for (Pair<String, NpTexture> t : mTextures.values()) {
            t.second.releaseAssets(gl);
        }
        mTextures.clear();
    }

    @Override
    public Iterator<NpTexture> iterator() {
        final Iterator<Pair<String, NpTexture>> values = 
                mTextures.values().iterator();
        
        return new Iterator<NpTexture>() {
            @Override
            public void remove() {
                values.remove();
            }
            
            @Override
            public NpTexture next() {
                return values.next().second;
            }
            
            @Override
            public boolean hasNext() {
                return values.hasNext();
            }
        };
        
    }
}