package org.chemodansama.engine.tmx;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TmxTilesetParser extends DefaultHandler {
    private TmxTile tile;
    private int tsFirstgid;
    private TmxImage tsImage;
    private int tsMargin;
    private String tsName;
    private String tsImageName;
    
    private int tsSpacing;
    private int tsTileHeight;
    private ArrayList<TmxTile> tsTiles = new ArrayList<TmxTile>();
    private int tsTileWidth;
    
    protected final ArrayList<TmxTileset> mTilesets;
    private boolean parsingTileset = false;
    
    public TmxTilesetParser(ArrayList<TmxTileset> tilesets) {
        
        if (tilesets == null) {
            throw new IllegalArgumentException("tilesets == null");
        }
        
        mTilesets = tilesets;
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (localName.equalsIgnoreCase("tileset")) {
            mTilesets.add(new TmxTileset(tsFirstgid, tsName, tsImageName, 
                                         tsTileWidth, tsTileHeight, 
                                         tsSpacing, tsMargin, 
                                         tsImage, tsTiles));
            tsTiles.clear();
            parsingTileset = false;
        } if (localName.equalsIgnoreCase("tile")) {
            tsTiles.add(tile);
            tile = null;
        } 
    }
    
    private int getAttributeAsInt(Attributes attributes, String qName) {
        String value = attributes.getValue(qName);
        return (value != null) ? Integer.parseInt(value) : 0;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (localName.equalsIgnoreCase("tileset")) {
            tsFirstgid = getAttributeAsInt(attributes, "firstgid");
            tsName = attributes.getValue("name");
            tsTileHeight = getAttributeAsInt(attributes, "tileheight");
            tsTileWidth = getAttributeAsInt(attributes, "tilewidth");
            tsSpacing = getAttributeAsInt(attributes, "spacing");
            tsMargin = getAttributeAsInt(attributes, "margin");
            tsImageName = attributes.getValue("image");
            if ((tsImageName == null) || (tsImageName.equalsIgnoreCase(""))) {
                tsImageName = tsName;
            }
            
            parsingTileset = true;
            
        } else if (localName.equalsIgnoreCase("image")) {
            if (parsingTileset) {
                String source = attributes.getValue("source");
                int trans = Integer.parseInt(attributes.getValue("trans"), 16);
                int w = getAttributeAsInt(attributes, "width");
                int h = getAttributeAsInt(attributes, "height");
                tsImage = new TmxImage(source, trans, w, h);
            }
        } else if (localName.equalsIgnoreCase("tile")) {
            tile = new TmxTile(getAttributeAsInt(attributes, "id"));
        } else if (localName.equalsIgnoreCase("property")) {
            if (tile != null) {
                tile.addProperty(attributes.getValue("name"), 
                                 attributes.getValue("value"));
            } 
        }
    }
}