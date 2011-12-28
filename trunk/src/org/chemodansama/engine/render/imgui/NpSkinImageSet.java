package org.chemodansama.engine.render.imgui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.render.NpTexture;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

public final class NpSkinImageSet {
    
    public class NpSkinImage {
        /* Specifies the name that will be used to identify
           the image within the Imageset. Required attribute. */
        private String mName = null;
        /* XPos: Specifies the X pixel coordinate of
           the top-left corner of the image on the source surface.
           Required attribute. */
        private int mXPos = 0;
        /* YPos: Specifies the Y pixel coordinate of
           the top-left corner of the image on the source surface.
           Required attribute. */
        private int mYPos = 0;
        /* Width: Specifies the width of the image in pixels. 
         * Required attribute. */
        private int mWidth = 0;
        /* Height: Specifies the height of the image in pixels. 
         * Required attribute. */
        private int mHeight = 0;
        /* XOffset: Specifies a horizontal offset to apply when
           rendering the image. Optional attribute, default is 0. */
        private int mXOffset = 0;
        /* YOffset: Specifies a vertical offset to apply when 
         * rendering the image. Optional attribute, default is 0. */
        private int mYOffset = 0;
        
        private NpSkinImage(String name, int x, int y, int w, int h) {
            mName = name;
            mXPos = x;
            mYPos = y;
            mWidth = w;
            mHeight = h;
        }
        
        int getHeight() {
            return mHeight;
        }
        
        String getName() {
            return mName;
        }
        
        int getWidth() {
            return mWidth;
        }
        
        int getXOffset() {
            return mXOffset;
        }
        
        int getXPos() {
            return mXPos;
        }
        
        int getYOffset() {
            return mYOffset;
        }
        
        int getYPos() {
            return mYPos;
        }
    }
    private final class XmlImageSetReader extends DefaultHandler {
        
        private final GL10 mGL;
        private final AssetManager mAssets;

        private HashMap<String, NpSkinImage> constructImageList(String name,
                String layout, int x, int y,  int w, int h,  int cx, int cy, 
                int cw, int ch) {
            HashMap<String, NpSkinImage> r = new HashMap<String, NpSkinImage>();
            
            if ((name == null) || (layout == null)) {
                return r;
            }
            
            if (layout.equalsIgnoreCase("grid")) {
                r.put(name + "0", new NpSkinImage(name, x, y, cx, cy));
                r.put(name + "1", new NpSkinImage(name, x + cx, y, cw, cy));
                r.put(name + "2", new NpSkinImage(name, x + cx + cw, y,
                                                  w - cx - cw, cy));
                
                r.put(name + "3", new NpSkinImage(name, x, y + cy, cx, ch));
                r.put(name + "4", new NpSkinImage(name, x + cx, y + cy, 
                                                  cw, ch));
                r.put(name + "5", new NpSkinImage(name, x + cx + cw, y + cy, 
                                                  w - cx - cw, ch));
                
                r.put(name + "6", new NpSkinImage(name, x, y + cy + ch, cx, 
                                                  h - cy - ch));
                r.put(name + "7", new NpSkinImage(name, x + cx, y + cy + ch, 
                                                  cw, h - cy - ch));
                r.put(name + "8", new NpSkinImage(name, x + cx + cw, 
                                                  y + cy + ch, w - cx - cw, 
                                                  h - cy - ch));
            }

            return r;
        }
        
        private XmlImageSetReader(GL10 gl, AssetManager assets) {
            super();
            
            if (gl == null) {
                throw new IllegalArgumentException("gl == null");
            }
            
            if (assets == null) {
                throw new IllegalArgumentException("assets == null");
            }
            
            mGL = gl;
            mAssets = assets;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            
            if (localName.equalsIgnoreCase("imageset")) {
                
                mName = attributes.getValue("Name");
                mTextureName = attributes.getValue("Imagefile");
                
                if (mTextureName != null) {
                    try {
                        mTexture = new NpTexture(mGL, mTextureName, mAssets, 
                                                 true);
                    } catch (IOException e) {
                        LogHelper.e("IOException in NpTexture(" + mTextureName 
                                    + ")");
                    }
                }
            } else if (localName.equalsIgnoreCase("image")) {
                
                String name = attributes.getValue("Name");
                int x = Integer.parseInt(attributes.getValue("XPos"));
                int y = Integer.parseInt(attributes.getValue("YPos"));
                int w = Integer.parseInt(attributes.getValue("Width"));
                int h = Integer.parseInt(attributes.getValue("Height"));
                
                mImages.put(name, new NpSkinImage(name, x, y, w, h));
            } else if (localName.equalsIgnoreCase("imagelist")) {
                
                String name = attributes.getValue("Name");
                String layout = attributes.getValue("Layout");
                
                int x = Integer.parseInt(attributes.getValue("XPos"));
                int y = Integer.parseInt(attributes.getValue("YPos"));
                int w = Integer.parseInt(attributes.getValue("Width"));
                int h = Integer.parseInt(attributes.getValue("Height"));
                
                int cx = Integer.parseInt(attributes.getValue("ClientX"));
                int cy = Integer.parseInt(attributes.getValue("ClientY"));
                int cw = Integer.parseInt(attributes.getValue("ClientW"));
                int ch = Integer.parseInt(attributes.getValue("ClientH"));
                
                mImages.putAll(constructImageList(name, layout, x, y, w, h, 
                                                  cx, cy, cw, ch));
            }
        }
    }
    
    private String mName = "";
    
    private HashMap<String, NpSkinImage> mImages;
    
    private String mTextureName = null;
    private NpTexture mTexture = null;
    
    // initialization block goes here.
    {
        mImages = new HashMap<String, NpSkinImageSet.NpSkinImage>();
    }
    
    public NpSkinImageSet(GL10 gl, AssetManager assets, 
            String imageSetFileName) {
        try {
            Xml.parse(assets.open(imageSetFileName), Encoding.US_ASCII, 
                      new XmlImageSetReader(gl, assets));
        } catch (IOException e) {
            return;
        } catch (SAXException e) {
            return;
        }
    }
    
    public NpSkinImage getImage(String imageName) {
        return mImages.get(imageName);
    }
    
    public String getName() {
        return mName;
    }
    
    public NpTexture getTexture() {
        return mTexture;
    }
    
    void refreshTexture(GL10 gl, AssetManager assets) {
        if (gl == null) {
            LogHelper.e("Cant reload skin imageset: gl == null");
            return;
        }
        
        if (assets == null) {
            LogHelper.e("Cant reload skin imageset: assets == null");
            return;
        }
        
        if (mTexture == null) {
            LogHelper.e("Cant reload skin imageset: mTexture == null");
            return;
        }
        
        if (mTextureName == null) {
            LogHelper.e("Cant reload skin imageset: mTextureName == null");
            return;
        }
        
        try {
            InputStream in = assets.open(mTextureName);
            mTexture.reloadOnSurfaceCreated(gl, in, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
