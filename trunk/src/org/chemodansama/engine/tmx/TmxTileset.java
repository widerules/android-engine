package org.chemodansama.engine.tmx;

import java.util.ArrayList;
import java.util.Collection;

import org.chemodansama.engine.LogTag;

import android.util.Log;

public class TmxTileset {
    private final int mFirstGid;
    private final String mName;
    private final int mTileWidth;
    private final int mTileHeight;
    private final int mSpacing;
    private final int mMarging;

    private final TmxImage mImage;
    
    private final ArrayList<TmxTile> mTiles;
    
    public TmxTileset(int firstGid, String name, int tileWidth, int tileHeight,
            int spacing, int marging, TmxImage image, 
            Collection<TmxTile> tiles) {
        mFirstGid = firstGid;
        mName = name;
        mTileWidth = tileWidth;
        mTileHeight = tileHeight;
        mSpacing = spacing;
        mMarging = marging;
        mImage = image;
        
        mTiles = new ArrayList<TmxTile>();
        
        if (Log.isLoggable(LogTag.TAG, Log.INFO)) {
            Log.i(LogTag.TAG, "tileset '" + name + "' added");
        }
        
        if (tiles != null) {
            mTiles.addAll(tiles);
        }
    }
    
    public int getFirstGid() {
        return mFirstGid;
    }

    public String getName() {
        return mName;
    }

    public int getTileWidth() {
        return mTileWidth;
    }

    public int getTileHeight() {
        return mTileHeight;
    }

    public int getSpacing() {
        return mSpacing;
    }

    public int getMarging() {
        return mMarging;
    }

    public TmxImage getImage() {
        return mImage;
    }
}
