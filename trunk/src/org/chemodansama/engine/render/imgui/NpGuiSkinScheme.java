package org.chemodansama.engine.render.imgui;

import java.io.IOException;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

final public class NpGuiSkinScheme {
    
    private class SchemeXmlReader extends DefaultHandler {

        static private final int INVALID    = 0;
        static private final int DOC        = 1;
        static private final int IMAGESETS  = 2;
        static private final int FONTS      = 3;
        static private final int WIDGETLOOK = 4;
        static private final int WIDGET     = 5;
        
        private GL10 mGL = null;
        
        private int mState = INVALID;
        
        private SchemeXmlReader(GL10 gl) {
            mGL = gl;
        }
        
        private void addFont(String alias, String fontFileName, 
                String charsFileName) {
            
        }
        
        private void addImageSet(String alias, String imageSetFileName) {
            NpGuiImageSet s = new NpGuiImageSet(mGL, mAssets, imageSetFileName); 
            mImageSets.put(alias, s);
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            
            if (localName.equalsIgnoreCase("imagesets") 
                    || localName.equalsIgnoreCase("fonts") 
                    || localName.equalsIgnoreCase("widgetlook")) {
                mState = DOC;
            } else if (localName.equalsIgnoreCase("widget")) {
                if (mState == WIDGET) {
                    mState = WIDGETLOOK;
                }
            }
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            
            super.startElement(uri, localName, qName, attributes);

            if (mState == INVALID) {
                if (localName.equalsIgnoreCase("guischeme")) {
                    mState = DOC;
                } 
            } else if (mState == DOC) {
                if (localName.equalsIgnoreCase("imagesets")) {
                    mState = IMAGESETS;
                } else if (localName.equalsIgnoreCase("fonts")) {
                    mState = FONTS;
                } else if (localName.equalsIgnoreCase("widgetlook")) {
                    mState = WIDGETLOOK;
                }
            } else if (mState == IMAGESETS) {
                if (localName.equalsIgnoreCase("imageset")) {
                    addImageSet(attributes.getValue("Name"), 
                                attributes.getValue("Filename"));
                }
            } else if (mState == FONTS) {
                if (localName.equalsIgnoreCase("font")) {
                    addFont(attributes.getValue("Name"), 
                            attributes.getValue("Filename"),
                            attributes.getValue("Chars"));
                }
            } else if (mState == WIDGETLOOK) {
                if (localName.equalsIgnoreCase("widget")) {
                    mState = WIDGET;
                }
            }
        }
    }
    
    private HashMap<String, NpGuiImageSet> mImageSets = null;

    {
        mImageSets = new HashMap<String, NpGuiImageSet>();
    }
    
    private AssetManager mAssets = null;
    
    public NpGuiSkinScheme(GL10 gl, AssetManager assets, String schemeFileName) {
        mAssets = assets;
        
        try {
            Xml.parse(assets.open(schemeFileName), Encoding.US_ASCII, 
                      new SchemeXmlReader(gl));
        } catch (IOException e) {
            return;
        } catch (SAXException e) {
            return;
        }
    }
}
