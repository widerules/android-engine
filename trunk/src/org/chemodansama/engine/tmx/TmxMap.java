package org.chemodansama.engine.tmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ListIterator;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.utils.NpUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

public class TmxMap {

    private String version = "";
    private int width;
    private int height;
    private int tileWidth;
    private int tileHeight;
    
    private final ArrayList<TmxTileset> mTilesets;
    private final ArrayList<TmxLayer> mLayers;
    
    private class TmxParser extends DefaultHandler {
       
        private int tsFirstgid;
        private String tsName;
        private int tsTileWidth;
        private int tsTileHeight;
        private TmxImage tsImage;
        private int tsSpacing;
        private int tsMargin;
        private ArrayList<TmxTile> tsTiles = new ArrayList<TmxTile>();
        private TmxTile tile;
        private TmxLayer layer;
        
        private final StringBuilder compressedData = new StringBuilder();
        private TmxDataEncoding dataEncoding;
        private TmxDataCompression dataCompression;
        private boolean isParsingData = false;
        
        public TmxParser() {
        }
        
        private int getAttributeAsInt(Attributes attributes, String qName) {
            String value = attributes.getValue(qName);
            return (value != null) ? Integer.parseInt(value) : 0;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ((qName == null) || (qName.equals(""))) {
                return;
            }
            
            if (qName.equalsIgnoreCase("map")) {
                version    = attributes.getValue("version");
                width      = getAttributeAsInt(attributes, "width");
                height     = getAttributeAsInt(attributes, "height");
                tileWidth  = getAttributeAsInt(attributes, "tilewidth");
                tileHeight = getAttributeAsInt(attributes, "tileheight");
            } else if (qName.equalsIgnoreCase("tileset")) {
                tsFirstgid = getAttributeAsInt(attributes, "firstdig");
                tsName = attributes.getValue("name");
                tsTileHeight = getAttributeAsInt(attributes, "tileheight");
                tsTileWidth = getAttributeAsInt(attributes, "tilewidth");
                tsSpacing = getAttributeAsInt(attributes, "spacing");
                tsMargin = getAttributeAsInt(attributes, "margin");
            } else if (qName.equalsIgnoreCase("image")) {
                
                String source = attributes.getValue("source");
                int trans = Integer.parseInt(attributes.getValue("trans"), 16);
                int w = getAttributeAsInt(attributes, "width");
                int h = getAttributeAsInt(attributes, "height");
                
                tsImage = new TmxImage(source, trans, w, h);
            } else if (qName.equalsIgnoreCase("tile")) {
                tile = new TmxTile(getAttributeAsInt(attributes, "id"));
            } else if (qName.equalsIgnoreCase("property")) {
                if (tile != null) {
                    tile.addParam(attributes.getValue("name"), 
                                  attributes.getValue("value"));
                } else if (layer != null) {
                    layer.addProperty(attributes.getValue("name"), 
                                      attributes.getValue("value"));
                }
            } else if (qName.equalsIgnoreCase("layer")) {
                layer = new TmxLayer(attributes.getValue("name"), 
                                     getAttributeAsInt(attributes, "width"), 
                                     getAttributeAsInt(attributes, "height"));
            } else if (qName.equalsIgnoreCase("data")) {
                
                String compressionStr = attributes.getValue("compression");
                dataCompression = TmxDataCompression.getFromString(compressionStr);
                
                String encodingStr = attributes.getValue("encoding");
                dataEncoding = TmxDataEncoding.getFromString(encodingStr);

                isParsingData = true;
            }
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
        
        private void flushTileSet() {
            mTilesets.add(new TmxTileset(tsFirstgid, tsName, 
                                         tsTileWidth, tsTileHeight, 
                                         tsSpacing, tsMargin, 
                                         tsImage, tsTiles));
            tsTiles.clear();
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            
            if ((qName == null) || (qName.equals(""))) {
                return;
            }
            
            if (qName.equalsIgnoreCase("tileset")) {
                flushTileSet();
            } else if (qName.equalsIgnoreCase("tile")) {
                tsTiles.add(tile);
                tile = null;
            } else if (qName.equalsIgnoreCase("layer")) {
                mLayers.add(layer);
                layer = null;
            } else if (qName.equalsIgnoreCase("data")) {
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
    }
    
    public TmxMap(AssetManager assets, String fileName) 
            throws IOException, IllegalArgumentException {
        
        mTilesets = new ArrayList<TmxTileset>();
        mLayers = new ArrayList<TmxLayer>();
        
        InputStream is = assets.open(fileName);
        
        try {
            Xml.parse(is, Encoding.US_ASCII, new TmxParser());
        } catch (SAXException e) {
            String msg = "SAXException while parsing '" + fileName + "'";
            LogHelper.e(msg);
            throw new IOException(msg);
        }
    }
    
    public int getLayersCount() {
        return mLayers.size();
    }
    
    public TmxLayer getLayer(int i) {
        return ((i >= 0) && (i < mLayers.size())) ? mLayers.get(i) : null;
    }
    
    public ListIterator<TmxLayer> getLayers() {
        return mLayers.listIterator();
    }
    
    public int getTilesetCount() {
        return mTilesets.size();
    }
    
    public TmxTileset getTileset(int i) {
        return ((i >= 0) && (i < mTilesets.size())) ? mTilesets.get(i) : null;
    }
    
    public ListIterator<TmxTileset> getTilesets() {
        return mTilesets.listIterator();
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
    
    public int getHeightInPels() {
        return height * tileHeight;
    }

    public int getHeight() {
        return height;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }
}
