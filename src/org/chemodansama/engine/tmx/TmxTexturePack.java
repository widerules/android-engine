package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;
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

/**
 * TexturePack - generic texture pack for gl-android-application. 
 *
 */
class TexturePack {
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
        
        NpTexture texture = new NpTexture(gl, in, CLAMP_TO_EDGE);
        
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

/**
 * TmxTexturePack - wrapper for TexturePack. Provides input from xml file.
 */
public class TmxTexturePack {
    
    public static final String EXT = ".tex";
    
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
            
            if ((localName == null) || (localName.equalsIgnoreCase(""))) {
                return;
            }

            if (localName.equalsIgnoreCase("image")) {
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
    
    private final TexturePack mTextureHolder = new TexturePack();
    
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
    
    public boolean refreshContextAssets(GL10 gl, AssetManager assets) {
        return mTextureHolder.refreshContextAssets(gl, assets);
    }
    
    public void release(GL10 gl) {
        mTextureHolder.release(gl);
    }
}
