package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.utils.NpUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

public class TmxTexturePack {
    private final TreeMap<String, NpTexture> mTextures;
    
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
                
                InputStream in = NpUtils.openStreamNoCatch(mAssets, texture);
                
                if (in == null) {
                    return;
                }
                
                try {
                    mTextures.put(alias, new NpTexture(mGL, in, true));
                } catch (IllegalArgumentException e) {
                    LogHelper.e("Can't instantiate texture '" + texture + "'");
                    return;
                }
                
                LogHelper.i("texture '" + texture + "' added as '" 
                            + alias + "'");
            }
        }
    }
    
    /**
     * 
     * @param gl
     * @param assets
     * @param fileName
     * @throws IOException if the specified file cannot be opened.
     */
    public TmxTexturePack(GL10 gl, AssetManager assets, String fileName) 
            throws IOException {
        mTextures = new TreeMap<String, NpTexture>();

        InputStream in = assets.open(fileName);
        
        DefaultHandler handler = new TexturePackHandler(gl, assets);
        
        try {
            Xml.parse(in, Encoding.US_ASCII, handler);
        } catch (IOException e) {
            throw new IllegalArgumentException("IOException while parsing", e);
        } catch (SAXException e) {
            throw new IllegalArgumentException("SAXException while parsing", e);
        }
    }
}
