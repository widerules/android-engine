package org.chemodansama.engine.render.imgui;

import java.io.IOException;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.render.NpTexture;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Log;
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
        
        private GL10 mGL = null;

        private XmlImageSetReader(GL10 gl) {
            super();
            
            mGL = gl;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            
            if (localName.equalsIgnoreCase("imageset")) {
                
                mName = attributes.getValue("Name");
                
                String imageFile = attributes.getValue("Imagefile");
                
                if ((imageFile != null) && (mGL != null) && (mAssets != null)) {
                    mTexture = new NpTexture();
                    
                    try {
                        if (mTexture.initFromStream(mAssets.open(imageFile))) {
                            mTexture.initGL10(mGL);
                            mTexture.releaseMips();
                            
                            Log.i(LogTag.TAG, imageFile + " loaded");
                        } else {
                            Log.e(LogTag.TAG, imageFile + " wasn't loaded");
                        }
                    } catch (IOException e) {
                        
                        Log.e(LogTag.TAG, "IOException while reading " 
                              + imageFile);
                        
                        return;
                    }
                }
                
            } else if (localName.equalsIgnoreCase("image")) {
                
                String name = attributes.getValue("Name");
                int x = Integer.parseInt(attributes.getValue("XPos"));
                int y = Integer.parseInt(attributes.getValue("YPos"));
                int w = Integer.parseInt(attributes.getValue("Width"));
                int h = Integer.parseInt(attributes.getValue("Height"));
                
                mImages.put(name, new NpSkinImage(name, x, y, w, h));
            }
        }
    }
    
    private String mName = "";
    
    private HashMap<String, NpSkinImage> mImages;
    
    private NpTexture mTexture = null;

    private AssetManager mAssets = null;
    
    // initialization block goes here.
    {
        mImages = new HashMap<String, NpSkinImageSet.NpSkinImage>();
    }
    
    public NpSkinImageSet(GL10 gl, AssetManager assets, 
            String imageSetFileName) {
        
        mAssets = assets;
        
        try {
            Xml.parse(assets.open(imageSetFileName), Encoding.US_ASCII, 
                      new XmlImageSetReader(gl));
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
    
}
