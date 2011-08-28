package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.render.NpTexture;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Pair;
import android.util.Xml;
import android.util.Xml.Encoding;

class TextureHolder {
    // clamp to edge is set by default.
    private final static boolean CLAMP_TO_EDGE = true;
    
    private final TreeMap<String, NpTexture> mTextures; // <Alias, Texture>
    // These two must be kept in sync. It is one the task of the class.
    private final ArrayList<Pair<String, String>> mTextureSource; // <Alias, FileName>

    public TextureHolder() {
        mTextures = new TreeMap<String, NpTexture>();
        mTextureSource = new ArrayList<Pair<String, String>>();
    } 
    
    public boolean isEmpty() {
        return mTextures.isEmpty() && mTextureSource.isEmpty();
    }
    
    public NpTexture get(String name) {
        if ((name == null) || (name.equals(""))) {
            return null;
        }
        
        return mTextures.get(name);
    }
    
    boolean put(GL10 gl, String alias, String file, AssetManager assets) 
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
        
        mTextures.put(alias, new NpTexture(gl, in, CLAMP_TO_EDGE));
        mTextureSource.add(new Pair<String, String>(alias, file));
        
        return true;
    }
    
    public boolean refreshTextures(GL10 gl, AssetManager assets) 
            throws IOException {
        
        if ((assets == null) || (gl == null)) {
            return false;
        }
        
        releaseTextures(gl);
        
        for (Pair<String, String> p : mTextureSource) {
            InputStream in = assets.open(p.second);
            mTextures.put(p.first, new NpTexture(gl, in, CLAMP_TO_EDGE));        
        }
        
        return true;
    }
    
    public void release(GL10 gl) {
        releaseTextures(gl);
        mTextureSource.clear();
    }
    
    private void releaseTextures(GL10 gl) {
        for (NpTexture t : mTextures.values()) {
            t.release(gl);
        }
        mTextures.clear();
    }
}

public class TmxTexturePack {
    
    private class TexturePackHandler extends DefaultHandler {
        
        private final AssetManager mAssets;
        private final GL10 mGL;

        public TexturePackHandler(GL10 gl, AssetManager assets) {
            mAssets = assets;
            mGL = gl;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            
            if ((qName == null) || (qName.equalsIgnoreCase(""))) {
                return;
            }

            if (qName.equalsIgnoreCase("image")) {
                String alias = attributes.getValue("alias");
                
                if ((alias == null) || (alias.equalsIgnoreCase(""))) {
                    return;
                }
                
                String texture = attributes.getValue("texture");
                
                if ((texture == null) || (texture.equalsIgnoreCase(""))) {
                    return;
                }
                
                boolean failed = false;
                
                try {
                    failed = !mTextureHolder.put(mGL, alias, texture, mAssets);
                } catch (IllegalArgumentException e) {
                    failed = true;
                } catch (IOException e) {
                    failed = true;
                }
                
                if (failed) {
                    LogHelper.e("Can't instantiate texture '" + texture + "'");
                    return;
                }
                
                LogHelper.i("texture '" + texture + "' added as '" 
                            + alias + "'");
            }
        }
    }
    
    private final TextureHolder mTextureHolder = new TextureHolder();
    
    public TmxTexturePack() {
    }
    
    public TmxTexturePack(GL10 gl, AssetManager assets, String fileName) {
        this();
        addTextures(gl, assets, fileName);
    }
    
    public boolean addTextures(GL10 gl, AssetManager assets, String fileName) {
        try {
            InputStream in = assets.open(fileName);
            DefaultHandler handler = new TexturePackHandler(gl, assets);
            Xml.parse(in, Encoding.US_ASCII, handler);
        } catch (SAXException e) {
            LogHelper.e("SAXException while parsing '" + fileName + "'");
            return false;
        } catch (IOException e) {
            LogHelper.e("IOException with file '" + fileName + "'");
            return false;
        }
        return true;
    }
    
    public NpTexture getTexture(String name) {
        if ((name == null) || (name.equals(""))) {
            return null;
        }
        
        return mTextureHolder.get(name);
    }

    @Override
    protected void finalize() throws Throwable {
        if (!mTextureHolder.isEmpty()) {
            LogHelper.e("texture pack was NOT released!");
        }
        super.finalize();
    }
    
    public boolean refreshTextures(GL10 gl, AssetManager assets) 
            throws IOException {
        return mTextureHolder.refreshTextures(gl, assets);
    }
    
    public void release(GL10 gl) {
        mTextureHolder.release(gl);
    }
}
