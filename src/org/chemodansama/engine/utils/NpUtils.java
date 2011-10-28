package org.chemodansama.engine.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.tmx.TmxTileset;
import org.chemodansama.engine.tmx.TmxTilesetParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

public class NpUtils {
    /** openStreamNoCatch - simply wraps opening stream, 
     *                      so no catch clause needed
     * @param fileName
     * @return returns InputStream object if no exception occurred,
     *                 null otherwise.
     */
    public static InputStream openStreamNoCatch(AssetManager assets, 
            String fileName) {
        
        InputStream in = null;
        try {
            in = assets.open(fileName);
        } catch (IOException e) {
            Log.e(LogTag.TAG, "IOException while open: " + fileName, e);
            return null;
        }
        return in;
    }
    
    public static int byteToUnsignedInt(byte b) {
        return b & 0xFF;
    }
    
    public static int getAttributeAsInt(Attributes attributes, String name) {
        String value = attributes.getValue(name);
        
        if ((value == null) || (value.equalsIgnoreCase(""))) {
            return 0;
        }
        
        return Integer.parseInt(value);
    }
    
    public static int[] readIntArray(String s) {
        if ((s == null) || (s.equalsIgnoreCase(""))) {
            return null;
        }
        
        String[] ints = s.split(" ");
        if ((ints == null) || (ints.length == 0)) {
            return null;
        }

        int[] r = new int[ints.length];
        int i = 0;
        for (String ss : ints) {
            r[i++] = Integer.parseInt(ss);
        }
        return r;
    }
    
    public static ArrayList<TmxTileset> parseTilesets(AssetManager assets, 
                                                      String fileName) throws IOException {

        ArrayList<TmxTileset> tilesets = new ArrayList<TmxTileset>();

        InputStream in = null;
        try {
            in = assets.open(fileName + TmxTileset.EXT);
            try {
                Xml.parse(in, Encoding.US_ASCII, 
                          new TmxTilesetParser(tilesets));
            } catch (SAXException e) {
                e.printStackTrace();
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return tilesets;
    }
}
