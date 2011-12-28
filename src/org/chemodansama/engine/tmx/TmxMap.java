package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

public class TmxMap {

    private class TmxParser extends TmxTilesetParser {
       
        private final StringBuilder compressedData = new StringBuilder();
        private TmxDataCompression dataCompression;
        private TmxDataEncoding dataEncoding;
        private boolean isParsingData = false;
        private TmxLayer layer;
        private TmxObjectGroup objects = null;
        private TmxMapObject object = null;
        
        public TmxParser(ArrayList<TmxTileset> tilesets) {
            super(tilesets);
        }
        
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if (isParsingData && (layer != null) 
                    && (dataCompression != TmxDataCompression.NONE)
                    && (dataEncoding != TmxDataEncoding.NONE)) {
                compressedData.append(ch, start, length);
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            
            if ((localName == null) || (localName.equals(""))) {
                return;
            }
            
            if (localName.equalsIgnoreCase("layer")) {
                mLayers.add(layer);
                layer = null;
            } else if (localName.equalsIgnoreCase("data")) {
                if (isParsingData && (layer != null) 
                        && (dataCompression != TmxDataCompression.NONE)
                        && (dataEncoding != TmxDataEncoding.NONE)) {
                    layer.setCompressedData(compressedData.toString(), 
                                            dataEncoding, dataCompression);
                }
                dataEncoding = TmxDataEncoding.NONE;
                dataCompression = TmxDataCompression.NONE;
                compressedData.setLength(0);
                isParsingData = false;
            } else if (localName.equalsIgnoreCase("objectgroup")) {
                if (objects != null) {
                    mObjectsGroups.add(objects);
                    objects = null;
                }
            } else if (localName.equals("object")) {
                object = null;
            }
        }
        
        /**
         * @param attributes 
         * @param qName specifies requested attribute name
         * @return parsed int value of specified attribute 
         *         or zero if attribute is not exists.
         */
        private int getAttributeAsInt(Attributes attributes, String qName) {
            return getAttributeAsInt(attributes, qName, 0);
        }
        
        /**
         * @param attributes
         * @param qName specifies requested attribute name
         * @param defaultValue 
         * @return parsed {@code int} value of specified attribute 
         *         or defaultValue if attribute is not exists.
         */
        private int getAttributeAsInt(Attributes attributes, String qName, 
                int defaultValue) {
            String value = attributes.getValue(qName);
            return (value != null) ? Integer.parseInt(value) : defaultValue;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ((localName == null) || (localName.equals(""))) {
                return;
            }
            
            if (localName.equalsIgnoreCase("map")) {
                version    = attributes.getValue("version");
                width      = getAttributeAsInt(attributes, "width");
                height     = getAttributeAsInt(attributes, "height");
                tileWidth  = getAttributeAsInt(attributes, "tilewidth");
                tileHeight = getAttributeAsInt(attributes, "tileheight");
            } else if (localName.equalsIgnoreCase("property")) {
                
                TmxEntity e = null;
                
                e = (layer != null) ? layer : e;
                e = (objects != null) ? objects : e;
                e = (object != null) ? object : e;
                
                if (e != null) {
                    e.addProperty(attributes.getValue("name"), 
                                  attributes.getValue("value"));
                }
                
            } else if (localName.equalsIgnoreCase("layer")) {
                
                String visible = attributes.getValue("visible");
                
                boolean isVisible = (visible != null) 
                                    ? Integer.parseInt(visible) != 0 
                                    : true;
                
                layer = new TmxLayer(attributes.getValue("name"), 
                                     getAttributeAsInt(attributes, "width"), 
                                     getAttributeAsInt(attributes, "height"),
                                     isVisible);
            } else if (localName.equalsIgnoreCase("data")) {
                
                String compressionStr = attributes.getValue("compression");
                dataCompression = TmxDataCompression.getFromString(compressionStr);
                
                String encodingStr = attributes.getValue("encoding");
                dataEncoding = TmxDataEncoding.getFromString(encodingStr);

                isParsingData = true;
            } else if (localName.equalsIgnoreCase("objectgroup")) {
                
                int w = getAttributeAsInt(attributes, "width");
                int h = getAttributeAsInt(attributes, "height");
                
                boolean visible = 
                        getAttributeAsInt(attributes, "visible", 1) > 0; 
                
                objects = new TmxObjectGroup(attributes.getValue("name"), w, h, 
                                             visible);
                
            } else if (localName.equalsIgnoreCase("object")) {
                if (objects != null) {
                    int gid = getAttributeAsInt(attributes, "gid");
                    int x = getAttributeAsInt(attributes, "x");
                    int y = getAttributeAsInt(attributes, "y");
                    int w = getAttributeAsInt(attributes, "width");
                    int h = getAttributeAsInt(attributes, "height");
                    String type = attributes.getValue("type");
                    String name = attributes.getValue("name");
                    
                    if (gid != 0) {
                        for (TmxTileset ts : mTilesets) {
                            if (ts.containsGid(gid)) {
                                w = ts.tileWidthInPels;
                                h = ts.tileHeightInPels;
                                break;
                            }
                        }
                    }
                    
                    object = new TmxMapObject(name, gid, x, y, w, h, type); 
                    objects.addObject(object);
                }
            }
        }
    }
    private int height;
    private final ArrayList<TmxLayer> mLayers;
    private final ArrayList<TmxObjectGroup> mObjectsGroups;
    private final ArrayList<TmxTileset> mTilesets;
    private int tileHeight;
    
    private int tileWidth;
    private String version = "";
    
    private int width;
    
    public TmxMap(AssetManager assets, String fileName) throws IOException {

        mTilesets = new ArrayList<TmxTileset>();
        mLayers = new ArrayList<TmxLayer>();
        mObjectsGroups = new ArrayList<TmxObjectGroup>();

        InputStream is = null;
        try {
            is = assets.open(fileName);
            try {
                Xml.parse(is, Encoding.US_ASCII, new TmxParser(mTilesets));
            } catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getHeightInPels() {
        return height * tileHeight;
    }
    
    public TmxEntity getLayer(int i) {
        return ((i >= 0) && (i < mLayers.size())) ? mLayers.get(i) : null;
    }
    
    public TmxLayer getLayer(String name) {
        
        if (name == null) {
            return null;
        }
        
        for (TmxLayer layer : mLayers) {
            if (name.equalsIgnoreCase(layer.name)) {
                return layer;
            }
        }
        return null;
    }
    
    public Iterable<TmxLayer> getLayers() {
        return mLayers;
    }
    
    public int getLayersCount() {
        return mLayers.size();
    }
    
    public int getTileHeight() {
        return tileHeight;
    }
    
    public TmxTileset getTileset(int i) {
        return ((i >= 0) && (i < mTilesets.size())) ? mTilesets.get(i) : null;
    }
    
    public TmxTileset getTilesetByGid(int gid) {
        for (TmxTileset ts : mTilesets) {
            if (ts.containsGid(gid)) {
                return ts;
            }
        }
        return null;
    }

    public int getTilesetCount() {
        return mTilesets.size();
    }
    
    public Iterable<TmxTileset> getTilesets() {
        return mTilesets;
    }
    
    public Iterable<TmxObjectGroup> getObjectGroups() {
        return mObjectsGroups;
    }
    
    public TmxObjectGroup getObjectGroup(String name) {
        for (TmxObjectGroup og : mObjectsGroups) {
            String oName = og.name;
            
            if ((oName != null) && (oName.equals(name))) {
                return og;
            }
        }
        return null;
    }
    
    public int getTileWidth() {
        return tileWidth;
    }

    public String getVersion() {
        return version;
    }

    public int getWidth() {
        return width;
    }

    public int getWidthInPels() {
        return width * tileWidth;
    }
}
