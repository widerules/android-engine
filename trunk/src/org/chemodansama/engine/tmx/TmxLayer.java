package org.chemodansama.engine.tmx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.utils.Base64;

import android.util.Log;

abstract class LayerDataReader {

    protected final byte[] mCompressedData;
    protected final int mLevelWidth;
    protected final int mLevelHeight;
    
    public LayerDataReader(byte[] compressedData, int levelWidth, 
            int levelHeight) {
        mCompressedData = compressedData;
        mLevelWidth = levelWidth;
        mLevelHeight = levelHeight;
    }
    
    public abstract int[] getData() throws IOException;
}

class GZIPDataReader extends LayerDataReader {

    public GZIPDataReader(byte[] compressedData, int levelWidth, int levelHeight) {
        super(compressedData, levelWidth, levelHeight);
    }

    @Override
    public int[] getData() throws IOException {
        
        ByteArrayInputStream bais = new ByteArrayInputStream(mCompressedData);
        
        GZIPInputStream gis;
        gis = new GZIPInputStream(bais);
        
        int intSize = mLevelHeight * mLevelWidth;
        
        byte[] buf = new byte[intSize * 4];
        int[] out = new int[intSize];
        
        int r = gis.read(buf, 0, intSize * 4);

        int j = 0;
        for (int i = 0; i < r; i += 4) {
            out[j++] = (buf[i] & 0xFF) 
                    | ((buf[i + 1] & 0xFF) << 8)
                    | ((buf[i + 2] & 0xFF) << 16)
                    | ((buf[i + 3] & 0xFF) << 24);
        }
        
        return out;
    }
    
}

public class TmxLayer {
    public final String name;
    public final int width;
    public final int height;
    
    public final boolean isVisible;
    
    private final TreeMap<String, String> mProperties;
    private int[] mData;
    
    public TmxLayer(String name, int width, int height, boolean isVisible) {
        this.height = height;
        this.width = width;
        this.name = name;
        
        this.isVisible = isVisible;
        
        mProperties = new TreeMap<String, String>();
    }

    void addProperty(String name, String value) {
        mProperties.put(name, value);
    }
    
    public String getProperty(String name) {
        return mProperties.get(name);
    }
    
    void setCompressedData(String data, TmxDataEncoding encoding, 
            TmxDataCompression compression) {
        
        byte[] decoded;
        
        switch (encoding) {
        case BASE64:
            try {
                decoded = Base64.decode(data, Base64.DONT_GUNZIP);
            } catch (IOException e) {
                LogHelper.e("IOException while base64 decoding.");
                return;
            }
            break;

        default:
            decoded = data.getBytes();
            break;
        }
        
        LayerDataReader dr;
        
        switch (compression) {
        case GZIP:
            dr = new GZIPDataReader(decoded, width, width); 
            break;

        default:
            Log.e(LogTag.TAG, 
                  "Unsupported compression: " + compression.toString());
            
            return;
        }
        
        try {
            mData = dr.getData();
        } catch (IOException e) {
            Log.e(LogTag.TAG, "Can't decompress data. Layer data is not set.");   
        }
    }
    
    public int getTileGid(int x, int y) {
        boolean coordsAreValid = (y >= 0) && (y < height) 
                                    && (x >= 0) && (x < width);
           
        return (coordsAreValid) ? mData[y * width + x] : 0;
    }
}
