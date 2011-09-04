package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

public class TmxMap {

    private class TmxParser extends DefaultHandler {
       
        private final StringBuilder compressedData = new StringBuilder();
        private TmxDataCompression dataCompression;
        private TmxDataEncoding dataEncoding;
        private boolean isParsingData = false;
        private TmxLayer layer;
        private TmxTile tile;
        private int tsFirstgid;
        private TmxImage tsImage;
        private int tsMargin;
        private String tsName;
        
        private int tsSpacing;
        private int tsTileHeight;
        private ArrayList<TmxTile> tsTiles = new ArrayList<TmxTile>();
        private int tsTileWidth;
        
        public TmxParser() {
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
            
            if (localName.equalsIgnoreCase("tileset")) {
                flushTileSet();
            } else if (localName.equalsIgnoreCase("tile")) {
                tsTiles.add(tile);
                tile = null;
            } else if (localName.equalsIgnoreCase("layer")) {
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
            }
        }
        
        private void flushTileSet() {
            mTilesets.add(new TmxTileset(tsFirstgid, tsName, 
                                         tsTileWidth, tsTileHeight, 
                                         tsSpacing, tsMargin, 
                                         tsImage, tsTiles));
            tsTiles.clear();
        }
        
        private int getAttributeAsInt(Attributes attributes, String qName) {
            String value = attributes.getValue(qName);
            return (value != null) ? Integer.parseInt(value) : 0;
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
            } else if (localName.equalsIgnoreCase("tileset")) {
                tsFirstgid = getAttributeAsInt(attributes, "firstgid");
                tsName = attributes.getValue("name");
                tsTileHeight = getAttributeAsInt(attributes, "tileheight");
                tsTileWidth = getAttributeAsInt(attributes, "tilewidth");
                tsSpacing = getAttributeAsInt(attributes, "spacing");
                tsMargin = getAttributeAsInt(attributes, "margin");
            } else if (localName.equalsIgnoreCase("image")) {
                
                String source = attributes.getValue("source");
                int trans = Integer.parseInt(attributes.getValue("trans"), 16);
                int w = getAttributeAsInt(attributes, "width");
                int h = getAttributeAsInt(attributes, "height");
                
                tsImage = new TmxImage(source, trans, w, h);
            } else if (localName.equalsIgnoreCase("tile")) {
                tile = new TmxTile(getAttributeAsInt(attributes, "id"));
            } else if (localName.equalsIgnoreCase("property")) {
                if (tile != null) {
                    tile.addParam(attributes.getValue("name"), 
                                  attributes.getValue("value"));
                } else if (layer != null) {
                    layer.addProperty(attributes.getValue("name"), 
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
            }
        }
    }
    private int height;
    private final ArrayList<TmxLayer> mLayers;
    private final ArrayList<TmxTileset> mTilesets;
    private int tileHeight;
    
    private int tileWidth;
    private String version = "";
    
    private int width;
    
    public TmxMap(AssetManager assets, String fileName) throws IOException {

        mTilesets = new ArrayList<TmxTileset>();
        mLayers = new ArrayList<TmxLayer>();
        
        InputStream is = assets.open(fileName);
        try {
            Xml.parse(is, Encoding.US_ASCII, new TmxParser());
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
        is.close();
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getHeightInPels() {
        return height * tileHeight;
    }
    
    public TmxLayer getLayer(int i) {
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
