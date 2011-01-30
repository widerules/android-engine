package org.chemodansama.engine.render.imgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

/**
 * @author Chemodansama
 * 
 * NpSkinScheme - presents XML description for the skin: pure data only.
 * No render methods at all. GL is used only for loading textures. 
 *
 */
final public class NpSkinScheme {

    private class SchemeXmlReader extends DefaultHandler {

        final private GL10 mGL;
        
        // widget look data for sax-parsing
        private String mWidgetName = "";
        
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
        
        private void addFont(String alias, String fontFileName, 
                String charsFileName) {
            //TODO: fill the procedure body
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

                String imageSet = attribs.getValue("ImageSet");
                String image = attribs.getValue("Image");

                String sourceStr = attribs.getValue("Source");

                NpWidgetDimSource source = 
                    NpWidgetDimSource.parseStr(sourceStr);

                return new NpWidgetDim(imageSet, image, source);
                
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
        
        private void resetCurrentDimAndOp() {
            mDim = null;
            mCurrentDim = null;
            mCurrentOp = null;
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            
            // quite readable, isn't it? >_<
            
            if (localName.equalsIgnoreCase("widget")) {
                mWidgetlook.put(mWidgetName, 
                                new NpWidgetlook(mWidgetName, mWidgetState));
                mWidgetName = "";
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
                        attributes.getValue("Filename"),
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

                NpWidgetDim mCurrentDim = createWidgetDim(attributes);

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
    
    final private HashMap<String, NpSkinImageSet> mImageSets = 
        new HashMap<String, NpSkinImageSet>();

    final private HashMap<String, NpWidgetlook> mWidgetlook = 
        new HashMap<String, NpWidgetlook>();
    
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
