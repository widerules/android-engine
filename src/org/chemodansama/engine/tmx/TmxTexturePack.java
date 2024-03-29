package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.NpGlContextAsset;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.render.NpTexturePack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

/**
 * TmxTexturePack - wrapper for TexturePack. Provides input from xml file.
 */
public class TmxTexturePack implements NpGlContextAsset {
    
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
    
    private final NpTexturePack mTextureHolder = new NpTexturePack();
    
    public TmxTexturePack() {
    }
    
    public TmxTexturePack(GL10 gl, AssetManager assets, String fileName) {
        this();
        addTextures(gl, assets, fileName);
    }
    
    public boolean addTextures(GL10 gl, AssetManager assets, String fileName) {
        InputStream in = null;
        try {
            in = assets.open(fileName);
            DefaultHandler handler = new TexturePackHandler(gl, assets);
            Xml.parse(in, Encoding.US_ASCII, handler);
        } catch (SAXException e) {
            LogHelper.e("SAXException while parsing '" + fileName + "'");
            return false;
        } catch (IOException e) {
            LogHelper.e("IOException with file '" + fileName + "'");
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
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
    
    @Override
    public void refreshContextAssets(GL10 gl, AssetManager assets) {
        mTextureHolder.refreshContextAssets(gl, assets);
    }
    
    @Override
    public void releaseAssets(GL10 gl) {
        mTextureHolder.release(gl);        
    }
}
