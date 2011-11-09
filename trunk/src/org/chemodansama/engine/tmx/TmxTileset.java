package org.chemodansama.engine.tmx;

import java.util.ArrayList;
import java.util.Collection;

import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.math.NpVec2;

import android.util.Log;

public class TmxTileset {
    
    public final static String EXT = ".tilesets";
    
    public final int firstGid;
    public final int marging;
    public final TmxImage mImage;
    private final ArrayList<TmxTile> mTiles = new ArrayList<TmxTile>();
    public final String name;
    public final int spacing;

    public final int tileHeightInPels;
    
    public final int tilesCount;
    public final int tileWidthInPels;
    
    public final float tileWidth;
    public final float tileHeight;
    
    public final int widthInTiles;
    public final int heightInTiles;

    public final String imageName;
    
    public TmxTileset(int firstGid, String name, String imageName, 
            int tileWidthInPels, int tileHeightInPels,
            int spacing, int marging, TmxImage image, 
            Collection<TmxTile> tiles) {
        this.firstGid = firstGid;
        this.name = name;
        this.imageName = imageName;
        this.tileWidthInPels = tileWidthInPels;
        this.tileHeightInPels = tileHeightInPels;
        this.spacing = spacing;
        this.marging = marging;
        this.mImage = image;
        
        if (Log.isLoggable(LogTag.TAG, Log.INFO)) {
            Log.i(LogTag.TAG, "tileset '" + name + "' added");
        }
        
        if (tiles != null) {
            mTiles.addAll(tiles);
        }
        
        if (image == null) {
            throw new NullPointerException("image is null");
        }
        
        if (tileHeightInPels == 0) {
            throw new IllegalArgumentException("tileHeight cant be zero");
        }
        
        if (tileWidthInPels == 0) {
            throw new IllegalArgumentException("tileWidth cant be zero");
        }
        
        tileHeight = (float) tileHeightInPels / image.getHeight();
        tileWidth = (float) tileWidthInPels / image.getWidth();
        
        widthInTiles = image.getWidth() / (tileWidthInPels + spacing);
        heightInTiles = image.getHeight() / (tileHeightInPels + spacing);

        tilesCount = widthInTiles * heightInTiles;
    }
    
    public boolean containsGid(int gid) {
        return (gid >= firstGid) && (gid < firstGid + tilesCount);
    }
    
    public TmxTile getTile(int tileGid) {
        
        int tileId = tileGid - firstGid;
        
        for (TmxTile t : mTiles) {
            if (t.id == tileId) {
                return t;
            }
        }
        return null;
    }
    
    public boolean getTileTexcoords(int tileGid, float[] tc) {

        if ((tc == null) || (tc.length < 2)) {
            return false;
        }
        
        if (!containsGid(tileGid)) {
            return false;
        }
        
        int localId = tileGid - firstGid;
                
        int tileX = localId % widthInTiles;
        int tileY = localId / widthInTiles;

        int imageH = mImage.getHeight();
        
        tc[0] = (float)tileX * (tileWidthInPels + spacing) / mImage.getWidth();
        tc[1] = (float)(imageH - tileY * (tileHeightInPels + spacing)) / imageH; 
        
        return true;
    }
    
    public boolean getTileTexcoords(int tileGid, NpVec2 tc) {
        
        if (tc == null) {
            return false;
        }
        
        return getTileTexcoords(tileGid, tc.coords);
    }
}
