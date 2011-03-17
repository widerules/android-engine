package org.chemodansama.engine.render.imgui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogTag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

/**
 * @author Chemodansama
 * 
 * NpSkinScheme - represents XML description for the skin: pure data only.
 * No render methods at all. GL is used only for loading textures. 
 *
 */
final public class NpSkinScheme {

    private class SchemeXmlReader extends DefaultHandler implements LogTag {

        final private GL10 mGL;
        
        // widget look data for sax-parsing
        private String mWidgetName = null;
        
        final private EnumMap<NpWidgetState, NpWidgetStatelook> mWidgetState = 
            new EnumMap<NpWidgetState, NpWidgetStatelook>(NpWidgetState.class);
        
        final private ArrayList<NpWidgetArea> mWidgetAreas = 
            new ArrayList<NpWidgetArea>();
        
        final private ArrayList<NpWidgetImage> mWidgetImages = 
            new ArrayList<NpWidgetImage>();
        
        private NpWidgetState mCurrentState = NpWidgetState.WS_NORMAL;
        private String mAreaName = "";
        
        private NpWidgetDim mDim = null; // first dim in dim-ops-tree
        private NpWidgetDim mCurrentDim = null; // last dim in dim-ops-tree
        private NpWidgetDimOp mCurrentOp = null;
        
        private NpWidgetDim mXDim = null, 
                            mYDim = null, 
                            mWDim = null, 
                            mHDim = null;
        
        private SchemeXmlReader(GL10 gl) {
            mGL = gl;
        }
        
        private boolean addFont(String alias, String fontFileName, 
                String charsFileName) {
            InputStream fontTexStream = null;

            try {
                fontTexStream = mAssets.open(fontFileName);
            } catch (IOException e) {
                
                Log.e(TAG, "IOException while open: " + fontFileName);
                return false;
            }
            
            InputStream fontCharsStream = null;

            try {
                fontCharsStream = mAssets.open(charsFileName);
            } catch (IOException e) {
                Log.e(TAG, "IOException while open: " + charsFileName);
                return false;
            }
            
            if (!mFontsMap.containsKey(alias)) {
                NpFont f = new NpFont(mGL, alias, fontTexStream, 
                                      fontCharsStream);
                
                mFontsMap.put(alias, f);
            } else {
                Log.w(TAG, "duplicated font alias: " + alias);
            }
            
            return true;
        }
        
        private void addImageSet(String alias, String imageSetFileName) {
            NpSkinImageSet s = new NpSkinImageSet(mGL, mAssets, 
                                                  imageSetFileName); 
            mImageSets.put(alias, s);
        }
        
        private NpWidgetDim createWidgetDim(Attributes attribs) {
            
            String dimType = attribs.getValue("Type");
            
            if (dimType == null) {
                // provide some default dim (absolute, with value == 0.0f)
                return new NpWidgetDim(0.0f);
            }

            if (dimType.equalsIgnoreCase("Image")) {

                String area = attribs.getValue("Area");

                NpWidgetDimSource source = 
                    NpWidgetDimSource.parseStr(attribs.getValue("Source"));

                return new NpWidgetDim(area, source);
                
            } else if (dimType.equalsIgnoreCase("Scale")) {

                float value = Float.parseFloat(attribs.getValue("Value"));

                String sourceStr = attribs.getValue("Source");

                NpWidgetDimSource source = 
                    NpWidgetDimSource.parseStr(sourceStr);

                return new NpWidgetDim(value, source);

            } else {
                float value = Float.parseFloat(attribs.getValue("Value"));

                return new NpWidgetDim(value);
            } 
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            
            // quite readable, isn't it? >_<
            if (localName.equalsIgnoreCase("WidgetLook") 
                    && (mWidgetName != null)) {
                
                Log.w(LogTag.TAG, 
                      "WidgetName is not null. <Widget> wasnt closed");
                
            } else if (localName.equalsIgnoreCase("widget")) {
                mWidgetlook.put(mWidgetName, 
                                new NpWidgetlook(mWidgetName, mWidgetState));
                mWidgetName = null;
                mWidgetState.clear();
            } else if (localName.equalsIgnoreCase("state")) {
                mWidgetState.put(mCurrentState, 
                                 new NpWidgetStatelook(mWidgetAreas, 
                                                       mWidgetImages));
                mWidgetAreas.clear();
                mWidgetImages.clear();
            } else if (localName.equalsIgnoreCase("area")) {
                if ((mAreaName != null) && (mXDim != null) 
                        && (mYDim != null) && (mWDim != null) 
                        && (mHDim != null)) {
                    mWidgetAreas.add(new NpWidgetArea(mAreaName, 
                                                      mXDim, mYDim, 
                                                      mWDim, mHDim));
                }
                mXDim = null;
                mYDim = null;
                mHDim = null;
                mWDim = null;
            } else if (localName.equalsIgnoreCase("x")) {
                mXDim = mDim;
                resetCurrentDimAndOp();
            } else if (localName.equalsIgnoreCase("y")) {
                mYDim = mDim;
                resetCurrentDimAndOp();
            } else if (localName.equalsIgnoreCase("width")) {
                mWDim = mDim;
                resetCurrentDimAndOp();
            } else if (localName.equalsIgnoreCase("height")) {
                mHDim = mDim;
                resetCurrentDimAndOp();
            }  
        }
        
        private void resetCurrentDimAndOp() {
            mDim = null;
            mCurrentDim = null;
            mCurrentOp = null;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            
            super.startElement(uri, localName, qName, attributes);

            // some reeeeaaaly awesome code goes here :E
            
            if (localName.equalsIgnoreCase("imageset")) {
                addImageSet(attributes.getValue("Name"), 
                            attributes.getValue("Filename"));
            } else if (localName.equalsIgnoreCase("font")) {
                addFont(attributes.getValue("Name"), 
                        attributes.getValue("File"),
                        attributes.getValue("Chars"));
            } else if (localName.equalsIgnoreCase("widget")) {
                mWidgetName = attributes.getValue("Name");
            } else if (localName.equalsIgnoreCase("state")) {
                mCurrentState = 
                    NpWidgetState.parseStr(attributes.getValue("Type"));
            } else if (localName.equalsIgnoreCase("areas")) {
                mWidgetAreas.clear();
            } else if (localName.equalsIgnoreCase("images")) {
                mWidgetImages.clear();
            } else if (localName.equalsIgnoreCase("area")) {
                mAreaName = attributes.getValue("Name");
            } else if (localName.equalsIgnoreCase("dim")) {

                mCurrentDim = createWidgetDim(attributes);

                if (mDim == null) {
                    mDim = mCurrentDim;
                } else if (mCurrentOp != null) {
                    mCurrentOp.setDim(mCurrentDim);
                }
            } else if (localName.equalsIgnoreCase("dimoperator")) {

                if (mCurrentDim != null) {
                    String s = attributes.getValue("op");
                    NpWidgetDimOpType t = NpWidgetDimOpType.parseStr(s);

                    mCurrentOp = new NpWidgetDimOp(t);

                    mCurrentDim.setOperator(mCurrentOp);
                }
            } else if (localName.equalsIgnoreCase("image")) {
                String area     = attributes.getValue("Area");
                String imageset = attributes.getValue("Imageset");
                String image    = attributes.getValue("Image");
                
                mWidgetImages.add(new NpWidgetImage(area, imageset, image));
            }
        }
    }
    
    final private HashMap<String, NpFont> mFontsMap = 
        new HashMap<String, NpFont>();
    
    final private HashMap<String, NpSkinImageSet> mImageSets = 
        new HashMap<String, NpSkinImageSet>();

    final private TreeMap<String, NpWidgetlook> mWidgetlook = 
        new TreeMap<String, NpWidgetlook>();
    
    final private AssetManager mAssets;
    
    public NpSkinScheme(GL10 gl, AssetManager assets, String schemeFileName) {
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
    
    public HashMap<String, NpFont> getFonts() {
        return mFontsMap;
    }
    
    public NpSkinImageSet getImageSet(String imageSetName) {
        if (imageSetName != null) {
            return mImageSets.get(imageSetName);
        } else {
            return null;
        }
    }
    
    public NpWidgetlook getWidget(String widgetName) {
        return mWidgetlook.get(widgetName);
    }
}